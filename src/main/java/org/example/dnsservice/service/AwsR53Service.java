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
import software.amazon.awssdk.services.route53.model.ChangeBatch;
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
                        response -> ChangeResourceRecordSetsRequest.builder()
                                .hostedZoneId(properties.hostedZoneId())
                                .changeBatch(
                                        ChangeBatch.builder()
                                                .changes(response.resourceRecordSets().stream()
                                                        .filter(it -> it.setIdentifier().equals(server.clusterSubdomain()))
                                                        .findFirst()
                                                                .map(it -> toCreateResourceRecordChange(server,it))
                                                                .orElseGet(() -> toCreateResourceRecordSetChange(server)
                                                        )
                                                )
                                                .build())
                                .build()
                ).join();

        changeResourceRecordSets(request);
        
        return ListResourceRecordSetsResponse.builder()
                .resourceRecordSets(toResourceRecordSets(request))
                .build();
    }

    public ListResourceRecordSetsResponse removeResourceRecordByServer(
            Server server
    ) {
        ChangeResourceRecordSetsRequest request =
                getListResourceRecordSetsResponse().thenApply(
                        response -> ChangeResourceRecordSetsRequest.builder()
                                .hostedZoneId(properties.hostedZoneId())
                                .changeBatch(
                                        ChangeBatch.builder()
                                                .changes(response.resourceRecordSets().stream()
                                                        .filter(it -> it.setIdentifier().equals(server.clusterSubdomain()))
                                                        .map(recordSet ->
                                                                {
                                                                    if(isLastResourceRecord(recordSet)){
                                                                        return toDeleteResourceRecordSetChange(recordSet);
                                                                    }else {
                                                                        return toRemoveResourceRecordChange(recordSet, server);
                                                                    }
                                                                }
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

    private static ResourceRecordSet addResourceRecord(ResourceRecordSet resourceRecordSet, Server server) {
        return resourceRecordSet.toBuilder().resourceRecords(
                Stream.concat(resourceRecordSet.resourceRecords().stream(),
                        Stream.of(server.toResourceRecord())
                ).toList()
        ).build();
    }

    private static boolean isLastResourceRecord(ResourceRecordSet recordSet) {
        return recordSet.resourceRecords().size() == 1;
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

    private Change toCreateResourceRecordChange(Server server, ResourceRecordSet existingRecordSet) {
        return buildChange(
                ChangeAction.UPSERT,
                existingRecordSet.name(),
                addResourceRecord(server, existingRecordSet),
                server.clusterSubdomain()
        );
    }

    private static List<String> addResourceRecord(Server server, ResourceRecordSet existingRecordSet) {
        return Stream.concat(existingRecordSet.resourceRecords().stream(),
                Stream.of(server.toResourceRecord())
        ).map(ResourceRecord::value).toList();
    }

    //add endpoint
    //when does not exist in r53
    private Change toCreateResourceRecordSetChange(Server server) {
        String hostedZoneName = getHostedZoneResponse().hostedZone().name();
        return buildChange(
                ChangeAction.UPSERT,
                server.getResourceRecordSetName(hostedZoneName),
                server.ipAddress(),
                server.clusterSubdomain()
        );
    }

    private Change toDeleteResourceRecordSetChange(ResourceRecordSet recordSet) {
        return buildChange(
                ChangeAction.DELETE,
                recordSet.name(),
                recordSet.resourceRecords().stream().map(ResourceRecord::value).toList(),
                recordSet.setIdentifier()
        );
    }

    private Change toRemoveResourceRecordChange(ResourceRecordSet recordSet, Server server) {
        return buildChange(
                recordSet.name(),
                removeResourceRecordFromResourceRecordSet(recordSet, server),
                recordSet.setIdentifier()
        );
    }

    private static List<String> removeResourceRecordFromResourceRecordSet(ResourceRecordSet recordSet, Server server) {
        return recordSet.resourceRecords().stream().map(ResourceRecord::value).filter(value -> !value.equals(server.ipAddress())).toList();
    }

    private Change buildChange(ChangeAction action, String name, String value, String setIdentifier) {
        return Change.builder()
                .action(action)
                .resourceRecordSet(ResourceRecordSet.builder()
                        .name(name)
                        .type(RRType.A)
                        .ttl(properties.ttl())
                        .setIdentifier(setIdentifier)
                        .weight(properties.weight())
                        .resourceRecords(
                                ResourceRecord.builder()
                                        .value(value)
                                        .build()
                        )
                        .build())
                .build();
    }

    private Change buildChange(String name, List<String> values, String setIdentifier) {
        return Change.builder()
                .action(ChangeAction.UPSERT)
                .resourceRecordSet(ResourceRecordSet.builder()
                        .name(name)
                        .type(RRType.A)
                        .ttl(properties.ttl())
                        .setIdentifier(setIdentifier)
                        .weight(properties.weight())
                        .resourceRecords(
                                values.stream().map(it ->
                                                ResourceRecord.builder()
                                                        .value(it)
                                                        .build()
                                        ).toList()
                        )
                        .build())
                .build();
    }

    private Change buildChange(ChangeAction action, String name, List<String> values, String setIdentifier) {
        return Change.builder()
                .action(action)
                .resourceRecordSet(ResourceRecordSet.builder()
                        .name(name)
                        .type(RRType.A)
                        .ttl(properties.ttl())
                        .setIdentifier(setIdentifier)
                        .weight(properties.weight())
                        .resourceRecords(
                                values.stream().map(it ->
                                                ResourceRecord.builder()
                                                        .value(it)
                                                        .build()
                                        ).toList()
                        )
                        .build())
                .build();
    }
}
