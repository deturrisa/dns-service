package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.Action;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.example.dnsservice.util.UnitTest;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.example.dnsservice.util.TestUtil.TestData;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
public class ServerServiceTest {

    @Mock
    private AwsR53Service awsR53Service;

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private R53Properties r53Properties;

    @InjectMocks
    private ServerService service;

    @Before
    public void setUp() {
        when(r53Properties.hostedZoneId()).thenReturn("hosted-zone-id");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetServerEntryWhenNoAOrCNameRecords(){
        //given
        ClusterEntity hkClusterEntity = new ClusterEntity(4,"Hong Kong","hongkong");

        ServerEntity hkServer1 = new ServerEntity(4,"rackspace-1","234.234.234.234",hkClusterEntity);
        ServerEntity hkServer2 = new ServerEntity(5,"rackspace-2","235.235.235.235",hkClusterEntity);

        when(serverRepository.findAll()).thenReturn(List.of(hkServer1,hkServer2));
        when(awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId())).thenReturn(
                CompletableFuture.completedFuture(TestData.DEFAULT_RESOURCE_RECORD_SETS_RESPONSE)
        );

        //when
        List<ServerEntry> result = service.getServerEntries();

        //then
        assertEquals(4, result.get(0).serverId());
        assertEquals("hongkong", result.get(0).cluster());
        assertEquals("NONE", result.get(0).dnsStatus());
        assertEquals(Action.ADD, result.get(0).action());

        assertEquals(5, result.get(1).serverId());
        assertEquals("hongkong", result.get(1).cluster());
        assertEquals("NONE", result.get(1).dnsStatus());
        assertEquals(Action.ADD, result.get(1).action());

    }

}
