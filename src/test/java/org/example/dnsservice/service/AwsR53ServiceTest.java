package org.example.dnsservice.service;

import static org.example.dnsservice.util.TestUtil.DOT_DOMAIN_COM;
import static org.example.dnsservice.util.TestUtil.HOSTED_ZONE_ID;
import static org.example.dnsservice.util.TestUtil.LA;
import static org.example.dnsservice.util.TestUtil.LA_IP_1;
import static org.example.dnsservice.util.TestUtil.NYC;
import static org.example.dnsservice.util.TestUtil.NYC_IP;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.createAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.createIpResourceRecords;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.createListResourceRecordSetsResponse;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getChangeResourceRecordSetsRequest;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getChangeResourceRecordSetsResponse;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getGenevaAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getGetHostedZoneRequest;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getGetHostedZoneResponse;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getLaAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getListResourceRecordSetsRequest;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getNsAndSoaResourceRecordSets;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getNycAResourceRecordSet;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.getResourceRecordSets;
import static org.example.dnsservice.util.TestUtil.ServerBuilder;
import static org.example.dnsservice.util.TestUtil.ServerTestData.GENEVA_SERVER;
import static org.example.dnsservice.util.TestUtil.ServerTestData.LA_SERVER_2;
import static org.example.dnsservice.util.TestUtil.ServerTestData.NYC_SERVER;
import static org.example.dnsservice.util.TestUtil.TTL;
import static org.example.dnsservice.util.TestUtil.USA;
import static org.example.dnsservice.util.TestUtil.WEIGHT;
import static org.example.dnsservice.util.TestUtil.getRandomIp;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;

@UnitTest
class AwsR53ServiceTest {

  @Mock private Route53AsyncClient route53AsyncClient;

  @Mock private R53Properties r53Properties;

  @InjectMocks private AwsR53Service service;

  @BeforeEach
  public void setUp() {
    when(r53Properties.hostedZoneId()).thenReturn(HOSTED_ZONE_ID);
    when(r53Properties.ttl()).thenReturn(TTL);
    when(r53Properties.weight()).thenReturn(WEIGHT);

    when(route53AsyncClient.changeResourceRecordSets(any(ChangeResourceRecordSetsRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(getChangeResourceRecordSetsResponse()));
  }

  @Nested
  public class AddTest {

    @Test
    public void testShouldAddNewResourceRecordSet() {
      // given
      var resourceRecordSets = getNsAndSoaResourceRecordSets();

      var listResourceRecordSetsResponse = createListResourceRecordSetsResponse(resourceRecordSets);

      when(route53AsyncClient.getHostedZone(getGetHostedZoneRequest()))
          .thenReturn(CompletableFuture.completedFuture(getGetHostedZoneResponse()));

      when(route53AsyncClient.listResourceRecordSets(getListResourceRecordSetsRequest()))
          .thenReturn(CompletableFuture.completedFuture(listResourceRecordSetsResponse));

      var expectedResourceRecordSet = getGenevaAResourceRecordSet();

      // when
      service.addResourceRecordByServer(GENEVA_SERVER);

      // then
      verify(route53AsyncClient, times(1))
          .changeResourceRecordSets(
              getChangeResourceRecordSetsRequest(ChangeAction.UPSERT, expectedResourceRecordSet));
    }

    @Test
    public void testShouldAddNewResourceRecord() {
      // given
      var newIp = getRandomIp();
      var newServer =
          new ServerBuilder().regionSubdomain(USA).clusterSubdomain(NYC).ipAddress(newIp).build();

      var listResourceRecordSetsResponse =
          createListResourceRecordSetsResponse(getResourceRecordSets(getNycAResourceRecordSet()));

      when(route53AsyncClient.listResourceRecordSets(getListResourceRecordSetsRequest()))
          .thenReturn(CompletableFuture.completedFuture(listResourceRecordSetsResponse));

      var expectedResourceRecordSet =
          createAResourceRecordSet(
              USA + DOT_DOMAIN_COM, NYC, createIpResourceRecords(List.of(NYC_IP, newIp)));

      // when
      service.addResourceRecordByServer(newServer);

      // then
      verify(route53AsyncClient, times(1))
          .changeResourceRecordSets(
              getChangeResourceRecordSetsRequest(ChangeAction.UPSERT, expectedResourceRecordSet));
    }
  }

  @Nested
  public class RemoveTest {

    @Test
    public void testShouldDeleteResourceRecordSet() {
      // given
      var nycResourceRecordSet = getNycAResourceRecordSet();

      var listResourceRecordSetsResponse =
          createListResourceRecordSetsResponse(getResourceRecordSets(nycResourceRecordSet));

      when(route53AsyncClient.listResourceRecordSets(getListResourceRecordSetsRequest()))
          .thenReturn(CompletableFuture.completedFuture(listResourceRecordSetsResponse));

      // when
      service.removeResourceRecordByServer(NYC_SERVER);

      // then
      verify(route53AsyncClient, times(1))
          .changeResourceRecordSets(
              getChangeResourceRecordSetsRequest(ChangeAction.DELETE, nycResourceRecordSet));
    }

    @Test
    public void testShouldRemoveResourceRecord() {
      // given
      var laResourceRecordSet = getLaAResourceRecordSet();

      var listResourceRecordSetsResponse =
          createListResourceRecordSetsResponse(getResourceRecordSets(laResourceRecordSet));

      when(route53AsyncClient.listResourceRecordSets(getListResourceRecordSetsRequest()))
          .thenReturn(CompletableFuture.completedFuture(listResourceRecordSetsResponse));

      var expectedResourceRecordSet =
          createAResourceRecordSet(
              USA + DOT_DOMAIN_COM, LA, createIpResourceRecords(List.of(LA_IP_1)));

      // when
      service.removeResourceRecordByServer(LA_SERVER_2);

      // then
      verify(route53AsyncClient, times(1))
          .changeResourceRecordSets(
              getChangeResourceRecordSetsRequest(ChangeAction.UPSERT, expectedResourceRecordSet));
    }
  }
}
