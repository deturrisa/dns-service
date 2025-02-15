package org.example.dnsservice.service;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.util.TestUtil;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@UnitTest
public class ARecordServiceTest {

    @Mock
    private AwsR53Service awsR53Service;

    @Mock
    private DomainRegionProperties domainRegionProperties;

    @InjectMocks
    private ARecordService service;

    @Test
    public void testShouldMapListRecordResponseToRoute53Records(){
        //given
        ResourceRecordSet ignoredNameResourceRecordSet = createAResourceRecordSet(
                "uk." + DOT_DOMAIN_COM,
                "ma",
                createIpResourceRecords(List.of("1.1.1.1"))
        );
        ResourceRecordSet ignoredSetIdentifierResourceRecordSet = getGermanyAResourceRecordSet(
                "ber",
                List.of("12.12.12.12")
        );
        ResourceRecordSet invalidDomainNameResourceRecordSet = createAResourceRecordSet(
                "abc.xyz" + DOT_DOMAIN_COM,
                "ma",
                createIpResourceRecords(List.of("1.1.1.1"))
        );

        List<ResourceRecordSet> resourceRecordSets = List.of(
                getNsResourceRecordSet(),
                getSoaResourceRecordSet(),
                getGermanyAResourceRecordSet(FRANKFURT,
                        List.of("12.12.12.12")
                ),
                getSwitzerlandAResourceRecordSet(GENEVA,
                        List.of("1.2.3.4")
                ),
                getHongKongAResourceRecordSet(HONG_KONG,
                        List.of("234.234.234.234", "235.235.235.235")
                ),
                getUsaAResourceRecordSet(LA,
                        List.of("123.123.123.123", "125.125.125.125")
                ),
                getUsaAResourceRecordSet(NYC,
                        List.of("13.13.13.13")
                ),
                ignoredNameResourceRecordSet,
                ignoredSetIdentifierResourceRecordSet,
                invalidDomainNameResourceRecordSet
        );

        ListResourceRecordSetsResponse response =
                createListResourceRecordSetsResponse(
                        resourceRecordSets
                );

        when(awsR53Service.getResourceRecordSets()).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        when(domainRegionProperties.getDomainRegions()).thenReturn(
                List.of(
                        new DomainRegion(USA, Set.of(LA, NYC)),
                        new DomainRegion(SWITZERLAND, Set.of(GENEVA)),
                        new DomainRegion(HONG_KONG, Set.of(HONG_KONG)),
                        new DomainRegion(GERMANY, Set.of(FRANKFURT))
                )
        );

        //when
        List<ARecord> result = service.getARecords();

        assertEquals(7, result.size());

        ARecord frankfurtRecord = result.get(0);
        assertEquals(FRANKFURT, frankfurtRecord.setIdentifier());
        assertEquals(GERMANY + DOT_DOMAIN_COM, frankfurtRecord.name());
        assertEquals("12.12.12.12", frankfurtRecord.ipAddress());

        ARecord genevaRecord = result.get(1);
        assertEquals(GENEVA, genevaRecord.setIdentifier());
        assertEquals(SWITZERLAND + DOT_DOMAIN_COM, genevaRecord.name());
        assertEquals("1.2.3.4", genevaRecord.ipAddress());

        ARecord hongKongRecord1 = result.get(2);
        assertEquals(HONG_KONG, hongKongRecord1.setIdentifier());
        assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongRecord1.name());
        assertEquals("234.234.234.234", hongKongRecord1.ipAddress());

        ARecord hongKongRecord2 = result.get(3);
        assertEquals(HONG_KONG, hongKongRecord2.setIdentifier());
        assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongRecord2.name());
        assertEquals("235.235.235.235", hongKongRecord2.ipAddress());

        ARecord laRecord1 = result.get(4);
        assertEquals(LA, laRecord1.setIdentifier());
        assertEquals(USA + DOT_DOMAIN_COM, laRecord1.name());
        assertEquals("123.123.123.123", laRecord1.ipAddress());

        ARecord laRecord2 = result.get(5);
        assertEquals(LA, laRecord2.setIdentifier());
        assertEquals(USA + DOT_DOMAIN_COM, laRecord2.name());
        assertEquals("125.125.125.125", laRecord2.ipAddress());

        ARecord nycRecord = result.get(6);
        assertEquals(NYC, nycRecord.setIdentifier());
        assertEquals(USA + DOT_DOMAIN_COM, nycRecord.name());
        assertEquals("13.13.13.13", nycRecord.ipAddress());

    }

    @Test
    public void testShouldDeleteARecordFromR53(){
        //given
        String ipAddressToRemove = "123.123.123.123";
        Long ttl = 300L;
        Long weight = 50L;

        Server serverToRemove = new TestUtil.ServerBuilder()
                .regionSubdomain(SWITZERLAND)
                .clusterSubdomain(GENEVA)
                .ipAddress(ipAddressToRemove)
                .build();

        ResourceRecordSet hongKongAResourceRecordSet = createAResourceRecordSet(
                HONG_KONG + DOT_DOMAIN_COM,
                HONG_KONG,
                List.of(createResourceRecord("234.234.234.234"),createResourceRecord("235.235.235.235")),
                ttl,
                weight
        );

        ResourceRecordSet usaAResourceRecordSet = createAResourceRecordSet(
                USA + DOT_DOMAIN_COM,
                USA,
                List.of(createResourceRecord("125.125.125.125")),
                ttl,
                weight
        );

        ListResourceRecordSetsResponse response =
                createListResourceRecordSetsResponse(
                        List.of(
                                getNsResourceRecordSet(),
                                getSoaResourceRecordSet(),
                                hongKongAResourceRecordSet,
                                usaAResourceRecordSet
                        )
                );


        doNothing().when(awsR53Service).removeResourceRecordByServer(serverToRemove);

        //when
        List<ARecord> result = service.removeServer(serverToRemove);

        //then
        assertEquals(3, result.size());

        ARecord hongKongARecord1 = result.get(0);
        assertEquals(HONG_KONG, hongKongARecord1.setIdentifier());
        assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongARecord1.name());
        assertEquals("234.234.234.234", hongKongARecord1.ipAddress());

        ARecord hongKongARecord2 = result.get(1);
        assertEquals(HONG_KONG, hongKongARecord2.setIdentifier());
        assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongARecord2.name());
        assertEquals("235.235.235.235", hongKongARecord2.ipAddress());

        ARecord usaARecord = result.get(2);
        assertEquals(USA, usaARecord.setIdentifier());
        assertEquals(USA + DOT_DOMAIN_COM, usaARecord.name());
        assertEquals("125.125.125.125", usaARecord.ipAddress());
    }

    @Test
    public void testShouldAddARecordToR53(){
        //given
        Long ttl = 300L;
        Long weight = 50L;

        Server serverToAdd = new TestUtil.ServerBuilder()
                .regionSubdomain(SWITZERLAND)
                .clusterSubdomain(GENEVA)
                .build();

        ResourceRecordSet hongKongAResourceRecordSet = createAResourceRecordSet(
                HONG_KONG + DOT_DOMAIN_COM,
                HONG_KONG,
                List.of(createResourceRecord("234.234.234.234"),createResourceRecord("235.235.235.235")),
                ttl,
                weight
        );

        ResourceRecordSet usaAResourceRecordSet = createAResourceRecordSet(
                USA + DOT_DOMAIN_COM,
                USA,
                List.of(createResourceRecord("125.125.125.125")),
                ttl,
                weight
        );

        ResourceRecordSet swissAResourceRecordSet = createAResourceRecordSet(
                SWITZERLAND + DOT_DOMAIN_COM,
                GENEVA,
                List.of(createResourceRecord("126.126.126.126")),
                ttl,
                weight
        );

        ListResourceRecordSetsResponse response =
                createListResourceRecordSetsResponse(
                        List.of(
                                getNsResourceRecordSet(),
                                getSoaResourceRecordSet(),
                                hongKongAResourceRecordSet,
                                usaAResourceRecordSet,
                                swissAResourceRecordSet
                        )
                );


        doNothing().when(awsR53Service).removeResourceRecordByServer(serverToAdd);

        //when
        List<ARecord> result = service.addServer(serverToAdd);

        //then
        assertEquals(4, result.size());

        ARecord hongKongARecord1 = result.get(0);
        assertEquals(HONG_KONG, hongKongARecord1.setIdentifier());
        assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongARecord1.name());
        assertEquals("234.234.234.234", hongKongARecord1.ipAddress());

        ARecord hongKongARecord2 = result.get(1);
        assertEquals(HONG_KONG, hongKongARecord2.setIdentifier());
        assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongARecord2.name());
        assertEquals("235.235.235.235", hongKongARecord2.ipAddress());

        ARecord usaARecord = result.get(2);
        assertEquals(USA, usaARecord.setIdentifier());
        assertEquals(USA + DOT_DOMAIN_COM, usaARecord.name());
        assertEquals("125.125.125.125", usaARecord.ipAddress());

        ARecord swissARecord = result.get(3);
        assertEquals(GENEVA, swissARecord.setIdentifier());
        assertEquals(SWITZERLAND + DOT_DOMAIN_COM, swissARecord.name());
        assertEquals("126.126.126.126", swissARecord.ipAddress());

    }
}
