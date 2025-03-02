package org.example.dnsservice.util;

import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Server;
import software.amazon.awssdk.services.route53.model.*;

import java.util.*;
import java.util.stream.Stream;

public class TestUtil {
    // r53 config
    public static final String CHANGE_ID = "changeId";
    public static final String HOSTED_ZONE_ID = "someHostedZoneId";
    public static final String DOT_DOMAIN_COM = ".domain.com.";
    public static final String DOT_DOMAIN_DOT_COM = ".domain.com";
    public static final String DOMAIN_DOT_COM_DOT = "domain.com.";
    public static final Long TTL = 10L;
    public static final Long WEIGHT = 10L;
    // domains
    public static final String GERMANY = "germany";
    public static final String SWITZERLAND = "switzerland";
    public static final String USA = "usa";
    public static final String ABC = "abc";
    // subdomains
    public static final String GENEVA = "ge";
    public static final String FRANKFURT = "fra";
    public static final String HONG_KONG = "hongkong";
    public static final String LA = "la";
    public static final String NYC = "nyc";
    public static final String XYZ = "xyz";
    // ip addresses
    public static String LA_IP_1 = "123.123.123.123";
    public static String LA_IP_2 = "125.125.125.125";
    public static String HONG_KONG_IP_1 = "234.234.234.234";
    public static String HONG_KONG_IP_2 = "235.235.235.235";
    public static String FRANKFURT_IP = "12.12.12.12";
    public static String NYC_IP = "13.13.13.13";
    public static String XYZ_IP = "5.5.5.5";
    public static String GENEVA_IP = "1.1.1.1";
    // cluster names
    public static final String LA_CLUSTER_NAME = "Los Angeles";
    public static final String NYC_CLUSTER_NAME = "New York";
    public static final String FRANKFURT_CLUSTER_NAME = "Frankfurt";
    public static final String HONG_KONG_CLUSTER_NAME = "Hong Kong";
    public static final String GENEVA_CLUSTER_NAME = "Geneva";
    // friendly names
    public static final String LA_FRIENDLY_NAME_1 = "ubiq-1";
    public static final String LA_FRIENDLY_NAME_2 = "ubiq-2";
    public static final String FRANKFURT_FRIENDLY_NAME = "leaseweb-de-1";
    public static final String HONG_KONG_FRIENDLY_NAME_1 = "rackspace-1";
    public static final String HONG_KONG_FRIENDLY_NAME_2 = "rackspace-2";
    public static final String NYC_FRIENDLY_NAME = "nyc-server-1";
    public static final String GENEVA_FRIENDLY_NAME = "geneva-friendly-name";

    public static class ResourceRecordSetTestData {

        public static ListResourceRecordSetsResponse createListResourceRecordSetsResponse(List<ResourceRecordSet> resourceRecordSets){
            return ListResourceRecordSetsResponse.builder()
                    .resourceRecordSets(getSoaResourceRecordSet())
                    .resourceRecordSets(getNsResourceRecordSet())
                    .resourceRecordSets(resourceRecordSets)
                    .build();
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

        public static ResourceRecordSet getGenevaAResourceRecordSet(){
            return createAResourceRecordSet(
                    SWITZERLAND + DOT_DOMAIN_COM,
                    GENEVA,
                    createIpResourceRecords(List.of(GENEVA_IP))
            );
        }

        public static ResourceRecordSet getLaAResourceRecordSet(){
            return createAResourceRecordSet(
                    USA + DOT_DOMAIN_COM,
                    LA,
                    createIpResourceRecords(List.of(LA_IP_1, LA_IP_2))
            );
        }

        public static ResourceRecordSet getHongKongAResourceRecordSet(){
            return createAResourceRecordSet(
                    HONG_KONG + DOT_DOMAIN_COM,
                    HONG_KONG,
                    createIpResourceRecords(List.of(HONG_KONG_IP_1, HONG_KONG_IP_2))
            );
        }

        public static ResourceRecordSet getFrankfurtAResourceRecordSet(){
            return createAResourceRecordSet(
                    GERMANY + DOT_DOMAIN_COM,
                    FRANKFURT,
                    createIpResourceRecords(List.of(FRANKFURT_IP))
            );
        }

        public static ResourceRecordSet getNycAResourceRecordSet(){
            return createAResourceRecordSet(
                    USA + DOT_DOMAIN_COM,
                    NYC,
                    createIpResourceRecords(List.of(NYC_IP))
            );
        }

        public static ResourceRecordSet getXyzAResourceRecordSet(){
            return createAResourceRecordSet(
                    ABC + DOT_DOMAIN_COM,
                    XYZ,
                    createIpResourceRecords(List.of(XYZ_IP))
            );
        }

        public static List<ResourceRecord> createIpResourceRecords(List<String> ipAddresses) {
            return ipAddresses.stream().map(ResourceRecordSetTestData::createResourceRecord).toList();
        }

        public static ResourceRecordSet createAResourceRecordSet(
                String name,
                String setIdentifier,
                List<ResourceRecord> resourceRecords
        ) {
            return ResourceRecordSet.builder()
                    .name(name)
                    .setIdentifier(setIdentifier)
                    .type(RRType.A)
                    .resourceRecords(resourceRecords)
                        .ttl(TTL)
                    .weight(WEIGHT)
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

        public static List<ResourceRecord> createResourceRecords(List<String> values){
            return values.stream().map(ResourceRecordSetTestData::createResourceRecord).toList();
        }

        public static long generateRandomTTL() {
            var random = new Random();
            return (long) (random.nextDouble() * (172800L));
        }

        public static List<ResourceRecordSet> getResourceRecordSets(ResourceRecordSet... resourceRecordSet) {
            return Stream.concat(
                    getNsAndSoaResourceRecordSets().stream(),
                    Arrays.stream(resourceRecordSet)
            ).toList();
        }

        public static List<ResourceRecordSet> getNsAndSoaResourceRecordSets() {
            return List.of(getNsResourceRecordSet(), getSoaResourceRecordSet());
        }

        public static ChangeResourceRecordSetsRequest getChangeResourceRecordSetsRequest(
                ChangeAction action,
                ResourceRecordSet resourceRecordSet
        ){
            return ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(HOSTED_ZONE_ID)
                    .changeBatch(
                            ChangeBatch.builder()
                                    .changes(
                                            Stream.of(resourceRecordSet)
                                                    .map(it ->
                                                            Change.builder()
                                                                    .action(action)
                                                                    .resourceRecordSet(it)
                                                                    .build()
                                                    )
                                                    .toList()
                                    )
                                    .build())
                    .build();
        }

        public static GetHostedZoneRequest getGetHostedZoneRequest(){
            return GetHostedZoneRequest.builder()
                    .id(HOSTED_ZONE_ID)
                    .build();
        }

        public static GetHostedZoneResponse getGetHostedZoneResponse(){
            return GetHostedZoneResponse.builder().hostedZone(
                    HostedZone.builder().name(DOMAIN_DOT_COM_DOT).build()
            ).build();
        }

        public static ListResourceRecordSetsRequest getListResourceRecordSetsRequest(){
            return ListResourceRecordSetsRequest.builder()
                    .hostedZoneId(HOSTED_ZONE_ID)
                    .build();
        }

        public static ChangeResourceRecordSetsResponse getChangeResourceRecordSetsResponse(){
            return ChangeResourceRecordSetsResponse.builder()
                    .changeInfo(
                            ChangeInfo.builder().id(CHANGE_ID).build()
                    )
                    .build();
        }
    }

    public static class ServerTestData {

        public static final Server GENEVA_SERVER = new ServerBuilder()
                .id(20)
                .clusterId(5)
                .regionSubdomain(SWITZERLAND)
                .clusterSubdomain(GENEVA)
                .clusterName(GENEVA_CLUSTER_NAME)
                .ipAddress(GENEVA_IP)
                .friendlyName(GENEVA_FRIENDLY_NAME)
                .build();
        
        public static final Server NYC_SERVER = new ServerBuilder()
                .id(6)
                .clusterId(2)
                .regionSubdomain(USA)
                .clusterSubdomain(NYC)
                .clusterName(NYC_CLUSTER_NAME)
                .ipAddress(NYC_IP)
                .friendlyName(NYC_FRIENDLY_NAME)
                .build();

        public static final Server LA_SERVER_1 = new ServerBuilder()
                .id(1)
                .clusterId(1)
                .regionSubdomain(USA)
                .clusterSubdomain(LA)
                .clusterName(LA_CLUSTER_NAME)
                .ipAddress(LA_IP_1)
                .friendlyName(LA_FRIENDLY_NAME_1)
                .build();

        public static final Server LA_SERVER_2 = new ServerBuilder()
                .id(2)
                .clusterId(1)
                .regionSubdomain(USA)
                .clusterSubdomain(LA)
                .clusterName(LA_CLUSTER_NAME)
                .ipAddress(LA_IP_2)
                .friendlyName(LA_FRIENDLY_NAME_2)
                .build();

        public static final Server FRANKFURT_SERVER = new ServerBuilder()
                .id(3)
                .clusterId(3)
                .regionSubdomain(GERMANY)
                .clusterSubdomain(FRANKFURT)
                .clusterName(FRANKFURT_CLUSTER_NAME)
                .ipAddress(FRANKFURT_IP)
                .friendlyName(FRANKFURT_FRIENDLY_NAME)
                .build();

        public static final Server HONG_KONG_SERVER_1 = new ServerBuilder()
                .id(4)
                .clusterId(4)
                .regionSubdomain(HONG_KONG)
                .clusterSubdomain(HONG_KONG)
                .clusterName(HONG_KONG_CLUSTER_NAME)
                .ipAddress(HONG_KONG_IP_1)
                .friendlyName(HONG_KONG_FRIENDLY_NAME_1)
                .build();

        public static final Server HONG_KONG_SERVER_2 = new ServerBuilder()
                .id(5)
                .clusterId(4)
                .regionSubdomain(HONG_KONG)
                .clusterSubdomain(HONG_KONG)
                .clusterName(HONG_KONG_CLUSTER_NAME)
                .ipAddress(HONG_KONG_IP_2)
                .friendlyName(HONG_KONG_FRIENDLY_NAME_2)
                .build();
    }
    
    public static class ARecordTestData {

        public static final ARecord GENEVA_A_RECORD = new ARecordBuilder()
                .name(SWITZERLAND + DOT_DOMAIN_COM)
                .setIdentifier(GENEVA)
                .ipAddress(GENEVA_IP)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();

        public static final ARecord NYC_A_RECORD = new ARecordBuilder()
                .name(USA + DOT_DOMAIN_COM)
                .setIdentifier(NYC)
                .ipAddress(NYC_IP)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();

        public static final ARecord LA_A_RECORD_1 = new ARecordBuilder()
                .name(USA + DOT_DOMAIN_COM)
                .setIdentifier(LA)
                .ipAddress(LA_IP_1)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();

        public static final ARecord LA_A_RECORD_2 = new ARecordBuilder()
                .name(USA + DOT_DOMAIN_COM)
                .setIdentifier(LA)
                .ipAddress(LA_IP_2)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();

        public static final ARecord FRANKFURT_A_RECORD = new ARecordBuilder()
                .name(GERMANY + DOT_DOMAIN_COM)
                .setIdentifier(FRANKFURT)
                .ipAddress(FRANKFURT_IP)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();

        public static final ARecord HONG_KONG_A_RECORD_1 = new ARecordBuilder()
                .name(HONG_KONG + DOT_DOMAIN_COM)
                .setIdentifier(HONG_KONG)
                .ipAddress(HONG_KONG_IP_1)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();

        public static final ARecord HONG_KONG_A_RECORD_2 = new ARecordBuilder()
                .name(HONG_KONG + DOT_DOMAIN_COM)
                .setIdentifier(HONG_KONG)
                .ipAddress(HONG_KONG_IP_2)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();

        public static final ARecord XYZ_A_RECORD = new ARecordBuilder()
                .name(ABC + DOT_DOMAIN_COM)
                .setIdentifier(XYZ)
                .ipAddress(XYZ_IP)
                .weight(WEIGHT)
                .ttl(TTL)
                .build();
    }

    public static class EntityTestData {
        // la
        public static final ClusterEntity LA_CLUSTER_ENTITY = new ClusterEntity(1,LA_CLUSTER_NAME, LA);
        public static final ServerEntity LA_SERVER_ENTITY_1 = new ServerEntity(1,LA_FRIENDLY_NAME_1, LA_IP_1, LA_CLUSTER_ENTITY);
        public static final ServerEntity LA_SERVER_ENTITY_2 = new ServerEntity(2,LA_FRIENDLY_NAME_2, LA_IP_2, LA_CLUSTER_ENTITY);

        //geneva
        public static final ClusterEntity GENEVA_CLUSTER_ENTITY = new ClusterEntity(5,GENEVA_CLUSTER_NAME, GENEVA);
        public static final ServerEntity GENEVA_SERVER_ENTITY = new ServerEntity(20,GENEVA_FRIENDLY_NAME, GENEVA_IP, GENEVA_CLUSTER_ENTITY);

        // nyc
        public static final ClusterEntity NYC_CLUSTER_ENTITY = new ClusterEntity(6,NYC_CLUSTER_NAME, NYC);
        public static final ServerEntity NYC_SERVER_ENTITY = new ServerEntity(7,NYC_FRIENDLY_NAME, NYC_IP, NYC_CLUSTER_ENTITY);
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

    private static class ARecordBuilder {
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

    public static String getRandomIp(){
        var r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }
}
