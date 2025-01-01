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

    @BeforeEach
    public void setup() {
        serverRepository.deleteAll();
        clusterRepository.deleteAll();
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
    public void testShouldRenderServerEntries() throws Exception {
        //given
        ClusterEntity laClusterEntity = new ClusterEntity(1,"Los Angeles",LA);
        ServerEntity laServer1 = new ServerEntity(1,"ubiq-1","123.123.123.123", laClusterEntity);
        ServerEntity laServer2 = new ServerEntity(2,"ubiq-2","125.125.125.125", laClusterEntity);

        ClusterEntity genevaClusterEntity = new ClusterEntity(5,"Geneva", GENEVA);
        ServerEntity genevaServer = new ServerEntity(20,"some-friendly-name-1","9.9.9.9", genevaClusterEntity);

        clusterRepository.saveAll(List.of(laClusterEntity, genevaClusterEntity));
        serverRepository.saveAll(List.of(laServer1, laServer2, genevaServer));

        ListResourceRecordSetsResponse response =
                createListResourceRecordSetsResponse(
                        List.of(
                                getNsResourceRecordSet(),
                                getSoaResourceRecordSet(),
                                getUsaAResourceRecordSet(LA,
                                        List.of("123.123.123.123", "125.125.125.125")
                                ),
                                getSwitzerlandAResourceRecordSet(GENEVA,
                                        List.of("9.9.9,9")
                                )
                        )
                );

        when(awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId())).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        //when
        //then
        int expectedRowCountWithHeader = 4;
        getDnsServiceHomePage()
                //server1
                .andExpect(xpath("//tr").nodeCount(expectedRowCountWithHeader))
                .andExpect(xpath("//tr[1]/td[1]").string("server" + laServer1.getId()))
                .andExpect(xpath("//tr[1]/td[2]").string(USA))
                .andExpect(xpath("//tr[1]/td[3]").string(USA + DOT_DOMAIN_COM))
                .andExpect(xpath("//tr[1]/td[4]").string(Action.REMOVE.getDescription()))
                //server2
                .andExpect(xpath("//tr[2]/td[1]").string("server" + laServer2.getId()))
                .andExpect(xpath("//tr[2]/td[2]").string(USA))
                .andExpect(xpath("//tr[2]/td[3]").string(USA + DOT_DOMAIN_COM))
                .andExpect(xpath("//tr[2]/td[4]").string(Action.REMOVE.getDescription()))
                //server20
                .andExpect(xpath("//tr[3]/td[1]").string("server" + genevaServer.getId()))
                .andExpect(xpath("//tr[3]/td[2]").string(SWITZERLAND))
                .andExpect(xpath("//tr[3]/td[3]").string("NONE"))
                .andExpect(xpath("//tr[3]/td[4]").string(Action.ADD.getDescription()));
    }

}
