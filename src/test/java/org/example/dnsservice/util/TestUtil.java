package org.example.dnsservice.util;

import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import java.util.Arrays;

public class TestUtil {

    public static class TestData{
        public static ListResourceRecordSetsResponse DEFAULT_RESOURCE_RECORD_SETS_RESPONSE =
                ListResourceRecordSetsResponse.builder()
                        .resourceRecordSets(
                                Arrays.asList(
                                        ResourceRecordSet.builder()
                                                .name("domain.com.")
                                                .type(RRType.NS)
                                                .ttl(172800L)
                                                .resourceRecords(
                                                        Arrays.asList(
                                                                ResourceRecord.builder()
                                                                        .value("ns-1173.awsdns-31.org.")
                                                                        .build(),
                                                                ResourceRecord.builder()
                                                                        .value("ns-428.awsdns-11.com.")
                                                                        .build()
                                                        )
                                                )
                                                .build(),
                                        ResourceRecordSet.builder()
                                                .name("domain.com.")
                                                .type(RRType.SOA)
                                                .ttl(900L)
                                                .resourceRecords(Arrays.asList(ResourceRecord.builder()
                                                        .value("ns-1243.awsdns-11.org. awsdns-hostmaster.amazon.com. 1 7200 900 1209600 86400")
                                                        .build()))
                                                .build()
                                )
                        )
                        .build();
    }

}
