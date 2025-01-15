package org.example.dnsservice.service;

import org.example.dnsservice.model.*;
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
import static org.mockito.Mockito.*;

@UnitTest
class EntryStoreServiceTest {

    @Mock
    private ARecordService aRecordService;

    @Mock
    private ServerService serverService;

    @InjectMocks
    private EntryStoreService service;

    private static final ServerBuilder swissServerBuilder =
            new ServerBuilder().id(1).regionSubdomain(SWITZERLAND);
    public static final ServerBuilder usaServerBuilder =
            new ServerBuilder().id(2).regionSubdomain(USA);
    public static final ServerBuilder germanyServerBuilder =
            new ServerBuilder().id(3).regionSubdomain(GERMANY);
    public static final ServerBuilder hongKongServerBuilder =
            new ServerBuilder().id(4).regionSubdomain(HONG_KONG);

    private final Server swissServer = swissServerBuilder.build();
    private final Server usaServer = usaServerBuilder.build();
    private final Server germanyServer = germanyServerBuilder.build();
    private final Server hongKongServer = hongKongServerBuilder.build();

    @Nested
    class GetTest{

        @BeforeEach
        public void setUp() {
            when(serverService.getServers()).thenReturn(
                    List.of(swissServer, usaServer, germanyServer, hongKongServer)
            );
        }

        @Nested
        class ServerEntryTest{

            @Nested
            class AddToRotationTest {

                @Test
                public void testMapServersFromDb(){
                    //given
                    //when
                    List<ServerEntry> result = service.getEntryStore().serverEntries();
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

                    when(aRecordService.getARecords()).thenReturn(List.of(singaporeARecord));

                    //when
                    List<ServerEntry> result = service.getEntryStore().serverEntries();

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
            class RemoveFromRotationTest{

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
                    when(aRecordService.getARecords()).thenReturn(
                            List.of(swissARecord, usaARecord, germanyARecord, hongKongARecord)
                    );
                }

                @Test
                public void testMapServersFromDbAndR53(){
                    //given
                    //when
                    List<ServerEntry> result = service.getEntryStore().serverEntries();

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
            class AddToAndRemoveFromRotationTest{

                @Test
                public void testMapAddToAndRemoveFromRotationEntries(){
                    //given
                    String ipAddress1 = "123.123.123.123";
                    String ipAddress2 = "125.125.125.125";

                    Server swissServer = new ServerBuilder()
                            .id(20)
                            .regionSubdomain(SWITZERLAND)
                            .clusterSubdomain(GENEVA)
                            .build();

                    Server usaServer1 = new ServerBuilder()
                            .id(1)
                            .regionSubdomain(USA)
                            .clusterSubdomain(LA)
                            .ipAddress(ipAddress1)
                            .build();

                    Server usaServer2 = new ServerBuilder()
                            .id(2)
                            .regionSubdomain(USA)
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

                    when(aRecordService.getARecords()).thenReturn(
                            List.of(usaARecord1, usaARecord2)
                    );

                    //when
                    List<ServerEntry> result = service.getEntryStore().serverEntries();

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

        @Nested
        class DnsEntryTest {

            @Test
            public void testMapPublishedDnsEntries(){
                //given
                String losAngeles = "Los Angeles";
                String laIpAddress1 = "123.123.123.123";
                String laIpAddress2 = "125.125.125.125";
                String laFriendlyName1 = "ubiq-1";
                String laFriendlyName2 = "ubiq-2";

                Server laServer1  =
                        new ServerBuilder()
                                .id(1)
                                .clusterSubdomain(LA)
                                .regionSubdomain(USA)
                                .ipAddress(laIpAddress1)
                                .friendlyName(laFriendlyName1)
                                .clusterName(losAngeles)
                                .build();

                Server laServer2  =
                        new ServerBuilder()
                                .id(2)
                                .clusterSubdomain(LA)
                                .regionSubdomain(USA)
                                .ipAddress(laIpAddress2)
                                .friendlyName(laFriendlyName2)
                                .clusterName(losAngeles)
                                .build();

                ARecord laARecord1 = new ARecordBuilder()
                        .name(USA + DOT_DOMAIN_COM)
                        .ipAddress(laIpAddress1)
                        .setIdentifier(LA)
                        .build();

                ARecord laARecord2 = new ARecordBuilder()
                        .name(USA + DOT_DOMAIN_COM)
                        .ipAddress(laIpAddress2)
                        .setIdentifier(LA)
                        .build();

                ARecord xyzARecord = new ARecordBuilder()
                        .name("abc" + DOT_DOMAIN_COM)
                        .ipAddress("5.5.5.5")
                        .setIdentifier("xyz")
                        .build();

                when(serverService.getServers()).thenReturn(
                        List.of(laServer1, laServer2)
                );

                when(aRecordService.getARecords()).thenReturn(
                        List.of(laARecord1, laARecord2, xyzARecord)
                );

                //when
                List<DnsEntry> result = service.getEntryStore().dnsEntries();

                //then
                assertEquals(3, result.size());

                assertEquals(LA + DOT_DOMAIN_COM, result.get(0).domainString());
                assertEquals(laIpAddress1, result.get(0).ip());
                assertEquals(laFriendlyName1, result.get(0).serverFriendlyName());
                assertEquals(losAngeles, result.get(0).clusterName());

                assertEquals(LA + DOT_DOMAIN_COM, result.get(1).domainString());
                assertEquals(laIpAddress2, result.get(1).ip());
                assertEquals(laFriendlyName2, result.get(1).serverFriendlyName());
                assertEquals(losAngeles, result.get(1).clusterName());

                assertEquals("xyz" + DOT_DOMAIN_COM, result.get(2).domainString());
                assertEquals("5.5.5.5", result.get(2).ip());
                assertEquals("not found", result.get(2).serverFriendlyName());
                assertEquals("N/A", result.get(2).clusterName());
                assertEquals("#ffcccc", result.get(2).statusColour());
            }
        }

        @Nested
        class ServerAndDnsEntryTest {

            @Test
            public void testMapServerAndDnsEntries(){
                //given
                String losAngeles = "Los Angeles";
                String laIpAddress1 = "123.123.123.123";
                String laIpAddress2 = "125.125.125.125";
                String laFriendlyName1 = "ubiq-1";
                String laFriendlyName2 = "ubiq-2";

                Server swissServer = new ServerBuilder()
                        .id(20)
                        .regionSubdomain(SWITZERLAND)
                        .clusterSubdomain(GENEVA)
                        .build();

                Server usaServer1 = new ServerBuilder()
                        .id(1)
                        .regionSubdomain(USA)
                        .clusterSubdomain(LA)
                        .clusterName(losAngeles)
                        .ipAddress(laIpAddress1)
                        .friendlyName(laFriendlyName1)
                        .build();

                Server usaServer2 = new ServerBuilder()
                        .id(2)
                        .regionSubdomain(USA)
                        .clusterSubdomain(LA)
                        .clusterName(losAngeles)
                        .ipAddress(laIpAddress2)
                        .friendlyName(laFriendlyName2)
                        .build();

                ARecord usaARecord1 = new ARecordBuilder()
                        .name(USA + DOT_DOMAIN_COM)
                        .ipAddress(laIpAddress1)
                        .setIdentifier(LA).build();

                ARecord usaARecord2 = new ARecordBuilder()
                        .name(USA + DOT_DOMAIN_COM)
                        .ipAddress(laIpAddress2)
                        .setIdentifier(LA).build();

                ARecord xyzARecord = new ARecordBuilder()
                        .name("abc" + DOT_DOMAIN_COM)
                        .ipAddress("5.5.5.5")
                        .setIdentifier("xyz")
                        .build();

                when(serverService.getServers()).thenReturn(
                        List.of(swissServer, usaServer1, usaServer2));

                when(aRecordService.getARecords()).thenReturn(
                        List.of(usaARecord1, usaARecord2, xyzARecord)
                );

                //when
                EntryStore entryStore = service.getEntryStore();
                List<ServerEntry> serverEntriesResult = entryStore.serverEntries();
                List<DnsEntry> dnsEntriesResult = entryStore.dnsEntries();

                //then

                //Server Page
                assertEquals(3, serverEntriesResult.size());
                assertUsa1(serverEntriesResult);
                assertUsa2(serverEntriesResult);
                assertSwitzerland(serverEntriesResult);

                //Dns Page
                assertEquals(3, dnsEntriesResult.size());
                assertEquals(LA + DOT_DOMAIN_COM, dnsEntriesResult.get(0).domainString());
                assertEquals(laIpAddress1, dnsEntriesResult.get(0).ip());
                assertEquals(laFriendlyName1, dnsEntriesResult.get(0).serverFriendlyName());
                assertEquals(losAngeles, dnsEntriesResult.get(0).clusterName());

                assertEquals(LA + DOT_DOMAIN_COM, dnsEntriesResult.get(1).domainString());
                assertEquals(laIpAddress2, dnsEntriesResult.get(1).ip());
                assertEquals(laFriendlyName2, dnsEntriesResult.get(1).serverFriendlyName());
                assertEquals(losAngeles, dnsEntriesResult.get(1).clusterName());

                assertEquals("xyz" + DOT_DOMAIN_COM, dnsEntriesResult.get(2).domainString());
                assertEquals("5.5.5.5", dnsEntriesResult.get(2).ip());
                assertEquals("not found", dnsEntriesResult.get(2).serverFriendlyName());
                assertEquals("N/A", dnsEntriesResult.get(2).clusterName());
                assertEquals("#ffcccc", dnsEntriesResult.get(2).statusColour());
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

    @Nested
    class PostTest{

        @Test
        public void testShouldRemoveFromRotation(){
            //given
            String losAngeles = "Los Angeles";
            String ipAddressToRemove = "123.123.123.123";
            String laFriendlyName1 = "ubiq-1";
            String laFriendlyName2 = "ubiq-2";

            String laIpAddress = "125.125.125.125";
            Integer idToRemove = 1;

            Server swissServer = new ServerBuilder()
                    .id(20)
                    .regionSubdomain(SWITZERLAND)
                    .clusterSubdomain(GENEVA)
                    .build();

            Server usaServer1 = new ServerBuilder()
                    .id(idToRemove)
                    .regionSubdomain(USA)
                    .clusterSubdomain(LA)
                    .clusterName(losAngeles)
                    .ipAddress(ipAddressToRemove)
                    .friendlyName(laFriendlyName1)
                    .build();

            Server usaServer2 = new ServerBuilder()
                    .id(2)
                    .regionSubdomain(USA)
                    .clusterSubdomain(LA)
                    .clusterName(losAngeles)
                    .ipAddress(laIpAddress)
                    .friendlyName(laFriendlyName2)
                    .build();

            ARecord usaARecord = new ARecordBuilder()
                    .name(USA + DOT_DOMAIN_COM)
                    .ipAddress(laIpAddress)
                    .setIdentifier(LA).build();

            ARecord xyzARecord = new ARecordBuilder()
                    .name("abc" + DOT_DOMAIN_COM)
                    .ipAddress("5.5.5.5")
                    .setIdentifier("xyz")
                    .build();

            when(serverService.getServers()).thenReturn(
                    List.of(swissServer, usaServer1, usaServer2));

            when(aRecordService.deleteByIpAddress(ipAddressToRemove)).thenReturn(
                    List.of(usaARecord, xyzARecord)
            );

            //when
            EntryStore entryStore = service.removeFromRotation(idToRemove);
            List<ServerEntry> serverEntriesResult = entryStore.serverEntries();
            List<DnsEntry> dnsEntriesResult = entryStore.dnsEntries();

            //then

            //Server Page
            assertEquals(3, serverEntriesResult.size());
            assertUsaAfterRemove(serverEntriesResult);
            assertSwitzerland(serverEntriesResult);

            //Dns Page
            assertEquals(2, dnsEntriesResult.size());
            assertEquals(LA + DOT_DOMAIN_COM, dnsEntriesResult.get(0).domainString());
            assertEquals(laIpAddress, dnsEntriesResult.get(0).ip());
            assertEquals(laFriendlyName2, dnsEntriesResult.get(0).serverFriendlyName());
            assertEquals(losAngeles, dnsEntriesResult.get(0).clusterName());

            assertEquals("xyz" + DOT_DOMAIN_COM, dnsEntriesResult.get(1).domainString());
            assertEquals("5.5.5.5", dnsEntriesResult.get(1).ip());
            assertEquals("not found", dnsEntriesResult.get(1).serverFriendlyName());
            assertEquals("N/A", dnsEntriesResult.get(1).clusterName());
            assertEquals("#ffcccc", dnsEntriesResult.get(1).statusColour());
        }

        @Test
        public void testShouldAddToRotation(){
            //given
            String losAngeles = "Los Angeles";
            String ipAddressToAdd = "123.123.123.123";
            String laFriendlyName1 = "ubiq-1";
            String laFriendlyName2 = "ubiq-2";

            String laIpAddress = "125.125.125.125";
            Integer idToAdd = 1;

            Server swissServer = new ServerBuilder()
                    .id(20)
                    .regionSubdomain(SWITZERLAND)
                    .clusterSubdomain(GENEVA)
                    .build();

            Server serverToAdd = new ServerBuilder()
                    .id(idToAdd)
                    .regionSubdomain(USA)
                    .clusterSubdomain(LA)
                    .clusterName(losAngeles)
                    .ipAddress(ipAddressToAdd)
                    .friendlyName(laFriendlyName1)
                    .build();

            Server usaServer2 = new ServerBuilder()
                    .id(2)
                    .regionSubdomain(USA)
                    .clusterSubdomain(LA)
                    .clusterName(losAngeles)
                    .ipAddress(laIpAddress)
                    .friendlyName(laFriendlyName2)
                    .build();

            ARecord usaARecord = new ARecordBuilder()
                    .name(USA + DOT_DOMAIN_COM)
                    .ipAddress(laIpAddress)
                    .setIdentifier(LA).build();

            ARecord aRecordToAdd = new ARecordBuilder()
                    .name(USA + DOT_DOMAIN_COM)
                    .ipAddress(ipAddressToAdd)
                    .setIdentifier(LA).build();

            ARecord xyzARecord = new ARecordBuilder()
                    .name("abc" + DOT_DOMAIN_COM)
                    .ipAddress("5.5.5.5")
                    .setIdentifier("xyz")
                    .build();

            when(serverService.getServers()).thenReturn(
                    List.of(swissServer, serverToAdd, usaServer2));

            when(aRecordService.addServer(serverToAdd)).thenReturn(
                    List.of(aRecordToAdd, usaARecord, xyzARecord)
            );

            //when
            EntryStore entryStore = service.addToRotation(idToAdd);
            List<ServerEntry> serverEntriesResult = entryStore.serverEntries();
            List<DnsEntry> dnsEntriesResult = entryStore.dnsEntries();

            //then

            //Server Page
            assertEquals(3, serverEntriesResult.size());

            assertEquals(1, serverEntriesResult.get(0).serverId());
            assertEquals(USA, serverEntriesResult.get(0).cluster());
            assertEquals(USA + DOT_DOMAIN_COM, serverEntriesResult.get(0).dnsStatus());
            assertEquals(Action.REMOVE, serverEntriesResult.get(0).action());

            //Dns Page
            assertEquals(3, dnsEntriesResult.size());
            assertEquals(LA + DOT_DOMAIN_COM, dnsEntriesResult.get(0).domainString());
            assertEquals(ipAddressToAdd, dnsEntriesResult.get(0).ip());
            assertEquals(laFriendlyName1, dnsEntriesResult.get(0).serverFriendlyName());
            assertEquals(losAngeles, dnsEntriesResult.get(0).clusterName());

            assertEquals(LA + DOT_DOMAIN_COM, dnsEntriesResult.get(1).domainString());
            assertEquals(laIpAddress, dnsEntriesResult.get(1).ip());
            assertEquals(laFriendlyName2, dnsEntriesResult.get(1).serverFriendlyName());
            assertEquals(losAngeles, dnsEntriesResult.get(1).clusterName());

            assertEquals("xyz" + DOT_DOMAIN_COM, dnsEntriesResult.get(2).domainString());
            assertEquals("5.5.5.5", dnsEntriesResult.get(2).ip());
            assertEquals("not found", dnsEntriesResult.get(2).serverFriendlyName());
            assertEquals("N/A", dnsEntriesResult.get(2).clusterName());
            assertEquals("#ffcccc", dnsEntriesResult.get(2).statusColour());
        }

        private static void assertUsaAfterRemove(List<ServerEntry> result) {
            assertEquals(1, result.get(0).serverId());
            assertEquals(USA, result.get(0).cluster());
            assertEquals("NONE", result.get(0).dnsStatus());
            assertEquals(Action.ADD, result.get(0).action());

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
