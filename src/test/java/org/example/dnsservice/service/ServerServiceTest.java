package org.example.dnsservice.service;

import org.example.dnsservice.configuration.Location;
import org.example.dnsservice.configuration.ServerLocationProperties;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Action;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.List;
import static org.example.dnsservice.util.TestUtil.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
class ServerServiceTest {

    @Mock
    private ServerLocationProperties properties;

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private Route53RecordMapper mapper;

    @InjectMocks
    private ServerService service;

    @BeforeEach
    public void setUp() {
        when(properties.getLocations()).thenReturn(
                List.of(
                    new Location(USA, List.of(USA, NYC)),
                    new Location(SWITZERLAND, List.of(GENEVA)),
                    new Location(HONG_KONG, List.of(HONG_KONG))
                )
        );
    }

    @Nested
    class AddToRotation {

        @Test
        public void testMapServersFromDb(){
            //given
            ClusterEntity clusterEntity = new ClusterEntity(5,"Geneva", GENEVA);

            ServerEntity serverEntity = new ServerEntity(20,"my-web-1","9.9.9.9", clusterEntity);

            when(serverRepository.findAll()).thenReturn(List.of(serverEntity));

            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(20, result.get(0).serverId());
            assertEquals(SWITZERLAND, result.get(0).cluster());
            assertEquals("NONE", result.get(0).dnsStatus());
            assertEquals(Action.ADD, result.get(0).action());
        }

        @Test
        public void testMapServersAndIgnoreUnknownServers(){
            //given
            ClusterEntity genevaCluster = new ClusterEntity(5,"Geneva", GENEVA);
            ServerEntity genevaServer = new ServerEntity(20,"my-web-1","9.9.9.9", genevaCluster);

            ClusterEntity londonCluster = new ClusterEntity(6,"London","lon");
            ServerEntity londonServer = new ServerEntity(21,"foo-web","1.1.1.1", londonCluster);

            when(serverRepository.findAll()).thenReturn(List.of(genevaServer, londonServer));

            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(1, result.size());
            assertEquals(20, result.get(0).serverId());
            assertEquals(SWITZERLAND, result.get(0).cluster());
            assertEquals("NONE", result.get(0).dnsStatus());
            assertEquals(Action.ADD, result.get(0).action());
        }

        //test no matching records from r53
    }

    @Nested
    class RemoveFromRotation{

        @Test
        public void testMapServersFromDbAndR53(){
            //given
            String ipAddress = "127.0.0.1";

            ClusterEntity clusterEntity = new ClusterEntity(5,"Geneva", GENEVA);
            ServerEntity serverEntity = new ServerEntity(20,"my-web-1", ipAddress, clusterEntity);

            ARecord genevaARecord = new ARecord(SWITZERLAND + DOT_DOMAIN_COM, ipAddress, GENEVA);

            when(serverRepository.findAll()).thenReturn(List.of(serverEntity));
            when(mapper.getRoute53Records()).thenReturn(List.of(genevaARecord));

            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(20, result.get(0).serverId());
            assertEquals(SWITZERLAND, result.get(0).cluster());
            assertEquals(SWITZERLAND + DOT_DOMAIN_COM, result.get(0).dnsStatus());
            assertEquals(Action.REMOVE, result.get(0).action());
        }

    }
}
