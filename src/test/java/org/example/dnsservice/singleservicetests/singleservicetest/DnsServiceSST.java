package org.example.dnsservice.singleservicetests.singleservicetest;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.Action;
import org.example.dnsservice.singleservicetests.BaseSST;
import org.example.dnsservice.singleservicetests.ExternalPlatform;
import org.example.dnsservice.singleservicetests.SingleServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.*;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.FRANKFURT;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@SingleServiceTest({ExternalPlatform.POSTGRES, ExternalPlatform.REST_SERVER})
public class DnsServiceSST extends BaseSST {

    @MockBean
    protected DomainRegionProperties domainRegionProperties;

    private static final ClusterEntity laClusterEntity = new ClusterEntity(1,"Los Angeles",LA);
    private static final String laIp1 = "123.123.123.123";
    private static final ServerEntity laServer1 = new ServerEntity(1,"ubiq-1", laIp1, laClusterEntity);
    private static final String laIp2 = "125.125.125.125";
    private static final ServerEntity laServer2 = new ServerEntity(2,"ubiq-2", laIp2, laClusterEntity);

    private static final ClusterEntity genevaClusterEntity = new ClusterEntity(5,"Geneva", GENEVA);
    private static final String genevaIp = "9.9.9.9";
    private static final ServerEntity genevaServer = new ServerEntity(20,"some-friendly-name-1", genevaIp, genevaClusterEntity);

    private static final ListResourceRecordSetsResponse listResourceRecordSetsResponse =
            createListResourceRecordSetsResponse(
                    List.of(
                            getNsResourceRecordSet(),
                            getSoaResourceRecordSet(),
                            getUsaAResourceRecordSet(
                                    LA,
                                    List.of(laIp1, laIp2)
                            ),
                            createAResourceRecordSet(
                                    "abc" + DOT_DOMAIN_COM,
                                    "xyz",
                                    createIpResourceRecords(List.of("5.5.5.5"))
                            )
                    )
            );

    @BeforeEach
    public void setup() {
        serverRepository.deleteAll();
        clusterRepository.deleteAll();
        when(domainRegionProperties.getDomainRegions()).thenReturn(
                List.of(
                        new DomainRegion(USA, Set.of(LA, NYC)),
                        new DomainRegion(SWITZERLAND, Set.of(GENEVA)),
                        new DomainRegion(HONG_KONG, Set.of(HONG_KONG)),
                        new DomainRegion(GERMANY, Set.of(FRANKFURT)),
                        new DomainRegion("abc", Set.of("xyz"))
                )
        );
        clusterRepository.saveAll(List.of(laClusterEntity, genevaClusterEntity));
        serverRepository.saveAll(List.of(laServer1, laServer2, genevaServer));

        when(awsR53Service.getResourceRecordSets()).thenReturn(
                CompletableFuture.completedFuture(listResourceRecordSetsResponse)
        );
    }

    @Test
    public void testShouldRenderHomePage() throws Exception {
        //given
        //when
        ResultActions response = getDnsServiceHomePage();

        //then
        assertHomePageServerEntryTable(response);
        assertHomePageDnsEntryTable(response);
    }

    @Test
    public void testShouldRenderRemoveFromRotationPage() throws Exception {
        //given
        String ipAddressToDelete = laIp1;

        ServerEntity serverToDelete = laServer1;

        ListResourceRecordSetsResponse listResourceRecordSetsResponse =
                createListResourceRecordSetsResponse(
                        List.of(
                                getNsResourceRecordSet(),
                                getSoaResourceRecordSet(),
                                getUsaAResourceRecordSet(
                                        LA,
                                        List.of(laIp2)
                                ),
                                createAResourceRecordSet(
                                        "abc" + DOT_DOMAIN_COM,
                                        "xyz",
                                        createIpResourceRecords(List.of("5.5.5.5"))
                                )
                        )
                );

        when(awsR53Service.removeResourceRecordByValue(ipAddressToDelete))
                .thenReturn(listResourceRecordSetsResponse);

        //when
        ResultActions response = clickRemoveFromRotationEndpoint(serverToDelete);

        //then
        assertRemoveServerEntryTable(response);
        assertRemoveDnsEntryTable(response);
    }

    private static void assertRemoveServerEntryTable(ResultActions resultActions) throws Exception {
        int expectedRowCount = 4;
        String serverTable = "//table[@id='serverEntries']";

        String friendlyNameCol = "td[1]";
        String clusterCol = "td[2]";
        String dnsStatusCol = "td[3]";
        String actionCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(serverTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(serverTable + "//tr[1]/" + friendlyNameCol).string("server" + laServer1.getId()))
                .andExpect(xpath(serverTable + "//tr[1]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[1]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[1]/" + actionCol).string(Action.ADD.getDescription()))
                //server2
                .andExpect(xpath(serverTable + "//tr[2]/" + friendlyNameCol).string("server" + laServer2.getId()))
                .andExpect(xpath(serverTable + "//tr[2]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[2]/" + dnsStatusCol).string(USA + DOT_DOMAIN_COM))
                .andExpect(xpath(serverTable + "//tr[2]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server20
                .andExpect(xpath( serverTable + "//tr[3]/" + friendlyNameCol).string("server" + genevaServer.getId()))
                .andExpect(xpath(serverTable + "//tr[3]/" + clusterCol).string(SWITZERLAND))
                .andExpect(xpath(serverTable + "//tr[3]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[3]/" + actionCol).string(Action.ADD.getDescription()));
    }


    private static void assertHomePageServerEntryTable(ResultActions resultActions) throws Exception {
        int expectedRowCount = 4;
        String serverTable = "//table[@id='serverEntries']";

        String friendlyNameCol = "td[1]";
        String clusterCol = "td[2]";
        String dnsStatusCol = "td[3]";
        String actionCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(serverTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(serverTable + "//tr[1]/" + friendlyNameCol).string("server" + laServer1.getId()))
                .andExpect(xpath(serverTable + "//tr[1]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[1]/" + dnsStatusCol).string(USA + DOT_DOMAIN_COM))
                .andExpect(xpath(serverTable + "//tr[1]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server2
                .andExpect(xpath(serverTable + "//tr[2]/" + friendlyNameCol).string("server" + laServer2.getId()))
                .andExpect(xpath(serverTable + "//tr[2]/" + clusterCol).string(USA))
                .andExpect(xpath(serverTable + "//tr[2]/" + dnsStatusCol).string(USA + DOT_DOMAIN_COM))
                .andExpect(xpath(serverTable + "//tr[2]/" + actionCol).string(Action.REMOVE.getDescription()))
                //server20
                .andExpect(xpath( serverTable + "//tr[3]/" + friendlyNameCol).string("server" + genevaServer.getId()))
                .andExpect(xpath(serverTable + "//tr[3]/" + clusterCol).string(SWITZERLAND))
                .andExpect(xpath(serverTable + "//tr[3]/" + dnsStatusCol).string("NONE"))
                .andExpect(xpath(serverTable + "//tr[3]/" + actionCol).string(Action.ADD.getDescription()));
    }

    private static void assertHomePageDnsEntryTable(ResultActions resultActions) throws Exception {
        int expectedRowCount = 4;
        String dnsTable = "//table[@id='dnsEntries']";

        String domainStringCol = "td[1]";
        String ipCol = "td[2]";
        String serverFriendlyNameCol = "td[3]";
        String clusterNameCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(dnsTable + "//tr").nodeCount(expectedRowCount))
                .andExpect(xpath(dnsTable + "//tr[1]/" + domainStringCol).string(LA + DOT_DOMAIN_COM))
                .andExpect(xpath(dnsTable + "//tr[1]/" + ipCol).string(laServer1.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + serverFriendlyNameCol).string(laServer1.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + clusterNameCol).string(laServer1.getCluster().getName()))
                //server2
                .andExpect(xpath(dnsTable + "//tr[2]/" + domainStringCol).string(LA + DOT_DOMAIN_COM))
                .andExpect(xpath(dnsTable + "//tr[2]/" + ipCol).string(laServer2.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + serverFriendlyNameCol).string(laServer2.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[2]/" + clusterNameCol).string(laServer2.getCluster().getName()))
                //server20
                .andExpect(xpath( dnsTable + "//tr[3]/" + domainStringCol).string("xyz" + DOT_DOMAIN_COM))
                .andExpect(xpath(dnsTable + "//tr[3]/" + ipCol).string("5.5.5.5"))
                .andExpect(xpath(dnsTable + "//tr[3]/" + serverFriendlyNameCol).string("not found"))
                .andExpect(xpath(dnsTable + "//tr[3]/" + clusterNameCol).string("N/A"));
    }


    private static void assertRemoveDnsEntryTable(ResultActions resultActions) throws Exception {
        int expectedRowCount = 3;
        String dnsTable = "//table[@id='dnsEntries']";

        String domainStringCol = "td[1]";
        String ipCol = "td[2]";
        String serverFriendlyNameCol = "td[3]";
        String clusterNameCol = "td[4]";

        resultActions
                //server1
                .andExpect(xpath(dnsTable + "//tr").nodeCount(expectedRowCount))//server2
                .andExpect(xpath(dnsTable + "//tr[1]/" + domainStringCol).string(LA + DOT_DOMAIN_COM))
                .andExpect(xpath(dnsTable + "//tr[1]/" + ipCol).string(laServer2.getIpString()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + serverFriendlyNameCol).string(laServer2.getFriendlyName()))
                .andExpect(xpath(dnsTable + "//tr[1]/" + clusterNameCol).string(laServer2.getCluster().getName()))
                //server20
                .andExpect(xpath( dnsTable + "//tr[2]/" + domainStringCol).string("xyz" + DOT_DOMAIN_COM))
                .andExpect(xpath(dnsTable + "//tr[2]/" + ipCol).string("5.5.5.5"))
                .andExpect(xpath(dnsTable + "//tr[2]/" + serverFriendlyNameCol).string("not found"))
                .andExpect(xpath(dnsTable + "//tr[2]/" + clusterNameCol).string("N/A"));
    }

}
