package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.service.AwsR53Service;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.when;

@UnitTest
public class Route53RecordMapperTest {

    @Mock
    private AwsR53Service awsR53Service;

    @Mock
    private R53Properties r53Properties;

    @Mock
    private DomainRegionProperties domainRegionProperties;

    @InjectMocks
    private Route53RecordMapper mapper;

    @BeforeEach
    public void setUp() {
        when(r53Properties.hostedZoneId()).thenReturn("hosted-zone-id");
        when(domainRegionProperties.getDomainRegions()).thenReturn(
                List.of(
                        new DomainRegion(USA, Set.of(LA, NYC)),
                        new DomainRegion(SWITZERLAND, Set.of(GENEVA)),
                        new DomainRegion(HONG_KONG, Set.of(HONG_KONG)),
                        new DomainRegion(GERMANY, Set.of(FRANKFURT))
                )
        );
    }

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

        when(awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId())).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        //when
        List<ARecord> result = mapper.getARecords();

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
}
