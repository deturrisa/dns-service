package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.R53Properties;
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
import java.util.concurrent.CompletableFuture;
import static org.example.dnsservice.util.TestUtil.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@UnitTest
public class Route53RecordMapperTest {

    @Mock
    private AwsR53Service awsR53Service;

    @Mock
    private R53Properties r53Properties;

    @InjectMocks
    private Route53RecordMapper mapper;

    @BeforeEach
    public void setUp() {
        when(r53Properties.hostedZoneId()).thenReturn("hosted-zone-id");
    }

    @Test
    public void testShouldMapListRecordResponseToRoute53Records(){
        //given
        List<ResourceRecordSet> resourceRecordSets = List.of(
                getNsResourceRecordSet(),
                getSoaResourceRecordSet(),
                getGermanyAResourceRecordSet("fra",
                        List.of("12.12.12.12")
                ),
                getSwitzerlandAResourceRecordSet("ge",
                        List.of("1.2.3.4")
                ),
                getHongKongAResourceRecordSet(null,
                        List.of("234.234.234.234", "235.235.235.235")
                ),
                getUsaAResourceRecordSet("la",
                        List.of("123.123.123.123", "125.125.125.125")
                ),
                getUsaAResourceRecordSet("nyc",
                        List.of("13.13.13.13")
                )
        );

        ListResourceRecordSetsResponse response =
                createListResourceRecordSetsResponse(
                        resourceRecordSets
                );

        when(awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId())).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        //when
        List<ARecord> result = mapper.getRoute53Records();

        assertEquals(7, result.size());

        ARecord frankfurtRecord = result.get(0);
        assertEquals("fra", frankfurtRecord.setIdentifier());
        assertEquals("germany.domain.com.", frankfurtRecord.name());
        assertEquals("12.12.12.12", frankfurtRecord.ipAddress());

        ARecord genevaRecord = result.get(1);
        assertEquals("ge", genevaRecord.setIdentifier());
        assertEquals("switzerland.domain.com.", genevaRecord.name());
        assertEquals("1.2.3.4", genevaRecord.ipAddress());

        ARecord hongKongRecord1 = result.get(2);
        assertEquals(null, hongKongRecord1.setIdentifier());
        assertEquals("hongkong.domain.com.", hongKongRecord1.name());
        assertEquals("234.234.234.234", hongKongRecord1.ipAddress());

        ARecord hongKongRecord2 = result.get(3);
        assertEquals(null, hongKongRecord2.setIdentifier());
        assertEquals("hongkong.domain.com.", hongKongRecord2.name());
        assertEquals("235.235.235.235", hongKongRecord2.ipAddress());

        ARecord laRecord1 = result.get(4);
        assertEquals("la", laRecord1.setIdentifier());
        assertEquals("usa.domain.com.", laRecord1.name());
        assertEquals("123.123.123.123", laRecord1.ipAddress());

        ARecord laRecord2 = result.get(5);
        assertEquals("la", laRecord2.setIdentifier());
        assertEquals("usa.domain.com.", laRecord2.name());
        assertEquals("125.125.125.125", laRecord2.ipAddress());

        ARecord nycRecord = result.get(6);
        assertEquals("nyc", nycRecord.setIdentifier());
        assertEquals("usa.domain.com.", nycRecord.name());
        assertEquals("13.13.13.13", nycRecord.ipAddress());

    }

}
