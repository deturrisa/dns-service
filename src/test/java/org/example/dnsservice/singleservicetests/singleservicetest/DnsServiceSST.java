package org.example.dnsservice.singleservicetests.singleservicetest;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.model.Action;
import org.example.dnsservice.singleservicetests.BaseSST;
import org.example.dnsservice.singleservicetests.ExternalPlatform;
import org.example.dnsservice.singleservicetests.SingleServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import software.amazon.awssdk.services.route53.model.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import static org.example.dnsservice.util.TestUtil.*;
import static org.example.dnsservice.util.TestUtil.EntityTestData.*;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@SingleServiceTest({ExternalPlatform.POSTGRES, ExternalPlatform.REST_SERVER})
public class DnsServiceSST extends BaseSST {

    @BeforeEach
    public void setup() {
        serverRepository.deleteAll();
        clusterRepository.deleteAll();
        when(r53Properties.hostedZoneId()).thenReturn(HOSTED_ZONE_ID);
        when(r53Properties.ttl()).thenReturn(TTL);
        when(r53Properties.weight()).thenReturn(WEIGHT);
        when(domainRegionProperties.getDomainRegions()).thenReturn(
                List.of(
                        new DomainRegion(USA, Set.of(LA, NYC)),
                        new DomainRegion(SWITZERLAND, Set.of(GENEVA)),
                        new DomainRegion(HONG_KONG, Set.of(HONG_KONG)),
                        new DomainRegion(GERMANY, Set.of(FRANKFURT)),
                        new DomainRegion(ABC, Set.of(XYZ))
                )
        );
    }

    @Test
    public void testShouldRenderHomePage() throws Exception {
        //given
        clusterRepository.saveAll(List.of(LA_CLUSTER_ENTITY, GENEVA_CLUSTER_ENTITY));
        serverRepository.saveAll(List.of(LA_SERVER_ENTITY_1, LA_SERVER_ENTITY_2, GENEVA_SERVER_ENTITY));

        var listResourceRecordSetsResponse =
                createListResourceRecordSetsResponse(List.of(
                        getLaAResourceRecordSet(),
                        getXyzAResourceRecordSet()
                ));

        when(route53AsyncClient.listResourceRecordSets(
                getListResourceRecordSetsRequest()
        )).thenReturn(CompletableFuture.completedFuture(listResourceRecordSetsResponse));

        //when
        var response = getDnsServiceHomePage();

        //then
        assertHomePageServerEntryTable(response);
        assertHomePageDnsEntryTable(response);
    }

    @Test
    public void testShouldRenderHomePageAfterDeleteResourceRecordSet() throws Exception {
        //given
        clusterRepository.saveAll(List.of(LA_CLUSTER_ENTITY, NYC_CLUSTER_ENTITY, GENEVA_CLUSTER_ENTITY));
        serverRepository.saveAll(List.of(LA_SERVER_ENTITY_1, LA_SERVER_ENTITY_2, NYC_SERVER_ENTITY, GENEVA_SERVER_ENTITY));

        var listResourceRecordSetsResponseBeforeDelete =
                createListResourceRecordSetsResponse(List.of(
                        getLaAResourceRecordSet(),
                        getNycAResourceRecordSet()
                ));

        var listResourceRecordSetsResponseAfterDelete =
                createListResourceRecordSetsResponse(List.of(
                        getLaAResourceRecordSet()
                ));

        when(route53AsyncClient.listResourceRecordSets(
                getListResourceRecordSetsRequest()
        )).thenReturn(
                CompletableFuture.completedFuture(listResourceRecordSetsResponseBeforeDelete),
                CompletableFuture.completedFuture(listResourceRecordSetsResponseAfterDelete)
        );

        var changeRequest = getChangeResourceRecordSetsRequest(
                ChangeAction.DELETE,
                getNycAResourceRecordSet()
        );

        when(route53AsyncClient.changeResourceRecordSets(changeRequest))
                .thenReturn(getChangeResourceRecordSetsResponse());

        //when
        var response = clickRemoveFromRotationEndpoint(NYC_SERVER_ENTITY);

        //then
        assertRemoveServerEntryHomePageAfterDeleteResourceRecordSet(response);
        assertRemoveDnsEntryHomePageAfterDeleteResourceRecordSet(response);
    }

    @Test
    public void testShouldRenderHomePageAfterRemoveResourceRecord() throws Exception {
        //given
        clusterRepository.saveAll(List.of(LA_CLUSTER_ENTITY, NYC_CLUSTER_ENTITY, GENEVA_CLUSTER_ENTITY));
        serverRepository.saveAll(List.of(LA_SERVER_ENTITY_1, LA_SERVER_ENTITY_2, NYC_SERVER_ENTITY, GENEVA_SERVER_ENTITY));

        var listResourceRecordSetsResponseBeforeRemove =
                createListResourceRecordSetsResponse(List.of(
                        getLaAResourceRecordSet(),
                        getNycAResourceRecordSet()
                ));

        var listResourceRecordSetsResponseAfterRemove =
                createListResourceRecordSetsResponse(List.of(
                        createAResourceRecordSet(
                                USA + DOT_DOMAIN_COM,
                                LA,
                                createIpResourceRecords(List.of(LA_IP_2))
                        ),
                        getNycAResourceRecordSet()
                ));

        when(route53AsyncClient.listResourceRecordSets(
                getListResourceRecordSetsRequest()
        )).thenReturn(
                CompletableFuture.completedFuture(listResourceRecordSetsResponseBeforeRemove),
                CompletableFuture.completedFuture(listResourceRecordSetsResponseAfterRemove)
        );

        var changeRequest = getChangeResourceRecordSetsRequest(
                ChangeAction.UPSERT,
                createAResourceRecordSet(
                        USA + DOT_DOMAIN_COM,
                        LA,
                        createResourceRecords(List.of(LA_IP_2))
                )
        );

        when(route53AsyncClient.changeResourceRecordSets(changeRequest))
                .thenReturn(getChangeResourceRecordSetsResponse());

        //when
        var response = clickRemoveFromRotationEndpoint(LA_SERVER_ENTITY_1);

        //then
        assertRemoveServerEntryHomePageAfterRemoveResourceRecord(response);
        assertRemoveDnsEntryHomePageAfterRemoveResourceRecord(response);
    }

    @Test
    public void testShouldRenderHomePageAfterAddResourceRecordSet() throws Exception {
        //given
        clusterRepository.saveAll(List.of(LA_CLUSTER_ENTITY, NYC_CLUSTER_ENTITY, GENEVA_CLUSTER_ENTITY));
        serverRepository.saveAll(List.of(LA_SERVER_ENTITY_1, LA_SERVER_ENTITY_2, NYC_SERVER_ENTITY, GENEVA_SERVER_ENTITY));

        var listResourceRecordSetsResponseBeforeAdd =
                createListResourceRecordSetsResponse(List.of(
                        getLaAResourceRecordSet(),
                        getNycAResourceRecordSet()
                ));

        var listResourceRecordSetsResponseAfterAdd =
                createListResourceRecordSetsResponse(List.of(
                        getLaAResourceRecordSet(),
                        getNycAResourceRecordSet(),
                        getGenevaAResourceRecordSet()
                ));

        when(route53AsyncClient.listResourceRecordSets(
                getListResourceRecordSetsRequest()
        )).thenReturn(
                CompletableFuture.completedFuture(listResourceRecordSetsResponseBeforeAdd),
                CompletableFuture.completedFuture(listResourceRecordSetsResponseAfterAdd)
        );

        var changeRequest = getChangeResourceRecordSetsRequest(
                ChangeAction.UPSERT,
                getGenevaAResourceRecordSet()
        );

        when(route53AsyncClient.changeResourceRecordSets(changeRequest))
                .thenReturn(getChangeResourceRecordSetsResponse());

        when(route53AsyncClient.getHostedZone(getGetHostedZoneRequest()))
                .thenReturn(CompletableFuture.completedFuture(getGetHostedZoneResponse()));

        //when
        var response = clickAddToRotationEndpoint(GENEVA_SERVER_ENTITY);

        //then
        assertAddServerEntryHomePageAfterAddResourceRecordSet(response);
        assertAddDnsEntryHomePageAfterAddResourceRecordSet(response);
    }

    @Test
    public void testShouldRenderHomePageAfterAddResourceRecord() throws Exception {
        //given
        clusterRepository.saveAll(List.of(LA_CLUSTER_ENTITY, NYC_CLUSTER_ENTITY, GENEVA_CLUSTER_ENTITY));
        serverRepository.saveAll(List.of(LA_SERVER_ENTITY_1, LA_SERVER_ENTITY_2, NYC_SERVER_ENTITY, GENEVA_SERVER_ENTITY));

        var listResourceRecordSetsResponseBeforeAdd =
                createListResourceRecordSetsResponse(List.of(
                        createAResourceRecordSet(
                                USA + DOT_DOMAIN_COM,
                                LA,
                                createIpResourceRecords(List.of(LA_IP_1))
                        ),
                        getNycAResourceRecordSet(),
                        getXyzAResourceRecordSet()
                ));

        var listResourceRecordSetsResponseAfterAdd =
                createListResourceRecordSetsResponse(List.of(
                        getLaAResourceRecordSet(),
                        getNycAResourceRecordSet(),
                        getXyzAResourceRecordSet()
                ));

        when(route53AsyncClient.listResourceRecordSets(
                getListResourceRecordSetsRequest()
        )).thenReturn(
                CompletableFuture.completedFuture(listResourceRecordSetsResponseBeforeAdd),
                CompletableFuture.completedFuture(listResourceRecordSetsResponseAfterAdd)
        );

        var changeRequest = getChangeResourceRecordSetsRequest(
                ChangeAction.UPSERT,
                getLaAResourceRecordSet()
        );

        when(route53AsyncClient.changeResourceRecordSets(changeRequest))
                .thenReturn(getChangeResourceRecordSetsResponse());

        when(route53AsyncClient.getHostedZone(getGetHostedZoneRequest()))
                .thenReturn(CompletableFuture.completedFuture(getGetHostedZoneResponse()));

        //when
        var response = clickAddToRotationEndpoint(LA_SERVER_ENTITY_2);

        //then
        assertAddServerEntryHomePageAfterAddResourceRecord(response);
        assertAddDnsEntryHomePageAfterAddResourceRecord(response);
    }


    private static void assertAddServerEntryHomePageAfterAddResourceRecord(ResultActions resultActions) throws Exception {
        var expectedRowCount = 5;
        var serverTable = "//table[@id='serverEntries']";

        var friendlyNameCol = "td[1]";
        var clusterCol = "td[2]";
        var dnsStatusCol = "td[3]";
        var actionCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(serverTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(serverTable + "//tr[1]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_1.getId()))
                .andExpect(xpath(serverTable + "//tr[1]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[1]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[1]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server2
                .andExpect(xpath(serverTable + "//tr[2]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_2.getId()))
                .andExpect(xpath(serverTable + "//tr[2]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[2]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[2]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server7
                .andExpect(xpath( serverTable + "//tr[3]/" + friendlyNameCol).string("server" + NYC_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[3]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[3]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[3]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server20
                .andExpect(xpath( serverTable + "//tr[4]/" + friendlyNameCol).string("server" + GENEVA_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[4]/" + clusterCol).string(SWITZERLAND))
                .andExpect(xpath(serverTable + "//tr[4]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[4]/" + actionCol).string(Action.ADD.getDescription()));
    }

    private static void assertAddDnsEntryHomePageAfterAddResourceRecord(ResultActions resultActions) throws Exception {
        var expectedRowCount = 5;
        var dnsTable = "//table[@id='dnsEntries']";

        var domainStringCol = "td[1]";
        var ipCol = "td[2]";
        var serverFriendlyNameCol = "td[3]";
        var clusterNameCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(dnsTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(dnsTable + "//tr[1]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[1]/" + ipCol).string(LA_SERVER_ENTITY_1.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_1.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + clusterNameCol).string(LA_SERVER_ENTITY_1.getCluster().getName()))
                //server2
                .andExpect(xpath(dnsTable + "//tr[2]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[2]/" + ipCol).string(LA_SERVER_ENTITY_2.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_2.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + clusterNameCol).string(LA_SERVER_ENTITY_2.getCluster().getName()))
                //server7
                .andExpect(xpath( dnsTable + "//tr[3]/" + domainStringCol).string(NYC + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[3]/" + ipCol).string(NYC_IP))
                .andExpect(xpath(dnsTable + "//tr[3]/" + serverFriendlyNameCol).string(NYC_SERVER_ENTITY.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[3]/" + clusterNameCol).string(NYC_SERVER_ENTITY.getCluster().getName()))
                //server20
                .andExpect(xpath( dnsTable + "//tr[4]/" + domainStringCol).string(XYZ + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[4]/" + ipCol).string(XYZ_IP))
                .andExpect(xpath(dnsTable + "//tr[4]/" + serverFriendlyNameCol).string("not found"))
                .andExpect(xpath(dnsTable + "//tr[4]/" + clusterNameCol).string("N/A"));
    }

    private static void assertRemoveServerEntryHomePageAfterRemoveResourceRecord(ResultActions resultActions) throws Exception {
        var expectedRowCount = 5;
        var serverTable = "//table[@id='serverEntries']";

        var friendlyNameCol = "td[1]";
        var clusterCol = "td[2]";
        var dnsStatusCol = "td[3]";
        var actionCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(serverTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(serverTable + "//tr[1]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_1.getId()))
                .andExpect(xpath(serverTable + "//tr[1]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[1]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[1]/" + actionCol).string(Action.ADD.getDescription()))
                //server2
                .andExpect(xpath(serverTable + "//tr[2]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_2.getId()))
                .andExpect(xpath(serverTable + "//tr[2]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[2]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[2]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server7
                .andExpect(xpath( serverTable + "//tr[3]/" + friendlyNameCol).string("server" + NYC_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[3]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[3]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[3]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server20
                .andExpect(xpath( serverTable + "//tr[4]/" + friendlyNameCol).string("server" + GENEVA_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[4]/" + clusterCol).string(SWITZERLAND))
                .andExpect(xpath(serverTable + "//tr[4]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[4]/" + actionCol).string(Action.ADD.getDescription()));
    }

    private static void assertRemoveDnsEntryHomePageAfterRemoveResourceRecord(ResultActions resultActions) throws Exception {
        var expectedRowCount = 3;
        var dnsTable = "//table[@id='dnsEntries']";

        var domainStringCol = "td[1]";
        var ipCol = "td[2]";
        var serverFriendlyNameCol = "td[3]";
        var clusterNameCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(dnsTable + "//tr").nodeCount(expectedRowCount))//server2
                .andExpect(xpath(dnsTable + "//tr[1]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[1]/" + ipCol).string(LA_SERVER_ENTITY_2.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_2.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + clusterNameCol).string(LA_SERVER_ENTITY_2.getCluster().getName()))
                //server20
                .andExpect(xpath( dnsTable + "//tr[2]/" + domainStringCol).string(NYC + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[2]/" + ipCol).string(NYC_IP))
                .andExpect(xpath(dnsTable + "//tr[2]/" + serverFriendlyNameCol).string(NYC_SERVER_ENTITY.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + clusterNameCol).string(NYC_SERVER_ENTITY.getCluster().getName()));
    }

    private static void assertRemoveServerEntryHomePageAfterDeleteResourceRecordSet(ResultActions resultActions) throws Exception {
        var expectedRowCount = 5;
        var serverTable = "//table[@id='serverEntries']";

        var friendlyNameCol = "td[1]";
        var clusterCol = "td[2]";
        var dnsStatusCol = "td[3]";
        var actionCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(serverTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(serverTable + "//tr[1]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_1.getId()))
                .andExpect(xpath(serverTable + "//tr[1]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[1]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[1]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server2
                .andExpect(xpath(serverTable + "//tr[2]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_2.getId()))
                .andExpect(xpath(serverTable + "//tr[2]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[2]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[2]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server7
                .andExpect(xpath( serverTable + "//tr[3]/" + friendlyNameCol).string("server" + NYC_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[3]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[3]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[3]/" + actionCol).string(Action.ADD.getDescription()))
                //server20
                .andExpect(xpath( serverTable + "//tr[4]/" + friendlyNameCol).string("server" + GENEVA_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[4]/" + clusterCol).string(SWITZERLAND))
                .andExpect(xpath(serverTable + "//tr[4]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[4]/" + actionCol).string(Action.ADD.getDescription()));
    }

    private static void assertRemoveDnsEntryHomePageAfterDeleteResourceRecordSet(ResultActions resultActions) throws Exception {
        var expectedRowCount = 3;
        var dnsTable = "//table[@id='dnsEntries']";

        var domainStringCol = "td[1]";
        var ipCol = "td[2]";
        var serverFriendlyNameCol = "td[3]";
        var clusterNameCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(dnsTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(dnsTable + "//tr[1]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[1]/" + ipCol).string(LA_SERVER_ENTITY_1.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_1.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + clusterNameCol).string(LA_SERVER_ENTITY_1.getCluster().getName()))
                //server2
                .andExpect(xpath(dnsTable + "//tr[2]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[2]/" + ipCol).string(LA_SERVER_ENTITY_2.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_2.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + clusterNameCol).string(LA_SERVER_ENTITY_2.getCluster().getName()));
    }

    private static void assertHomePageServerEntryTable(ResultActions resultActions) throws Exception {
        var expectedRowCount = 4;
        var serverTable = "//table[@id='serverEntries']";

        var friendlyNameCol = "td[1]";
        var clusterCol = "td[2]";
        var dnsStatusCol = "td[3]";
        var actionCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(serverTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(serverTable + "//tr[1]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_1.getId()))
                .andExpect(xpath(serverTable + "//tr[1]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[1]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[1]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server2
                .andExpect(xpath(serverTable + "//tr[2]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_2.getId()))
                .andExpect(xpath(serverTable + "//tr[2]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[2]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[2]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server20
                .andExpect(xpath( serverTable + "//tr[3]/" + friendlyNameCol).string("server" + GENEVA_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[3]/" + clusterCol).string(SWITZERLAND))
                .andExpect(xpath(serverTable + "//tr[3]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[3]/" + actionCol).string(Action.ADD.getDescription()));
    }

    private static void assertHomePageDnsEntryTable(ResultActions resultActions) throws Exception {
        var expectedRowCount = 4;
        var dnsTable = "//table[@id='dnsEntries']";

        var domainStringCol = "td[1]";
        var ipCol = "td[2]";
        var serverFriendlyNameCol = "td[3]";
        var clusterNameCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(dnsTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(dnsTable + "//tr[1]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[1]/" + ipCol).string(LA_SERVER_ENTITY_1.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_1.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + clusterNameCol).string(LA_SERVER_ENTITY_1.getCluster().getName()))
                //server2
                .andExpect(xpath(dnsTable + "//tr[2]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[2]/" + ipCol).string(LA_SERVER_ENTITY_2.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_2.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + clusterNameCol).string(LA_SERVER_ENTITY_2.getCluster().getName()))
                //server20
                .andExpect(xpath( dnsTable + "//tr[3]/" + domainStringCol).string(XYZ + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[3]/" + ipCol).string(XYZ_IP))
                .andExpect(xpath(dnsTable + "//tr[3]/" + serverFriendlyNameCol).string("not found"))
                .andExpect(xpath(dnsTable + "//tr[3]/" + clusterNameCol).string("N/A"));
    }

    private static void assertAddServerEntryHomePageAfterAddResourceRecordSet(ResultActions resultActions) throws Exception {
        var expectedRowCount = 5;
        var serverTable = "//table[@id='serverEntries']";

        var friendlyNameCol = "td[1]";
        var clusterCol = "td[2]";
        var dnsStatusCol = "td[3]";
        var actionCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(serverTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(serverTable + "//tr[1]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_1.getId()))
                .andExpect(xpath(serverTable + "//tr[1]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[1]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[1]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server2
                .andExpect(xpath(serverTable + "//tr[2]/" + friendlyNameCol).string("server" + LA_SERVER_ENTITY_2.getId()))
                .andExpect(xpath(serverTable + "//tr[2]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[2]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[2]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server7
                .andExpect(xpath( serverTable + "//tr[3]/" + friendlyNameCol).string("server" + NYC_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[3]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[3]/" + dnsStatusCol).string(USA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[3]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server20
                .andExpect(xpath( serverTable + "//tr[4]/" + friendlyNameCol).string("server" + GENEVA_SERVER_ENTITY.getId()))
                .andExpect(xpath(serverTable + "//tr[4]/" + clusterCol).string(SWITZERLAND))
                .andExpect(xpath(serverTable + "//tr[4]/" + dnsStatusCol).string(SWITZERLAND + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(serverTable + "//tr[4]/" + actionCol).string(Action.REMOVE.getDescription()));
    }

    private static void assertAddDnsEntryHomePageAfterAddResourceRecordSet(ResultActions resultActions) throws Exception {
        var expectedRowCount = 5;
        var dnsTable = "//table[@id='dnsEntries']";

        var domainStringCol = "td[1]";
        var ipCol = "td[2]";
        var serverFriendlyNameCol = "td[3]";
        var clusterNameCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(dnsTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(dnsTable + "//tr[1]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[1]/" + ipCol).string(LA_SERVER_ENTITY_1.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_1.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + clusterNameCol).string(LA_SERVER_ENTITY_1.getCluster().getName()))
                //server2
                .andExpect(xpath(dnsTable + "//tr[2]/" + domainStringCol).string(LA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[2]/" + ipCol).string(LA_SERVER_ENTITY_2.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + serverFriendlyNameCol).string(LA_SERVER_ENTITY_2.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + clusterNameCol).string(LA_SERVER_ENTITY_2.getCluster().getName()))
                //server7
                .andExpect(xpath( dnsTable + "//tr[3]/" + domainStringCol).string(NYC + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[3]/" + ipCol).string(NYC_IP))
                .andExpect(xpath(dnsTable + "//tr[3]/" + serverFriendlyNameCol).string(NYC_SERVER_ENTITY.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[3]/" + clusterNameCol).string(NYC_SERVER_ENTITY.getCluster().getName()))
                //server20
                .andExpect(xpath( dnsTable + "//tr[4]/" + domainStringCol).string(GENEVA + DOT_DOMAIN_DOT_COM))
                .andExpect(xpath(dnsTable + "//tr[4]/" + ipCol).string(GENEVA_IP))
                .andExpect(xpath(dnsTable + "//tr[4]/" + serverFriendlyNameCol).string(GENEVA_SERVER_ENTITY.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[4]/" + clusterNameCol).string(GENEVA_SERVER_ENTITY.getCluster().getName()));
    }

}
