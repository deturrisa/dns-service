package org.example.dnsservice.util;

import software.amazon.awssdk.services.route53.model.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestUtil {

    public static class TestData{

        public static final String DOMAIN_COM = "domain.com.";

        public static ListResourceRecordSetsResponse createListResourceRecordSetsResponse(List<ResourceRecordSet> resourceRecordSets){
            return ListResourceRecordSetsResponse.builder()
                    .resourceRecordSets(createDefaultResourceRecordSets())
                    .resourceRecordSets(resourceRecordSets)
                    .build();
        }

        public static ListResourceRecordSetsResponse getDefaultResourceRecordSetsResponse() {
            return ListResourceRecordSetsResponse.builder()
                    .resourceRecordSets(createDefaultResourceRecordSets())
                    .build();
        }

        private static List<ResourceRecordSet> createDefaultResourceRecordSets() {
            return Arrays.asList(
                    getNsResourceRecordSet(),
                    getSoaResourceRecordSet()
            );
        }

        public static ResourceRecordSet getSoaResourceRecordSet() {
            return createResourceRecordSet(DOMAIN_COM, RRType.SOA,
                    Collections.singletonList(
                            createResourceRecord("ns-1243.awsdns-11.org. awsdns-hostmaster.amazon.com. 1 7200 900 1209600 86400")
                    ));
        }

        public static ResourceRecordSet getNsResourceRecordSet() {
            return createResourceRecordSet(DOMAIN_COM, RRType.NS,
                    Arrays.asList(
                            createResourceRecord("ns-1173.awsdns-31.org."),
                            createResourceRecord("ns-428.awsdns-11.com.")
                    ));
        }

        public static ResourceRecordSet getUsaAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    "usa." + DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        public static ResourceRecordSet getSwitzerlandAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    "switzerland." + DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        public static ResourceRecordSet getHongKongAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    "hongkong." + DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        public static ResourceRecordSet getGermanyAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    "germany." + DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        private static List<ResourceRecord> createIpResourceRecords(List<String> ipAddresses) {
            return ipAddresses.stream().map(TestData::createResourceRecord).toList();
        }

        private static ResourceRecordSet createAResourceRecordSet(
                String name,
                String setIdentifier,
                List<ResourceRecord> ipResourceRecords) {
            return ResourceRecordSet.builder()
                    .name(name)
                    .setIdentifier(setIdentifier)
                    .type(RRType.A)
                    .resourceRecords(ipResourceRecords)
                    .build();
        }

        private static ResourceRecordSet createResourceRecordSet(String name, RRType type, List<ResourceRecord> resourceRecords) {
            return ResourceRecordSet.builder()
                    .name(name)
                    .type(type)
                    .ttl(generateRandomTTL())
                    .resourceRecords(resourceRecords)
                    .build();
        }

        public static ResourceRecord createResourceRecord(String value) {
            return ResourceRecord.builder()
                    .value(value)
                    .build();
        }

        public static long generateRandomTTL() {
            Random random = new Random();
            return (long) (random.nextDouble() * (172800L));
        }

    }

}
