package org.example.dnsservice.service;

import static org.example.dnsservice.util.TestUtil.DOT_DOMAIN_COM;
import static org.example.dnsservice.util.TestUtil.FRANKFURT;
import static org.example.dnsservice.util.TestUtil.FRANKFURT_IP;
import static org.example.dnsservice.util.TestUtil.GENEVA;
import static org.example.dnsservice.util.TestUtil.GENEVA_IP;
import static org.example.dnsservice.util.TestUtil.GERMANY;
import static org.example.dnsservice.util.TestUtil.HONG_KONG;
import static org.example.dnsservice.util.TestUtil.LA;
import static org.example.dnsservice.util.TestUtil.NYC;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.createAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.createIpResourceRecords;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.createListResourceRecordSetsResponse;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getFrankfurtAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getGenevaAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getHongKongAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getLaAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getNsResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getNycAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getSoaResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.SWITZERLAND;
import static org.example.dnsservice.util.TestUtil.USA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@UnitTest
public class ARecordServiceTest {

  @Mock private AwsR53Service awsR53Service;

  @Mock private DomainRegionProperties domainRegionProperties;

  @InjectMocks private ARecordService service;

  @Test
  public void testShouldMapListRecordResponseToRoute53Records() {
    // given
    var ignoredNameResourceRecordSet =
        createAResourceRecordSet(
            "uk." + DOT_DOMAIN_COM, "ma", createIpResourceRecords(List.of("1.1.1.1")));
    var ignoredSetIdentifierResourceRecordSet =
        createAResourceRecordSet(
            GERMANY + DOT_DOMAIN_COM, "ber", createIpResourceRecords(List.of("12.12.12.12")));
    var invalidDomainNameResourceRecordSet =
        createAResourceRecordSet(
            "abc.xyz" + DOT_DOMAIN_COM, "ma", createIpResourceRecords(List.of("1.1.1.1")));

    var resourceRecordSets =
        List.of(
            getNsResourceRecordSet(),
            getSoaResourceRecordSet(),
            getFrankfurtAResourceRecordSet(),
            getGenevaAResourceRecordSet(),
            getHongKongAResourceRecordSet(),
            getLaAResourceRecordSet(),
            getNycAResourceRecordSet(),
            ignoredNameResourceRecordSet,
            ignoredSetIdentifierResourceRecordSet,
            invalidDomainNameResourceRecordSet);

    var response = createListResourceRecordSetsResponse(resourceRecordSets);

    when(awsR53Service.getResourceRecordSets())
        .thenReturn(CompletableFuture.completedFuture(response));

    when(domainRegionProperties.getDomainRegions())
        .thenReturn(
            List.of(
                new DomainRegion(USA, Set.of(LA, NYC)),
                new DomainRegion(SWITZERLAND, Set.of(GENEVA)),
                new DomainRegion(HONG_KONG, Set.of(HONG_KONG)),
                new DomainRegion(GERMANY, Set.of(FRANKFURT))));

    // when
    var result = service.getARecords();

    assertEquals(7, result.size());

    var frankfurtRecord = result.get(0);
    assertEquals(FRANKFURT, frankfurtRecord.setIdentifier());
    assertEquals(GERMANY + DOT_DOMAIN_COM, frankfurtRecord.name());
    assertEquals(FRANKFURT_IP, frankfurtRecord.ipAddress());

    var genevaRecord = result.get(1);
    assertEquals(GENEVA, genevaRecord.setIdentifier());
    assertEquals(SWITZERLAND + DOT_DOMAIN_COM, genevaRecord.name());
    assertEquals(GENEVA_IP, genevaRecord.ipAddress());

    var hongKongRecord1 = result.get(2);
    assertEquals(HONG_KONG, hongKongRecord1.setIdentifier());
    assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongRecord1.name());
    assertEquals("234.234.234.234", hongKongRecord1.ipAddress());

    var hongKongRecord2 = result.get(3);
    assertEquals(HONG_KONG, hongKongRecord2.setIdentifier());
    assertEquals(HONG_KONG + DOT_DOMAIN_COM, hongKongRecord2.name());
    assertEquals("235.235.235.235", hongKongRecord2.ipAddress());

    var laRecord1 = result.get(4);
    assertEquals(LA, laRecord1.setIdentifier());
    assertEquals(USA + DOT_DOMAIN_COM, laRecord1.name());
    assertEquals("123.123.123.123", laRecord1.ipAddress());

    var laRecord2 = result.get(5);
    assertEquals(LA, laRecord2.setIdentifier());
    assertEquals(USA + DOT_DOMAIN_COM, laRecord2.name());
    assertEquals("125.125.125.125", laRecord2.ipAddress());

    var nycRecord = result.get(6);
    assertEquals(NYC, nycRecord.setIdentifier());
    assertEquals(USA + DOT_DOMAIN_COM, nycRecord.name());
    assertEquals("13.13.13.13", nycRecord.ipAddress());
  }
}
