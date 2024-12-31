package org.example.dnsservice.singleservicetests.singleservicetest;

import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.Action;
import org.example.dnsservice.singleservicetests.BaseSST;
import org.example.dnsservice.singleservicetests.ExternalPlatform;
import org.example.dnsservice.singleservicetests.SingleServiceTest;
import org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@SingleServiceTest({ExternalPlatform.POSTGRES, ExternalPlatform.REST_SERVER})
public class DnsServiceSST extends BaseSST {

    @Test
    public void testShouldRenderServerTableWithTwoRecordsToAddToRotation() throws Exception {
        //given
        ClusterEntity hkClusterEntity = new ClusterEntity(4,"Hong Kong","hongkong");
        ServerEntity hkServer1 = new ServerEntity(4,"rackspace-1","234.234.234.234",hkClusterEntity);
        ServerEntity hkServer2 = new ServerEntity(5,"rackspace-2","235.235.235.235",hkClusterEntity);

        clusterRepository.save(hkClusterEntity);
        serverRepository.saveAll(List.of(hkServer1,hkServer2));

        when(awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId())).thenReturn(
                CompletableFuture.completedFuture(ResourceRecordSetTestData.getDefaultResourceRecordSetsResponse())
        );

        //when
        //then
        getDnsServiceHomePage()
                .andExpect(xpath("//tr").nodeCount(3))
                .andExpect(xpath("//tr[1]/td[1]").string("server" + hkServer1.getId()))
                .andExpect(xpath("//tr[1]/td[2]").string(hkClusterEntity.getSubdomain()))
                .andExpect(xpath("//tr[1]/td[3]").string("NONE"))
                .andExpect(xpath("//tr[1]/td[4]").string(Action.ADD.getDescription()))
                .andExpect(xpath("//tr[2]/td[1]").string("server" + hkServer2.getId()))
                .andExpect(xpath("//tr[2]/td[2]").string(hkClusterEntity.getSubdomain()))
                .andExpect(xpath("//tr[2]/td[3]").string("NONE"))
                .andExpect(xpath("//tr[2]/td[4]").string(Action.ADD.getDescription()));


    }

}
