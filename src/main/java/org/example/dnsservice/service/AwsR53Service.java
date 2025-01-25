package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.exception.R53AddRecordException;
import org.example.dnsservice.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AwsR53Service {

    private final Route53AsyncClient route53AsyncClient;
    private final R53Properties properties;

    private static final Logger log = LoggerFactory.getLogger(AwsR53Service.class);

    @Autowired
    public AwsR53Service(Route53AsyncClient route53AsyncClient, R53Properties properties) {
        this.route53AsyncClient = route53AsyncClient;
        this.properties = properties;
    }

    public CompletableFuture<ListResourceRecordSetsResponse> getResourceRecordSets(){
        return getListResourceRecordSetsResponse();
    }

    public ListResourceRecordSetsResponse addResourceRecordByServer(Server server) {
        ChangeResourceRecordSetsRequest request =
                getListResourceRecordSetsResponse().thenApply(
                        response -> {
                            List<ResourceRecordSet> resourceRecordSets =
                                    response.resourceRecordSets().stream().toList();

                            ResourceRecordSet upsertedResourceRecordSet =
                                    upsertResourceRecordSet(resourceRecordSets, server);

                            List<ResourceRecordSet> updatedResourceRecordSets =
                                    addOrUpdateResourceRecordSet(
                                            resourceRecordSets,
                                            upsertedResourceRecordSet
                                    ).filter(AwsR53Service::isARecord)
                                            .toList();

                            Stream<Change> changes = updatedResourceRecordSets.stream()
                                    .map(AwsR53Service::toChange);

                            return ChangeResourceRecordSetsRequest.builder()
                                    .hostedZoneId(properties.hostedZoneId())
                                    .changeBatch(
                                            ChangeBatch.builder()
                                                    .changes(changes.toList())
                                                    .build())
                                    .build();
                        }
                ).join();

        changeResourceRecordSets(request);
        
        return ListResourceRecordSetsResponse.builder()
                .resourceRecordSets(toResourceRecordSets(request))
                .build();
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
        route53AsyncClient.changeResourceRecordSets(request)
                .exceptionally(throwable -> {
                    throw new R53AddRecordException("There was an issue adding record to R53: " + throwable.getMessage());
                })
                .thenAccept(response -> {
                    log.info("Successful change request: {}", request.toString());
                })
                .join();
    }

    private static boolean isARecord(ResourceRecordSet resourceRecordSet){
        return resourceRecordSet.type().equals(RRType.A);
    }

    private GetHostedZoneResponse getHostedZoneResponse(){
        return route53AsyncClient.getHostedZone(GetHostedZoneRequest.builder()
                .id(properties.hostedZoneId())
                .build()).join();
    }

    private ResourceRecordSet upsertResourceRecordSet(List<ResourceRecordSet> resourceRecordSets, Server server){
        String hostedZoneName = getHostedZoneResponse().hostedZone().name();
        return resourceRecordSets.stream()
                .filter(recordSet -> recordSet.name().equals(server.getResourceRecordSetName(hostedZoneName)))
                .findFirst()
                .map(it -> appendResourceRecordToResourceRecordSet(server, it))
                .orElseGet(() -> createNewResourceRecordSet(server, hostedZoneName));
    }

    private static ResourceRecordSet appendResourceRecordToResourceRecordSet(Server server, ResourceRecordSet it) {
        List<ResourceRecord> resourceRecords = new ArrayList<>(it.resourceRecords());
        resourceRecords.add(server.toResourceRecord());
        return it.toBuilder().resourceRecords(resourceRecords).build();
    }

    private ResourceRecordSet createNewResourceRecordSet(Server server, String hostedZoneName) {
        return ResourceRecordSet.builder()
                .name(server.getResourceRecordSetName(hostedZoneName))
                .type(RRType.A)
                .ttl(properties.ttl())
                .weight(properties.weight())
                .setIdentifier(server.clusterSubdomain())
                .resourceRecords(
                        ResourceRecord.builder().value(server.ipAddress()).build()
                ).build();
    }

    public static Stream<ResourceRecordSet> addOrUpdateResourceRecordSet(
            List<ResourceRecordSet> resourceRecordSets,
            ResourceRecordSet resourceRecordSet
    ) {
        return Stream.concat(resourceRecordSets.stream(), Stream.of(resourceRecordSet));
    }
}
