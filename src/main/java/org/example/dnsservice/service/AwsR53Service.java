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

    public CompletableFuture<ListResourceRecordSetsResponse> getResourceRecordSets(String hostedZoneId){
        return route53AsyncClient.listResourceRecordSets(generateListResourceRecordSetsRequest(hostedZoneId));
    }
    private ListResourceRecordSetsRequest generateListResourceRecordSetsRequest(String hostedZoneId) {
        return ListResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId).build();
    }
}
