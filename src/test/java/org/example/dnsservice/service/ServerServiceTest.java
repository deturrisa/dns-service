package org.example.dnsservice.service;

import org.example.dnsservice.configuration.Location;
import org.example.dnsservice.configuration.ServerLocationProperties;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.mapper.Route53RecordMapper;
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
                    new Location("usa", List.of("usa","nyc")),
                    new Location("switzerland", List.of("ge")),
                    new Location("hongkong", List.of("hongkong"))
                )
        );
    }

    @Nested
    class AddToRotation {

        @Test
        public void testWhenMapperReturnsNothingThenMapServersFromDb(){
            //given
            ClusterEntity clusterEntity = new ClusterEntity(5,"Geneva","ge");

            ServerEntity serverEntity = new ServerEntity(20,"my-web-1","9.9.9.9", clusterEntity);

            when(serverRepository.findAll()).thenReturn(List.of(serverEntity));

            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(20, result.get(0).serverId());
            assertEquals("switzerland", result.get(0).cluster());
            assertEquals("NONE", result.get(0).dnsStatus());
            assertEquals(Action.ADD, result.get(0).action());
        }

        @Test
        public void testWhenMapperReturnsNothingThenMapServersFromDbAndIgnoreServerNotInProperties(){
            //given
            ClusterEntity genevaCluster = new ClusterEntity(5,"Geneva","ge");
            ServerEntity genevaServer = new ServerEntity(20,"my-web-1","9.9.9.9", genevaCluster);

            ClusterEntity londonCluster = new ClusterEntity(6,"London","lon");
            ServerEntity londonServer = new ServerEntity(21,"foo-web","1.1.1.1", londonCluster);

            when(serverRepository.findAll()).thenReturn(List.of(genevaServer, londonServer));

            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(1, result.size());
            assertEquals(20, result.get(0).serverId());
            assertEquals("switzerland", result.get(0).cluster());
            assertEquals("NONE", result.get(0).dnsStatus());
            assertEquals(Action.ADD, result.get(0).action());
        }
    }

    @Nested
    class RemoveFromRotation{

    }
}
