package org.example.dnsservice.service;

import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Action;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.util.TestUtil;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.List;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.*;
import static org.example.dnsservice.util.TestUtil.ARecordBuilder;
import static org.example.dnsservice.util.TestUtil.ServerBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
class ServerEntryServiceTest {


    @Mock
    private Route53RecordMapper mapper;

    @Mock
    private ServerService serverService;

    @InjectMocks
    private ServerEntryService service;

    private static final ServerBuilder swissServerBuilder = new ServerBuilder().id(1).clusterRegion(SWITZERLAND);
    public static final ServerBuilder usaServerBuilder = new ServerBuilder().id(2).clusterRegion(USA);
    public static final ServerBuilder germanyServerBuilder = new ServerBuilder().id(3).clusterRegion(GERMANY);
    public static final ServerBuilder hongKongServerBuilder = new ServerBuilder().id(4).clusterRegion(HONG_KONG);

    private final Server swissServer = swissServerBuilder.build();
    private final Server usaServer = usaServerBuilder.build();
    private final Server germanyServer = germanyServerBuilder.build();
    private final Server hongKongServer = hongKongServerBuilder.build();

    @BeforeEach
    public void setUp() {
        when(serverService.getServers()).thenReturn(
                List.of(swissServer, usaServer, germanyServer, hongKongServer)
        );
    }

    @Nested
    class AddToRotation {

        @Test
        public void testMapServersFromDb(){
            //given
            //when
            List<ServerEntry> result = service.getServerEntries();
            //then
            assertEquals(4, result.size());
            assertSwitzerland(result);
            assertUsa(result);
            assertGermany(result);
            assertHongKong(result);
        }

        @Test
        public void testMapServersAndIgnoreUnknownARecords(){
            //given
            ARecord singaporeARecord = new ARecord("Singapore" + DOT_DOMAIN_COM, "1.1.1.1", "sg");
            when(mapper.getARecords()).thenReturn(List.of(singaporeARecord));

            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(4, result.size());
            assertSwitzerland(result);
            assertUsa(result);
            assertGermany(result);
            assertHongKong(result);
        }

        private static void assertSwitzerland(List<ServerEntry> result) {
            assertEquals(1, result.get(0).serverId());
            assertEquals(SWITZERLAND, result.get(0).cluster());
            assertEquals("NONE", result.get(0).dnsStatus());
            assertEquals(Action.ADD, result.get(0).action());
        }

        private static void assertUsa(List<ServerEntry> result) {
            assertEquals(2, result.get(1).serverId());
            assertEquals(USA, result.get(1).cluster());
            assertEquals("NONE", result.get(1).dnsStatus());
            assertEquals(Action.ADD, result.get(1).action());
        }

        private static void assertGermany(List<ServerEntry> result) {
            assertEquals(3, result.get(2).serverId());
            assertEquals(GERMANY, result.get(2).cluster());
            assertEquals("NONE", result.get(2).dnsStatus());
            assertEquals(Action.ADD, result.get(2).action());
        }

        private static void assertHongKong(List<ServerEntry> result) {
            assertEquals(4, result.get(3).serverId());
            assertEquals(HONG_KONG, result.get(3).cluster());
            assertEquals("NONE", result.get(3).dnsStatus());
            assertEquals(Action.ADD, result.get(3).action());
        }
    }

    @Nested
    class RemoveFromRotation{

        private final Server swissServer =
                swissServerBuilder.clusterSubdomain(GENEVA).build();
        private final Server usaServer =
                usaServerBuilder.clusterSubdomain(LA).build();
        private final Server germanyServer =
                germanyServerBuilder.clusterSubdomain(FRANKFURT).build();
        private final Server hongKongServer =
                hongKongServerBuilder.clusterSubdomain(HONG_KONG).build();

        private final ARecord swissARecord = new ARecordBuilder()
                        .name(SWITZERLAND + DOT_DOMAIN_COM)
                            .setIdentifier(GENEVA).build();

        private final ARecord usaARecord = new ARecordBuilder()
                        .name(USA + DOT_DOMAIN_COM)
                            .setIdentifier(LA).build();

        private final ARecord germanyARecord =
                new ARecordBuilder()
                        .name(GERMANY + DOT_DOMAIN_COM)
                            .setIdentifier(FRANKFURT).build();

        private final ARecord hongKongARecord =
                new ARecordBuilder()
                        .name(HONG_KONG + DOT_DOMAIN_COM)
                            .setIdentifier(HONG_KONG).build();

        @BeforeEach
        public void setUp() {
            when(serverService.getServers()).thenReturn(
                    List.of(swissServer, usaServer, germanyServer, hongKongServer));
            when(mapper.getARecords()).thenReturn(
                    List.of(swissARecord, usaARecord, germanyARecord, hongKongARecord)
            );
        }

        @Test
        public void testMapServersFromDbAndR53(){
            //given
            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(4, result.size());
            assertSwitzerland(result);
            assertUsa(result);
            assertGermany(result);
            assertHongKong(result);
        }

        private static void assertSwitzerland(List<ServerEntry> result) {
            assertEquals(1, result.get(0).serverId());
            assertEquals(SWITZERLAND, result.get(0).cluster());
            assertEquals(SWITZERLAND + DOT_DOMAIN_COM, result.get(0).dnsStatus());
            assertEquals(Action.REMOVE, result.get(0).action());
        }

        private static void assertUsa(List<ServerEntry> result) {
            assertEquals(2, result.get(1).serverId());
            assertEquals(USA, result.get(1).cluster());
            assertEquals(USA + DOT_DOMAIN_COM, result.get(1).dnsStatus());
            assertEquals(Action.REMOVE, result.get(1).action());
        }

        private static void assertGermany(List<ServerEntry> result) {
            assertEquals(3, result.get(2).serverId());
            assertEquals(GERMANY, result.get(2).cluster());
            assertEquals(GERMANY + DOT_DOMAIN_COM, result.get(2).dnsStatus());
            assertEquals(Action.REMOVE, result.get(2).action());
        }

        private static void assertHongKong(List<ServerEntry> result) {
            assertEquals(4, result.get(3).serverId());
            assertEquals(HONG_KONG, result.get(3).cluster());
            assertEquals(HONG_KONG + DOT_DOMAIN_COM, result.get(3).dnsStatus());
            assertEquals(Action.REMOVE, result.get(3).action());
        }

    }
}
