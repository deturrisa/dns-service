package org.example.dnsservice.util;

import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestUtil {

    public static class TestData{

        public static ListResourceRecordSetsResponse createListResourceRecordSetsResponse(List<ResourceRecordSet> resourceRecordSets){
            return ListResourceRecordSetsResponse.builder()
                    .resourceRecordSets(createDefaultResourceRecordSets())
                    .resourceRecordSets(resourceRecordSets)
                    .build();
        }

        public static List<ResourceRecordSet> createCNameResourceRecordSets(String domainName, List<ResourceRecord> resourceRecords) {
            return Collections.singletonList(
                    createResourceRecordSet(domainName, RRType.CNAME, resourceRecords)
            );
        }

        public static List<ResourceRecordSet> createANameResourceRecordSets(String address, List<ResourceRecord> resourceRecords) {
            return Collections.singletonList(
                    createResourceRecordSet(address, RRType.A, resourceRecords)
            );
        }

        public static ListResourceRecordSetsResponse getDefaultResourceRecordSetsResponse() {
            return ListResourceRecordSetsResponse.builder()
                    .resourceRecordSets(createDefaultResourceRecordSets())
                    .build();
        }

        private static List<ResourceRecordSet> createDefaultResourceRecordSets() {
            return Arrays.asList(
                    createResourceRecordSet("domain.com.", RRType.NS,
                            Arrays.asList(
                                    createResourceRecord("ns-1173.awsdns-31.org."),
                                    createResourceRecord("ns-428.awsdns-11.com.")
                            )),
                    createResourceRecordSet("domain.com.", RRType.SOA,
                            Collections.singletonList(
                                    createResourceRecord("ns-1243.awsdns-11.org. awsdns-hostmaster.amazon.com. 1 7200 900 1209600 86400")
                            ))
            );
        }

        public static List<ResourceRecordSet> getResourceRecordSets(){
            return Arrays.asList(
                    createResourceRecordSet("domain.com.", RRType.NS,
                            List.of(
                                    createResourceRecord("ns-1173.awsdns-31.org."),
                                    createResourceRecord("ns-428.awsdns-11.com.")
                            )),
                    createResourceRecordSet("domain.com.", RRType.SOA,
                            List.of(
                                    createResourceRecord("ns-1243.awsdns-11.org. awsdns-hostmaster.amazon.com. 1 7200 900 1209600 86400")
                            )),
                    createResourceRecordSet("usa.domain.com.", RRType.CNAME,
                            List.of(
                                    createResourceRecord("la.domain.com")
                            )),
                    createResourceRecordSet("usa.domain.com.", RRType.CNAME,
                            List.of(
                                    createResourceRecord("nyc.domain.com")
                            )),
                    createResourceRecordSet("hongkong.domain.com.", RRType.A,
                            List.of(
                                    createResourceRecord("1.2.3.4.5"),
                                    createResourceRecord("6.7.8.9.10")
                            ))                  ,
                    createResourceRecordSet("la.domain.com.", RRType.A,
                            List.of(
                                    createResourceRecord("123.123.123.123")
                            )),
                    createResourceRecordSet("la.domain.com.", RRType.A,
                            List.of(
                                    createResourceRecord("125.125.125.125")
                            )),
                    createResourceRecordSet("nyc.domain.com.", RRType.A,
                            List.of(
                                    createResourceRecord("111.111.111.111")
                            ))

            );
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
