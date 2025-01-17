package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.model.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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


    public ListResourceRecordSetsResponse addResourceRecordByServer(Server server) {
        String hostedZoneName = getHostedZoneResponse().hostedZone().name();

        ChangeResourceRecordSetsRequest request =
                getListResourceRecordSetsResponse().thenApply(
                        response -> {
                            ResourceRecordSet resourceRecordSet = response.resourceRecordSets().stream()
                                    .filter(recordSet -> recordSet.name().equals(server.getResourceRecordSetName(hostedZoneName)))
                                    .findFirst()
                                    .map(it -> appendResourceRecordToResourceRecordSet(server, it))
                                    .orElseGet(() -> createNewResourceRecordSet(server, hostedZoneName));

                            return ChangeResourceRecordSetsRequest.builder()
                                    .hostedZoneId(properties.hostedZoneId())
                                    .changeBatch(
                                            ChangeBatch.builder()
                                                    .changes(List.of(toChange(resourceRecordSet)))
                                                    .build())
                                    .build();
                        }
                ).join();

        changeResourceRecordSets(request);
        
        return ListResourceRecordSetsResponse.builder()
                .resourceRecordSets(toResourceRecordSets(request))
                .build();
    }

    private static ResourceRecordSet createNewResourceRecordSet(Server server, String hostedZoneName) {
        return ResourceRecordSet.builder()
                .name(server.getResourceRecordSetName(hostedZoneName))
                .setIdentifier(server.clusterSubdomain())
                .resourceRecords(
                        ResourceRecord.builder().value(server.ipAddress()).build()
                ).build();
    }

    private static ResourceRecordSet appendResourceRecordToResourceRecordSet(Server server, ResourceRecordSet it) {
        List<ResourceRecord> resourceRecords = it.resourceRecords();
        resourceRecords.add(server.toResourceRecord());
        return it.toBuilder().resourceRecords(resourceRecords).build();
    }


    public ListResourceRecordSetsResponse removeResourceRecordByValue(
            String value
    ) {
        ChangeResourceRecordSetsRequest request =
                getListResourceRecordSetsResponse().thenApply(
                        response -> ChangeResourceRecordSetsRequest.builder()
                                .hostedZoneId(properties.hostedZoneId())
                                .changeBatch(
                                        ChangeBatch.builder()
                                                .changes(response.resourceRecordSets().stream()
                                                        .filter(AwsR53Service::isARecord)
                                                        .map(recordSet -> toChange(
                                                                removeResourceRecordByValue(recordSet, value)
                                                            )
                                                        )
                                                        .toList())
                                                .build())
                                .build()
                ).join();

        changeResourceRecordSets(request);

        return ListResourceRecordSetsResponse.builder()
                .resourceRecordSets(toResourceRecordSets(request))
                .build();
    }

    private static List<ResourceRecordSet> toResourceRecordSets(ChangeResourceRecordSetsRequest request) {
        return request.changeBatch()
                .changes()
                .stream()
                .map(Change::resourceRecordSet)
                .collect(Collectors.toList());
    }

    private static Change toChange(ResourceRecordSet recordSet) {
        return isEmpty(recordSet) ? toDeleteChange(recordSet) : toUpsertChange(recordSet);
    }

    private static boolean isEmpty(ResourceRecordSet recordSet) {
        return recordSet.resourceRecords().isEmpty();
    }

    private static Change toUpsertChange(ResourceRecordSet recordSet) {
        return Change.builder()
                .resourceRecordSet(recordSet)
                .action(ChangeAction.UPSERT)
                .build();
    }

    private static Change toDeleteChange(ResourceRecordSet recordSet){
        return Change.builder()
                .resourceRecordSet(recordSet)
                .action(ChangeAction.DELETE)
                .build();
    }

    private static ResourceRecordSet removeResourceRecordByValue(ResourceRecordSet recordSet, String value){
        return recordSet.toBuilder().resourceRecords(
                recordSet.resourceRecords().stream()
                        .filter(resourceRecord -> !resourceRecord.value().equals(value))
                        .toList()
        ).build();
    }

    private CompletableFuture<ListResourceRecordSetsResponse> getListResourceRecordSetsResponse() {
        return route53AsyncClient.listResourceRecordSets(generateListResourceRecordSetsRequest());
    }

    private ListResourceRecordSetsRequest generateListResourceRecordSetsRequest() {
        return ListResourceRecordSetsRequest.builder()
                .hostedZoneId(properties.hostedZoneId()).build();
    }

    private void changeResourceRecordSets(ChangeResourceRecordSetsRequest request) {
        //TODO catch exception
        route53AsyncClient.changeResourceRecordSets(request);
    }

    private static boolean isARecord(ResourceRecordSet resourceRecordSet){
        return resourceRecordSet.type().equals(RRType.A);
    }

    private GetHostedZoneResponse getHostedZoneResponse(){
        return route53AsyncClient.getHostedZone(GetHostedZoneRequest.builder()
                .id(properties.hostedZoneId())
                .build()).join();
    }
}
