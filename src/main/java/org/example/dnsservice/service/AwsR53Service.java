package org.example.dnsservice.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.exception.R53AddRecordException;
import org.example.dnsservice.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.GetHostedZoneRequest;
import software.amazon.awssdk.services.route53.model.GetHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

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

  public CompletableFuture<ListResourceRecordSetsResponse> getResourceRecordSets() {
    return getListResourceRecordSetsResponse();
  }

  public void addResourceRecordByServer(Server server) {
    var request =
        getListResourceRecordSetsResponse()
            .thenApply(
                response ->
                    ChangeResourceRecordSetsRequest.builder()
                        .hostedZoneId(properties.hostedZoneId())
                        .changeBatch(
                            ChangeBatch.builder()
                                .changes(
                                    response.resourceRecordSets().stream()
                                        .filter(it -> isSetIdentifierMatch(server, it))
                                        .findFirst()
                                        .map(it -> toCreateResourceRecordChange(server, it))
                                        .orElseGet(() -> toCreateResourceRecordSetChange(server)))
                                .build())
                        .build())
            .join();

    changeResourceRecordSets(request);
  }

  public void removeResourceRecordByServer(Server server) {
    var request =
        getListResourceRecordSetsResponse()
            .thenApply(
                response ->
                    ChangeResourceRecordSetsRequest.builder()
                        .hostedZoneId(properties.hostedZoneId())
                        .changeBatch(
                            ChangeBatch.builder()
                                .changes(
                                    response.resourceRecordSets().stream()
                                        .filter(it -> isSetIdentifierMatch(server, it))
                                        .map(
                                            recordSet -> {
                                              if (isLastResourceRecord(recordSet)) {
                                                return toDeleteResourceRecordSetChange(recordSet);
                                              } else {
                                                return toRemoveResourceRecordChange(
                                                    recordSet, server);
                                              }
                                            })
                                        .toList())
                                .build())
                        .build())
            .join();

    changeResourceRecordSets(request);
  }

  private static boolean isLastResourceRecord(ResourceRecordSet recordSet) {
    return recordSet.resourceRecords().size() == 1;
  }

  private CompletableFuture<ListResourceRecordSetsResponse> getListResourceRecordSetsResponse() {
    return route53AsyncClient
        .listResourceRecordSets(generateListResourceRecordSetsRequest())
        .thenApply(
            response ->
                response.toBuilder()
                    .resourceRecordSets(
                        response.resourceRecordSets().stream()
                            .filter(it -> it.setIdentifier() != null)
                            .toList())
                    .build());
  }

  private ListResourceRecordSetsRequest generateListResourceRecordSetsRequest() {
    return ListResourceRecordSetsRequest.builder().hostedZoneId(properties.hostedZoneId()).build();
  }

  private CompletableFuture<ChangeResourceRecordSetsResponse> changeResourceRecordSets(
      ChangeResourceRecordSetsRequest request) {
    return route53AsyncClient
        .changeResourceRecordSets(request)
        .exceptionally(
            throwable -> {
              throw new R53AddRecordException(
                  "There was an issue adding record to R53: " + throwable.getMessage());
            })
        .thenApply(
            response -> {
              log.info("Successful change request: {}", request.toString());
              return response;
            });
  }

  private GetHostedZoneResponse getHostedZoneResponse() {
    return route53AsyncClient
        .getHostedZone(GetHostedZoneRequest.builder().id(properties.hostedZoneId()).build())
        .join();
  }

  private Change toCreateResourceRecordChange(Server server, ResourceRecordSet existingRecordSet) {
    return buildChange(
        ChangeAction.UPSERT,
        existingRecordSet.name(),
        addResourceRecord(server, existingRecordSet),
        server.clusterSubdomain());
  }

  private static List<String> addResourceRecord(
      Server server, ResourceRecordSet existingRecordSet) {
    return Stream.concat(
            existingRecordSet.resourceRecords().stream(), Stream.of(server.toResourceRecord()))
        .map(ResourceRecord::value)
        .toList();
  }

  private Change toCreateResourceRecordSetChange(Server server) {
    var hostedZoneName = getHostedZoneResponse().hostedZone().name();
    return buildChange(
        ChangeAction.UPSERT, // domain.com.
        server.getResourceRecordSetName(hostedZoneName),
        server.ipAddress(),
        server.clusterSubdomain());
  }

  private Change toDeleteResourceRecordSetChange(ResourceRecordSet recordSet) {
    return buildChange(
        ChangeAction.DELETE,
        recordSet.name(),
        recordSet.resourceRecords().stream().map(ResourceRecord::value).toList(),
        recordSet.setIdentifier());
  }

  private Change toRemoveResourceRecordChange(ResourceRecordSet recordSet, Server server) {
    return buildChange(
        recordSet.name(),
        removeResourceRecordFromResourceRecordSet(recordSet, server),
        recordSet.setIdentifier());
  }

  private static List<String> removeResourceRecordFromResourceRecordSet(
      ResourceRecordSet recordSet, Server server) {
    return recordSet.resourceRecords().stream()
        .map(ResourceRecord::value)
        .filter(value -> !value.equals(server.ipAddress()))
        .toList();
  }

  private Change buildChange(ChangeAction action, String name, String value, String setIdentifier) {
    return Change.builder()
        .action(action)
        .resourceRecordSet(
            ResourceRecordSet.builder()
                .name(name)
                .type(RRType.A)
                .ttl(properties.ttl())
                .setIdentifier(setIdentifier)
                .weight(properties.weight())
                .resourceRecords(ResourceRecord.builder().value(value).build())
                .build())
        .build();
  }

  private Change buildChange(String name, List<String> values, String setIdentifier) {
    return Change.builder()
        .action(ChangeAction.UPSERT)
        .resourceRecordSet(
            ResourceRecordSet.builder()
                .name(name)
                .type(RRType.A)
                .ttl(properties.ttl())
                .setIdentifier(setIdentifier)
                .weight(properties.weight())
                .resourceRecords(
                    values.stream().map(it -> ResourceRecord.builder().value(it).build()).toList())
                .build())
        .build();
  }

  private Change buildChange(
      ChangeAction action, String name, List<String> values, String setIdentifier) {
    return Change.builder()
        .action(action)
        .resourceRecordSet(
            ResourceRecordSet.builder()
                .name(name)
                .type(RRType.A)
                .ttl(properties.ttl())
                .setIdentifier(setIdentifier)
                .weight(properties.weight())
                .resourceRecords(
                    values.stream().map(it -> ResourceRecord.builder().value(it).build()).toList())
                .build())
        .build();
  }

  private static boolean isSetIdentifierMatch(Server server, ResourceRecordSet it) {
    return it.setIdentifier() != null && it.setIdentifier().equals(server.clusterSubdomain());
  }
}
