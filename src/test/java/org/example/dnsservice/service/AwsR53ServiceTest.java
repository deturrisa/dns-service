package org.example.dnsservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.util.TestUtil;
import org.example.dnsservice.util.TestUtil.ServerBuilder;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.*;


@UnitTest
class AwsR53ServiceTest {

    @Mock
    private Route53AsyncClient route53AsyncClient;

    @Mock
    private R53Properties r53Properties;

    @InjectMocks
    private AwsR53Service service;

    @BeforeEach
    public void setUp() {
        when(r53Properties.hostedZoneId()).thenReturn(HOSTED_ZONE_ID);
    }

    @Test
    void testShouldUpsertWhenResourceRecordsNotEmpty() {
        // given
        String ipAddressToRemove = "123.123.123.123";
        Long ttl = 300L;
        Long weight = 50L;

        Server serverToRemove = new TestUtil.ServerBuilder()
                .regionSubdomain(SWITZERLAND)
                .clusterSubdomain(GENEVA)
                .ipAddress(ipAddressToRemove)
                .build();

        ResourceRecordSet hongKongAResourceRecordSet = getUsaAResourceRecordSet(
                LA,
                List.of("234.234.234.234", "235.235.235.235"),
                ttl,
                weight
        );

        ResourceRecordSet usaAResourceRecordSet = getUsaAResourceRecordSet(
                LA,
                List.of(ipAddressToRemove, "125.125.125.125"),
                ttl,
                weight
        );

        ResourceRecordSet exptectedUsaAResourceRecordSet = getUsaAResourceRecordSet(
                LA,
                List.of("125.125.125.125"),
                ttl,
                weight
        );

        List<ResourceRecordSet> resourceRecordSets = List.of(
                getNsResourceRecordSet(),
                getSoaResourceRecordSet(),
                hongKongAResourceRecordSet,
                usaAResourceRecordSet
        );

        ListResourceRecordSetsResponse listResourceRecordSetsResponse = createListResourceRecordSetsResponse(
                resourceRecordSets
        );

        when(route53AsyncClient.listResourceRecordSets(
                ListResourceRecordSetsRequest.builder()
                        .hostedZoneId(HOSTED_ZONE_ID)
                        .build()
        )).thenReturn(CompletableFuture.completedFuture(listResourceRecordSetsResponse));

        List<ResourceRecordSet> expectedResourceRecordSets = List.of(
                hongKongAResourceRecordSet,
                exptectedUsaAResourceRecordSet
        );

        // when
        service.removeResourceRecordByServer(serverToRemove);

        // then
        verify(route53AsyncClient, times(1))
                .changeResourceRecordSets(
                        getUpsertChangeResourceRecordSetsRequest(expectedResourceRecordSets)
                );
    }

    @Test
    void testShouldDeleteWhenResourceRecordsIsEmpty() {
        // given
        String ipAddressToRemove = "123.123.123.123";
        Long ttl = 300L;
        Long weight = 50L;

        Server serverToRemove = new TestUtil.ServerBuilder()
                .regionSubdomain(SWITZERLAND)
                .clusterSubdomain(GENEVA)
                .ipAddress(ipAddressToRemove)
                .build();

        ResourceRecordSet usaAResourceRecordSet = getUsaAResourceRecordSet(
                LA,
                List.of(ipAddressToRemove),
                ttl,
                weight
        );

        ResourceRecordSet exptectedUsaAResourceRecordSet = getUsaAResourceRecordSet(
                LA,
                List.of(),
                ttl,+
                weight
        );

        List<ResourceRecordSet> resourceRecordSets = List.of(
                getNsResourceRecordSet(),
                getSoaResourceRecordSet(),
                usaAResourceRecordSet
        );

        ListResourceRecordSetsResponse listResourceRecordSetsResponse = createListResourceRecordSetsResponse(
                resourceRecordSets
        );

        when(route53AsyncClient.listResourceRecordSets(
                ListResourceRecordSetsRequest.builder()
                        .hostedZoneId(HOSTED_ZONE_ID)
                        .build()
        )).thenReturn(CompletableFuture.completedFuture(listResourceRecordSetsResponse));

        List<ResourceRecordSet> expectedResourceRecordSets = List.of(
                exptectedUsaAResourceRecordSet
        );

        ListResourceRecordSetsResponse expectedListResourceRecordSetsResponse = createListResourceRecordSetsResponse(
                expectedResourceRecordSets
        );

        // when
        service.removeResourceRecordByServer(serverToRemove);

        // then

        verify(route53AsyncClient, times(1))
                .changeResourceRecordSets(
                        getDeleteChangeResourceRecordSetsRequest(expectedResourceRecordSets)
                );
    }

    @Test
    void testShouldAddNewResourceRecordSetWhenServerDoesNotExistsInResourceRecordSet() {
        //given
        String ipAddress = "123.123.123.123";
        Server server = new ServerBuilder()
                .regionSubdomain(USA)
                .clusterSubdomain(LA)
                .ipAddress(ipAddress)
                .build();

        when(route53AsyncClient.getHostedZone(
                GetHostedZoneRequest.builder()
                        .id(HOSTED_ZONE_ID)
                        .build())
        ).thenReturn(CompletableFuture.completedFuture(
                GetHostedZoneResponse.builder()
                        .hostedZone(
                                HostedZone.builder()
                                        .name("domain.com.")
                                        .build())
                        .build()));

        when(route53AsyncClient.listResourceRecordSets(
                ListResourceRecordSetsRequest.builder()
                        .hostedZoneId(HOSTED_ZONE_ID)
                        .build())
        ).thenReturn(CompletableFuture.completedFuture(
                ListResourceRecordSetsResponse.builder()
                        .resourceRecordSets(List.of())
                        .build()));

        //when
        service.addResourceRecordByServer(server);

        //then
        getUpsertChangeResourceRecordSetsRequest(List.of());
    }

    @Test
    void testShouldAddResourceRecordWhenServerExistsInResourceRecordSet() {
        //given
        String ipAddress = "125.125.125.125";
        Server server = new ServerBuilder()
                .regionSubdomain(USA)
                .clusterSubdomain(LA)
                .ipAddress(ipAddress)
                .build();

        when(route53AsyncClient.getHostedZone(
                GetHostedZoneRequest.builder()
                        .id(HOSTED_ZONE_ID)
                        .build())
        ).thenReturn(CompletableFuture.completedFuture(
                GetHostedZoneResponse.builder()
                        .hostedZone(
                                HostedZone.builder()
                                        .name("domain.com.")
                                        .build())
                        .build()));

        ResourceRecord existingRecord = ResourceRecord.builder()
                .value("123.123.123.123")
                .build();

        ResourceRecordSet existingRecordSet = ResourceRecordSet.builder()
                .name(USA + DOT_DOMAIN_COM)
                .type(RRType.A)
                .resourceRecords(List.of(existingRecord))
                .build();

        when(route53AsyncClient.listResourceRecordSets(
                ListResourceRecordSetsRequest.builder()
                        .hostedZoneId(HOSTED_ZONE_ID)
                        .build())
        ).thenReturn(CompletableFuture.completedFuture(
                ListResourceRecordSetsResponse.builder()
                        .resourceRecordSets(List.of(existingRecordSet))
                        .build()));

        //when
        service.addResourceRecordByServer(server);

        //then
        verify(route53AsyncClient, times(1))
                .changeResourceRecordSets(
                        getUpsertChangeResourceRecordSetsRequest(List.of(existingRecordSet))
                );
    }

}

