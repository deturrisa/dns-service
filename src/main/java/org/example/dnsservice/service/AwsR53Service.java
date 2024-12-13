package org.example.dnsservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
public class AwsR53Service {

    private final Route53AsyncClient route53AsyncClient;

    @Autowired
    public AwsR53Service(Route53AsyncClient route53AsyncClient) {
        this.route53AsyncClient = route53AsyncClient;
    }

    public void upsertResourceRecordSets(String hostedZoneId, String fullDomain, String[] ipAddresses){

        ResourceRecord[] resourceRecords = getIpAddressResourceRecords(ipAddresses);

        ResourceRecordSet resourceRecordSet = getARecordResourceRecordSet(fullDomain, resourceRecords);

        ChangeResourceRecordSetsRequest request = getUpsertChangeResourceRecordSetsRequest(hostedZoneId, resourceRecordSet);

        route53AsyncClient.changeResourceRecordSets(request);
    }

    public CompletableFuture<ListResourceRecordSetsResponse> getResourceRecordSets(String hostedZoneId){
        return route53AsyncClient.listResourceRecordSets(generateListResourceRecordSetsRequest(hostedZoneId));
    }

    private ResourceRecordSet getARecordResourceRecordSet(String fullDomain, ResourceRecord[] resourceRecords){
        Long ttl = 300L;
        return ResourceRecordSet.builder()
                .name(fullDomain)
                .type(RRType.A)
                .ttl(ttl)
                .resourceRecords(resourceRecords)
                .build();
    }

    private ResourceRecord[] getIpAddressResourceRecords(String[] ipAddresses){
        return Arrays.stream(ipAddresses)
                .map(ip -> ResourceRecord.builder().value(ip).build())
                .toArray(ResourceRecord[]::new);
    }

    private ChangeResourceRecordSetsRequest getUpsertChangeResourceRecordSetsRequest(String hostedZoneId, ResourceRecordSet resourceRecordSet){
        Change change = Change.builder()
                .action(ChangeAction.UPSERT)
                .resourceRecordSet(resourceRecordSet)
                .build();

        ChangeBatch changeBatch = ChangeBatch.builder()
                .changes(change)
                .build();

        return ChangeResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId)
                .changeBatch(changeBatch)
                .build();
    }

    private ListResourceRecordSetsRequest generateListResourceRecordSetsRequest(String hostedZoneId) {
        return ListResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId).build();
    }
}
