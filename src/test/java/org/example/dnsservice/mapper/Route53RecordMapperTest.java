package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.CNameRecord;
import org.example.dnsservice.model.Route53Record;
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
        List<Route53Record> result = mapper.getRoute53Records();

        assertEquals(4, result.size());

        assertInstanceOf(CNameRecord.class, result.get(0));
        CNameRecord usaCNameRecord = (CNameRecord) result.get(0);
        assertEquals("usa.domain.com.", usaCNameRecord.getResourceRecordSetName());
        assertEquals("la.domain.com", usaCNameRecord.getResourceRecordValue());

        assertInstanceOf(ARecord.class, result.get(1));
        assertEquals("hongkong.domain.com.", result.get(1).getResourceRecordSetName());
        assertEquals("1.2.3.4.5", result.get(1).getResourceRecordValue());

        assertInstanceOf(ARecord.class, result.get(2));
        assertEquals("hongkong.domain.com.", result.get(2).getResourceRecordSetName());
        assertEquals("6.7.8.9.10", result.get(2).getResourceRecordValue());

        assertInstanceOf(ARecord.class, result.get(3));
        ARecord laARecord = (ARecord) result.get(3);
        assertEquals("la.domain.com.", laARecord.getResourceRecordSetName());
        assertEquals("123.123.123.123", laARecord.getResourceRecordValue());
        assertEquals(usaCNameRecord, laARecord.getCNameRecord());

    }

}
