package org.example.dnsservice.service;

import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Action;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.model.ServerEntry;
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
            ARecord singaporeARecord = new ARecordBuilder()
                            .name("singapore" + DOT_DOMAIN_COM)
                            .setIdentifier("sg")
                            .build();

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
                swissServerBuilder.clusterSubdomain(GENEVA).ipAddress("1.1.1.1").build();
        private final Server usaServer =
                usaServerBuilder.clusterSubdomain(LA).ipAddress("2.2.2.2").build();
        private final Server germanyServer =
                germanyServerBuilder.clusterSubdomain(FRANKFURT).ipAddress("3.3.3.3").build();
        private final Server hongKongServer =
                hongKongServerBuilder.clusterSubdomain(HONG_KONG).ipAddress("4.4.4.4").build();

        private final ARecord swissARecord = new ARecordBuilder()
                        .name(SWITZERLAND + DOT_DOMAIN_COM)
                            .ipAddress(swissServer.ipAddress())
                                .setIdentifier(GENEVA).build();

        private final ARecord usaARecord = new ARecordBuilder()
                        .name(USA + DOT_DOMAIN_COM)
                            .ipAddress(usaServer.ipAddress())
                                .setIdentifier(LA).build();

        private final ARecord germanyARecord =
                new ARecordBuilder()
                        .name(GERMANY + DOT_DOMAIN_COM)
                            .ipAddress(germanyServer.ipAddress())
                                .setIdentifier(FRANKFURT).build();

        private final ARecord hongKongARecord =
                new ARecordBuilder()
                        .name(HONG_KONG + DOT_DOMAIN_COM)
                            .ipAddress(hongKongServer.ipAddress())
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

    @Nested
    class AddToAndRemoveFromRotation{

        @Test
        public void testMapAddToAndRemoveFromRotationEntries(){
            //given
            String ipAddress1 = "123.123.123.123";
            String ipAddress2 = "125.125.125.125";

            Server swissServer = new ServerBuilder()
                    .id(20)
                    .clusterRegion(SWITZERLAND)
                    .clusterSubdomain(GENEVA)
                    .build();

            Server usaServer1 = new ServerBuilder()
                    .id(1)
                    .clusterRegion(USA)
                    .clusterSubdomain(LA)
                    .ipAddress(ipAddress1)
                    .build();

            Server usaServer2 = new ServerBuilder()
                    .id(2)
                    .clusterRegion(USA)
                    .clusterSubdomain(LA)
                    .ipAddress(ipAddress2)
                    .build();

            ARecord usaARecord1 = new ARecordBuilder()
                    .name(USA + DOT_DOMAIN_COM)
                    .ipAddress(ipAddress1)
                    .setIdentifier(LA).build();

            ARecord usaARecord2 = new ARecordBuilder()
                    .name(USA + DOT_DOMAIN_COM)
                    .ipAddress(ipAddress2)
                    .setIdentifier(LA).build();

            when(serverService.getServers()).thenReturn(
                    List.of(swissServer, usaServer1, usaServer2));

            when(mapper.getARecords()).thenReturn(
                    List.of(usaARecord1, usaARecord2)
            );

            //when
            List<ServerEntry> result = service.getServerEntries();

            //then
            assertEquals(3, result.size());
            assertUsa1(result);
            assertUsa2(result);
            assertSwitzerland(result);
        }

        private static void assertUsa1(List<ServerEntry> result) {
            assertEquals(1, result.get(0).serverId());
            assertEquals(USA, result.get(0).cluster());
            assertEquals(USA + DOT_DOMAIN_COM, result.get(0).dnsStatus());
            assertEquals(Action.REMOVE, result.get(0).action());
        }

        private static void assertUsa2(List<ServerEntry> result) {
            assertEquals(2, result.get(1).serverId());
            assertEquals(USA, result.get(1).cluster());
            assertEquals(USA + DOT_DOMAIN_COM, result.get(1).dnsStatus());
            assertEquals(Action.REMOVE, result.get(1).action());
        }

        private static void assertSwitzerland(List<ServerEntry> result) {
            assertEquals(20, result.get(2).serverId());
            assertEquals(SWITZERLAND, result.get(2).cluster());
            assertEquals("NONE", result.get(2).dnsStatus());
            assertEquals(Action.ADD, result.get(2).action());
        }
    }
}
