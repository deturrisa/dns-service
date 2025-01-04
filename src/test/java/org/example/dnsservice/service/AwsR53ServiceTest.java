package org.example.dnsservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.*;


@ExtendWith(MockitoExtension.class)
class AwsR53ServiceTest {

    @InjectMocks
    private AwsR53Service service;

    @Mock
    private Route53AsyncClient route53AsyncClient;

    @Test
    void upsertResourceRecordSet() {
        // given
        String ipAddressToRemove = "123.123.123.123";
        Long ttl = 300L;
        Long weight = 50L;

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

        ListResourceRecordSetsResponse expectedListResourceRecordSetsResponse = createListResourceRecordSetsResponse(
                expectedResourceRecordSets
        );

        // when
        ListResourceRecordSetsResponse result =
                service.upsertResourceRecordSet(HOSTED_ZONE_ID, ipAddressToRemove);

        // then
        assertThat(result).isEqualTo(expectedListResourceRecordSetsResponse);

        verify(route53AsyncClient, times(1))
                .changeResourceRecordSets(
                        getChangeResourceRecordSetsRequest(expectedResourceRecordSets)
                );
    }
}