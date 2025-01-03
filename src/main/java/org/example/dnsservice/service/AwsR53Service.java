package org.example.dnsservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AwsR53Service {

    private final Route53AsyncClient route53AsyncClient;

    @Autowired
    public AwsR53Service(Route53AsyncClient route53AsyncClient) {
        this.route53AsyncClient = route53AsyncClient;
    }

    public CompletableFuture<ListResourceRecordSetsResponse> getResourceRecordSets(String hostedZoneId){
        return getListResourceRecordSetsResponse(hostedZoneId);
    }

    private CompletableFuture<ListResourceRecordSetsResponse> getListResourceRecordSetsResponse(String hostedZoneId) {
        return route53AsyncClient.listResourceRecordSets(generateListResourceRecordSetsRequest(hostedZoneId));
    }

    private ListResourceRecordSetsRequest generateListResourceRecordSetsRequest(String hostedZoneId) {
        return ListResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId).build();
    }

    public ListResourceRecordSetsResponse upsertResourceRecordSet(
            String hostedZoneId, String ipAddress) {
        CompletableFuture<ListResourceRecordSetsResponse> listResourceRecordSetsResponse =
                getListResourceRecordSetsResponse(hostedZoneId);
        List<ResourceRecordSet> resourceRecordSets =
                listResourceRecordSetsResponse
                        .thenApply(
                                response ->
                                        response.resourceRecordSets().stream()
                                                .filter(this::isARecord)
                                                .map(
                                                        resourceRecordSet ->
                                                                toResourceRecordSetWithFilteredIp(ipAddress, resourceRecordSet))
                                                .toList())
                        .join();

        deleteARecord(hostedZoneId, resourceRecordSets);

        return ListResourceRecordSetsResponse.builder()
                .resourceRecordSets(resourceRecordSets)
                .build();
    }

    private void deleteARecord(String hostedZoneId, List<ResourceRecordSet> resourceRecordSets) {
        route53AsyncClient.changeResourceRecordSets(
                toChangeResourceRecordSetsRequest(hostedZoneId, resourceRecordSets)
        );
    }

    private static ChangeResourceRecordSetsRequest toChangeResourceRecordSetsRequest(String hostedZoneId, List<ResourceRecordSet> resourceRecordSets) {
        return ChangeResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId)
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

    private static ResourceRecordSet toResourceRecordSetWithFilteredIp(String ipAddress, ResourceRecordSet resourceRecordSet) {
        List<ResourceRecord> filtered =
                resourceRecordSet.resourceRecords().stream().filter(
                        resourceRecord -> !matchesIpAddress(ipAddress, resourceRecord)
                ).toList();

        return ResourceRecordSet.builder()
                .name(resourceRecordSet.name())
                .type(resourceRecordSet.type())
                .setIdentifier(resourceRecordSet.setIdentifier())
                .ttl(resourceRecordSet.ttl())
                .weight(resourceRecordSet.weight())
                .resourceRecords(filtered).build();
    }

    private static boolean matchesIpAddress(String ipAddress, ResourceRecord resourceRecord) {
        return resourceRecord.value().equals(ipAddress);
    }

    private boolean isARecord(ResourceRecordSet resourceRecordSet) {
        return resourceRecordSet.type().equals(RRType.A);
    }
}
