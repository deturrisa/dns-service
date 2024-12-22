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
        List<ResourceRecordSet> resourceRecordSets = getResourceRecordSets();

        ListResourceRecordSetsResponse response =
                createListResourceRecordSetsResponse(
                        resourceRecordSets
                );

        when(awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId())).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        //when
        List<ARecord> result = mapper.getRoute53Records();

        assertEquals(8, result.size());

        ARecord frankfurtRecord = result.get(0);
        assertEquals("fra.domain.com.", frankfurtRecord.cityDomain());
        assertEquals("germany.domain.com.", frankfurtRecord.countryDomain());
        assertEquals("12.12.12.12", frankfurtRecord.ipAddress());

        ARecord genevaRecord = result.get(1);
        assertEquals("ge.domain.com.", genevaRecord.cityDomain());
        assertEquals("switzerland.domain.com.", genevaRecord.countryDomain());
        assertEquals("1.2.3.4", genevaRecord.ipAddress());

        ARecord hongKongRecord1 = result.get(2);
        assertEquals("hongkong.domain.com.", hongKongRecord1.cityDomain());
        assertEquals("hongkong.domain.com.", hongKongRecord1.countryDomain());
        assertEquals("234.234.234.234", hongKongRecord1.ipAddress());

        ARecord hongKongRecord2 = result.get(3);
        assertEquals("hongkong.domain.com.", hongKongRecord2.cityDomain());
        assertEquals("hongkong.domain.com.", hongKongRecord2.countryDomain());
        assertEquals("235.235.235.235", hongKongRecord2.ipAddress());

        ARecord laRecord1 = result.get(4);
        assertEquals("la.domain.com.", laRecord1.cityDomain());
        assertEquals("usa.domain.com.", laRecord1.countryDomain());
        assertEquals("123.123.123.123", laRecord1.ipAddress());

        ARecord laRecord2 = result.get(5);
        assertEquals("la.domain.com.", laRecord2.cityDomain());
        assertEquals("usa.domain.com.", laRecord2.countryDomain());
        assertEquals("125.125.125.125", laRecord2.ipAddress());

        ARecord nycRecord = result.get(6);
        assertEquals("nyc.domain.com.", nycRecord.cityDomain());
        assertEquals("usa.domain.com.", nycRecord.countryDomain());
        assertEquals("13.13.13.13", nycRecord.ipAddress());

        ARecord unknownRecord = result.get(7);
        assertEquals("xyz.domain.com.", unknownRecord.cityDomain());
        assertEquals("usa.domain.com.", unknownRecord.countryDomain());
        assertEquals("5.5.5.5", unknownRecord.ipAddress());

    }

}
