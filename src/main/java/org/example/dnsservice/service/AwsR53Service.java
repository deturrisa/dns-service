package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AwsR53Service {

    private final Route53AsyncClient route53AsyncClient;
    private final R53Properties properties;

    @Autowired
    public AwsR53Service(Route53AsyncClient route53AsyncClient, R53Properties properties) {
        this.route53AsyncClient = route53AsyncClient;
        this.properties = properties;
    }

    public CompletableFuture<ListResourceRecordSetsResponse> getResourceRecordSets(){
        return getListResourceRecordSetsResponse();
    }

    public ListResourceRecordSetsResponse upsertResourceRecordSet(
            String ipAddress
    ) {
        List<ResourceRecordSet> resourceRecordSets =
                getListResourceRecordSetsResponse().thenApply(
                        response -> response.resourceRecordSets().stream()
                                .filter(AwsR53Service::isARecord)
                                .map(resourceRecordSet ->
                                                applyIpToRemoveFromResourceRecordSet(resourceRecordSet,ipAddress)
                                ).toList()
                ).join();

        upsertARecord(resourceRecordSets);

        return ListResourceRecordSetsResponse.builder()
                .resourceRecordSets(resourceRecordSets)
                .build();
    }

    private CompletableFuture<ListResourceRecordSetsResponse> getListResourceRecordSetsResponse() {
        return route53AsyncClient.listResourceRecordSets(generateListResourceRecordSetsRequest());
    }

    private ListResourceRecordSetsRequest generateListResourceRecordSetsRequest() {
        return ListResourceRecordSetsRequest.builder()
                .hostedZoneId(properties.hostedZoneId()).build();
    }

    private static ResourceRecordSet applyIpToRemoveFromResourceRecordSet(ResourceRecordSet resourceRecordSet, String ipAddress) {
        List<ResourceRecord> updatedRecords =
                resourceRecordSet.resourceRecords().stream()
                        .filter(resourceRecord -> !matchIPAddressWithResourceRecord(ipAddress, resourceRecord))
                        .toList();

        return applyResourceRecordsToResourceRecordSet(resourceRecordSet, updatedRecords);
    }

    private static ResourceRecordSet applyResourceRecordsToResourceRecordSet(
            ResourceRecordSet resourceRecordSet,
            List<ResourceRecord> resourceRecords
    ) {
        return ResourceRecordSet.builder()
                .name(resourceRecordSet.name())
                .type(resourceRecordSet.type())
                .setIdentifier(resourceRecordSet.setIdentifier())
                .ttl(resourceRecordSet.ttl())
                .weight(resourceRecordSet.weight())
                .resourceRecords(resourceRecords).build();
    }

    private static boolean matchIPAddressWithResourceRecord(String ipAddress, ResourceRecord resourceRecord) {
        return resourceRecord.value().equals(ipAddress);
    }

    private void upsertARecord(List<ResourceRecordSet> resourceRecordSets) {
        route53AsyncClient.changeResourceRecordSets(
                toChangeResourceRecordSetsRequest(resourceRecordSets)
        );
    }

    private ChangeResourceRecordSetsRequest toChangeResourceRecordSetsRequest(List<ResourceRecordSet> resourceRecordSets) {
        return ChangeResourceRecordSetsRequest.builder()
                .hostedZoneId(properties.hostedZoneId())
                .changeBatch(
                        ChangeBatch.builder()
                                .changes(resourceRecordSets.stream()
                                        .map(recordSet -> Change.builder()
                                                .action(ChangeAction.UPSERT)
                                                .resourceRecordSet(recordSet)
                                                .build())
                                        .toList())
                                .build())
                .build();
    }

    private static boolean isARecord(ResourceRecordSet resourceRecordSet){
        return resourceRecordSet.type().equals(RRType.A);
    }
}
