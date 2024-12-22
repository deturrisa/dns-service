package org.example.dnsservice.util;

import software.amazon.awssdk.services.route53.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.example.dnsservice.model.SupportedCountryCode.*;

public class TestUtil {

    public static class TestData{

        public static final String DOMAIN_COM = "domain.com.";

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
                    createResourceRecordSet(DOMAIN_COM, RRType.NS,
                            Arrays.asList(
                                    createResourceRecord("ns-1173.awsdns-31.org."),
                                    createResourceRecord("ns-428.awsdns-11.com.")
                            )),
                    createResourceRecordSet(DOMAIN_COM, RRType.SOA,
                            Collections.singletonList(
                                    createResourceRecord("ns-1243.awsdns-11.org. awsdns-hostmaster.amazon.com. 1 7200 900 1209600 86400")
                            ))
            );
        }

        public static List<ResourceRecordSet> getResourceRecordSets(){
            return Arrays.asList(
                    createResourceRecordSet(DOMAIN_COM, RRType.NS,
                            List.of(
                                    createResourceRecord("ns-1173.awsdns-31.org."),
                                    createResourceRecord("ns-428.awsdns-11.com.")
                            )),
                    createResourceRecordSet(DOMAIN_COM, RRType.SOA,
                            List.of(
                                    createResourceRecord("ns-1243.awsdns-11.org. awsdns-hostmaster.amazon.com. 1 7200 900 1209600 86400")
                            )),
                    createAResourceRecordSet(
                            "fra." + DOMAIN_COM,
                            "fra",
                            createIpResourceRecords(
                                    List.of("12.12.12.12")
                            ),
                            GeoLocation.builder().countryCode(DE.name()).build()
                    ),
                    createAResourceRecordSet(
                            "ge." + DOMAIN_COM,
                            "ge",
                            createIpResourceRecords(
                                    List.of("1.2.3.4")
                            ),
                            GeoLocation.builder().countryCode(CH.name()).build()
                    ),
                    createAResourceRecordSet(
                            "hongkong." + DOMAIN_COM,
                            "hongkong",
                            createIpResourceRecords(
                                    List.of("234.234.234.234","235.235.235.235")
                            ),
                            GeoLocation.builder().countryCode(HK.name()).build()
                    ),
                    createAResourceRecordSet(
                            "la." + DOMAIN_COM,
                            "la",
                            createIpResourceRecords(
                                    List.of("123.123.123.123","125.125.125.125")
                            ),
                            GeoLocation.builder().countryCode(US.name()).build()
                    ),
                    createAResourceRecordSet(
                            "nyc." + DOMAIN_COM,
                            "nyc",
                            createIpResourceRecords(
                                    List.of("13.13.13.13")
                            ),
                            GeoLocation.builder().countryCode(US.name()).build()
                    ),
                    createAResourceRecordSet(
                            "xyz." + DOMAIN_COM,
                            "xyz",
                            createIpResourceRecords(
                                    List.of("5.5.5.5")
                            ),
                            GeoLocation.builder().countryCode(US.name()).build()
                    )
            );
        }

        private static List<ResourceRecord> createIpResourceRecords(List<String> ipAddresses) {
            return ipAddresses.stream().map(TestData::createResourceRecord).toList();
        }

        private static ResourceRecordSet createAResourceRecordSet(
                String name,
                String setIdentifier,
                List<ResourceRecord> ipResourceRecords,
                GeoLocation geoLocation) {
            return ResourceRecordSet.builder()
                    .name(name)
                    .setIdentifier(setIdentifier)
                    .type(RRType.A)
                    .geoLocation(geoLocation)
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
