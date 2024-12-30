package org.example.dnsservice.service;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.repository.ServerRepository;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.List;
import java.util.Set;
import static org.example.dnsservice.util.TestUtil.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
class ServerServiceTest {

    @Mock
    private DomainRegionProperties properties;

    @Mock
    private ServerRepository serverRepository;

    @InjectMocks
    private ServerService service;

    private ClusterEntity clusterEntity = new ClusterEntity(5,"Geneva", GENEVA);

    private ServerEntity serverEntity = new ServerEntity(
            2,
            "my-web-2",
            "9.9.9.9",
            clusterEntity
    );

    @BeforeEach
    public void setUp() {
        when(properties.getDomainRegions()).thenReturn(
                List.of(
                    new DomainRegion(USA, Set.of(LA, NYC)),
                    new DomainRegion(SWITZERLAND, Set.of(GENEVA)),
                    new DomainRegion(HONG_KONG, Set.of(HONG_KONG))
                )
        );
    }

    @Test
    public void testShouldFilterInvalidIpAddresses(){
        //given
        ServerEntity invalidIpAddressEntity = new ServerEntity(
                1,
                "my-web-1",
                "invalid_ip_address",
                clusterEntity
        );

        when(serverRepository.findAll()).thenReturn(List.of(serverEntity, invalidIpAddressEntity));

        //when
        List<Server> result = service.getServers();

        //then
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).clusterId());
        assertEquals("Geneva", result.get(0).clusterName());
        assertEquals(GENEVA, result.get(0).clusterSubdomain());
        assertEquals(2, result.get(0).id());
        assertEquals("my-web-2", result.get(0).friendlyName());
        assertEquals("9.9.9.9", result.get(0).ipAddress());
    }

    @Test
    public void testShouldFilterSubdomainNotMatchingLocalityCodes(){
        //given
        ClusterEntity nonMatchingLocalityCodeClusterEntity =
                new ClusterEntity(2,"no_match", "no_match");

        ServerEntity nonMatchingLocalityCodeServerEntity = new ServerEntity(
                3,
                "my-web-1",
                "1.1.1.1",
                nonMatchingLocalityCodeClusterEntity
        );

        when(serverRepository.findAll()).thenReturn(List.of(serverEntity, nonMatchingLocalityCodeServerEntity));

        //when
        List<Server> result = service.getServers();

        //then
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).clusterId());
        assertEquals("Geneva", result.get(0).clusterName());
        assertEquals(GENEVA, result.get(0).clusterSubdomain());
        assertEquals(2, result.get(0).id());
        assertEquals("my-web-2", result.get(0).friendlyName());
        assertEquals("9.9.9.9", result.get(0).ipAddress());
    }

}
