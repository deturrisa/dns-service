package org.example.dnsservice.util;

import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Server;
import software.amazon.awssdk.services.route53.model.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TestUtil {

    public static class ResourceRecordSetTestData {

        public static final String HOSTED_ZONE_ID = "someHostedZoneId";
        public static final String DOT_DOMAIN_COM = ".domain.com.";
        public static final String USA = "usa";
        public static final String NYC = "nyc";
        public static final String LA = "la";
        public static final String SWITZERLAND = "switzerland";
        public static final String GENEVA = "ge";
        public static final String FRANKFURT = "fra";
        public static final String HONG_KONG = "hongkong";
        public static final String GERMANY = "germany";

        public static ListResourceRecordSetsResponse createListResourceRecordSetsResponse(List<ResourceRecordSet> resourceRecordSets){
            return ListResourceRecordSetsResponse.builder()
                    .resourceRecordSets(createDefaultResourceRecordSets())
                    .resourceRecordSets(resourceRecordSets)
                    .build();
        }

        private static List<ResourceRecordSet> createDefaultResourceRecordSets() {
            return Arrays.asList(
                    getNsResourceRecordSet(),
                    getSoaResourceRecordSet()
            );
        }

        public static ResourceRecordSet getSoaResourceRecordSet() {
            return createResourceRecordSet(DOT_DOMAIN_COM, RRType.SOA,
                    Collections.singletonList(
                            createResourceRecord("ns-1243.awsdns-11.org. awsdns-hostmaster.amazon.com. 1 7200 900 1209600 86400")
                    ));
        }

        public static ResourceRecordSet getNsResourceRecordSet() {
            return createResourceRecordSet(DOT_DOMAIN_COM, RRType.NS,
                    Arrays.asList(
                            createResourceRecord("ns-1173.awsdns-31.org."),
                            createResourceRecord("ns-428.awsdns-11.com.")
                    ));
        }

        public static ResourceRecordSet getUsaAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    USA + DOT_DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        public static ResourceRecordSet getUsaAResourceRecordSet(
                String setIdentifier,
                List<String> ips,
                Long ttl,
                Long weight
        ){
            return createAResourceRecordSet(
                    USA + DOT_DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips),
                    ttl,
                    weight
            );
        }

        public static ResourceRecordSet getSwitzerlandAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    SWITZERLAND + DOT_DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        public static ResourceRecordSet getHongKongAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    HONG_KONG + DOT_DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        public static ResourceRecordSet getHongKongAResourceRecordSet(
                String setIdentifier,
                List<String> ips,
                Long ttl,
                Long weight
        ){
            return createAResourceRecordSet(
                    HONG_KONG + DOT_DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips),
                    ttl,
                    weight
            );
        }

        public static ResourceRecordSet getGermanyAResourceRecordSet(String setIdentifier, List<String> ips){
            return createAResourceRecordSet(
                    GERMANY + DOT_DOMAIN_COM,
                    setIdentifier,
                    createIpResourceRecords(ips)
            );
        }

        public static List<ResourceRecord> createIpResourceRecords(List<String> ipAddresses) {
            return ipAddresses.stream().map(ResourceRecordSetTestData::createResourceRecord).toList();
        }

        public static ResourceRecordSet createAResourceRecordSet(
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

        public static ResourceRecordSet createAResourceRecordSet(
                String name,
                String setIdentifier,
                List<ResourceRecord> resourceRecords,
                Long ttl,
                Long weight
        ) {
            return ResourceRecordSet.builder()
                    .name(name)
                    .setIdentifier(setIdentifier)
                    .type(RRType.A)
                    .resourceRecords(resourceRecords)
                    .ttl(ttl)
                    .weight(weight)
                    .build();
        }

        public static ChangeResourceRecordSetsRequest getDeleteChangeResourceRecordSetsRequest(
                List<ResourceRecordSet> resourceRecordSets
        ) {
            return ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(HOSTED_ZONE_ID)
                    .changeBatch(
                            ChangeBatch.builder()
                                    .changes(resourceRecordSets.stream()
                                            .map(ResourceRecordSetTestData::getDeleteChange)
                                            .collect(Collectors.toList()))
                                    .build())
                    .build();
        }

        public static Change getDeleteChange(ResourceRecordSet resourceRecordSet) {
            return Change.builder()
                    .action(ChangeAction.DELETE)
                    .resourceRecordSet(resourceRecordSet)
                    .build();
        }

        public static ChangeResourceRecordSetsRequest getUpsertChangeResourceRecordSetsRequest(
                List<ResourceRecordSet> resourceRecordSets
        ) {
            return ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(HOSTED_ZONE_ID)
                    .changeBatch(
                            ChangeBatch.builder()
                                    .changes(resourceRecordSets.stream()
                                            .map(ResourceRecordSetTestData::getUpsertChange)
                                            .collect(Collectors.toList()))
                                    .build())
                    .build();
        }

        public static Change getUpsertChange(ResourceRecordSet resourceRecordSet) {
            return Change.builder()
                    .action(ChangeAction.UPSERT)
                    .resourceRecordSet(resourceRecordSet)
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

    public static class ServerBuilder {

        private Integer id = 12;
        private Integer clusterId = 25;
        private String clusterName = "Some Cluster Name";
        private String regionSubdomain = "Some Cluster Region";
        private String clusterSubdomain = "Some Cluster Subdomain";
        private String friendlyName = "Some Friendly Name";
        private String ipAddress = getRandomIp();

        public ServerBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public ServerBuilder clusterId(Integer clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public ServerBuilder clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public ServerBuilder regionSubdomain(String regionSubdomain) {
            this.regionSubdomain = regionSubdomain;
            return this;
        }

        public ServerBuilder clusterSubdomain(String clusterSubdomain) {
            this.clusterSubdomain = clusterSubdomain;
            return this;
        }

        public ServerBuilder friendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
            return this;
        }

        public ServerBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Server build(){
            return new Server(id, clusterId, clusterName, regionSubdomain, clusterSubdomain, friendlyName, ipAddress);
        }
    }

    public static class ARecordBuilder {
        private String setIdentifier = "si";
        private String name = "region.domain.com.";
        private String ipAddress = getRandomIp();
        private Long weight = 50L;
        private Long ttl = 300L;

        public ARecordBuilder setIdentifier(String setIdentifier) {
            this.setIdentifier = setIdentifier;
            return this;
        }

        public ARecordBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ARecordBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public ARecordBuilder weight(Long weight) {
            this.weight = weight;
            return this;
        }

        public ARecordBuilder ttl(Long ttl) {
            this.ttl = ttl;
            return this;
        }

        public ARecord build(){
            return new ARecord(name, ipAddress, setIdentifier, weight, ttl);
        }
    }

    private static String getRandomIp(){
        Random r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }
}
