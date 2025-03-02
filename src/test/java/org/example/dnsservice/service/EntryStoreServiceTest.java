package org.example.dnsservice.service;

import org.example.dnsservice.model.*;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.List;

import static org.example.dnsservice.util.TestUtil.*;
import static org.example.dnsservice.util.TestUtil.ARecordTestData.*;
import static org.example.dnsservice.util.TestUtil.ResourceRecordSetTestData.*;
import static org.example.dnsservice.util.TestUtil.ServerTestData.*;
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

    @Nested
    class GetTest{

        @BeforeEach
        public void setUp() {
            when(serverService.getServers()).thenReturn(
                    List.of(GENEVA_SERVER, LA_SERVER_1, FRANKFURT_SERVER, HONG_KONG_SERVER_1)
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
                    var result = service.getEntryStore().serverEntries();
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
                    var singaporeARecord = new ARecord(
                            "singapore" + DOT_DOMAIN_COM,
                            "2.2.2.2",
                            "sg",
                            10L,
                            10L
                    );

                    when(aRecordService.getARecords()).thenReturn(List.of(singaporeARecord));

                    //when
                    var result = service.getEntryStore().serverEntries();

                    //then
                    assertEquals(4, result.size());
                    assertSwitzerland(result);
                    assertUsa(result);
                    assertGermany(result);
                    assertHongKong(result);
                }

                private static void assertHongKong(List<ServerEntry> result) {
                    assertEquals(HONG_KONG_SERVER_1.id(), result.get(2).serverId());
                    assertEquals(HONG_KONG, result.get(2).cluster());
                    assertEquals("NONE", result.get(2).dnsStatus());
                    assertEquals(Action.ADD, result.get(2).action());
                }

                private static void assertUsa(List<ServerEntry> result) {
                    assertEquals(LA_SERVER_1.id(), result.get(0).serverId());
                    assertEquals(USA, result.get(0).cluster());
                    assertEquals("NONE", result.get(0).dnsStatus());
                    assertEquals(Action.ADD, result.get(0).action());
                }

                private static void assertGermany(List<ServerEntry> result) {
                    assertEquals(FRANKFURT_SERVER.id(), result.get(1).serverId());
                    assertEquals(GERMANY, result.get(1).cluster());
                    assertEquals("NONE", result.get(1).dnsStatus());
                    assertEquals(Action.ADD, result.get(1).action());
                }

                private static void assertSwitzerland(List<ServerEntry> result) {
                    assertEquals(GENEVA_SERVER.id(), result.get(3).serverId());
                    assertEquals(SWITZERLAND, result.get(3).cluster());
                    assertEquals("NONE", result.get(3).dnsStatus());
                    assertEquals(Action.ADD, result.get(3).action());
                }
            }

            @Nested
            class RemoveFromRotationTest{

                @BeforeEach
                public void setUp() {
                    when(aRecordService.getARecords()).thenReturn(
                            List.of(HONG_KONG_A_RECORD_1, FRANKFURT_A_RECORD, NYC_A_RECORD, GENEVA_A_RECORD)
                    );
                    when(serverService.getServers()).thenReturn(
                            List.of(HONG_KONG_SERVER_1, FRANKFURT_SERVER, NYC_SERVER, GENEVA_SERVER));
                }

                @Test
                public void testMapServersFromDbAndR53(){
                    //given
                    //when
                    var result = service.getEntryStore().serverEntries();

                    //then
                    assertEquals(4, result.size());
                    assertSwitzerland(result);
                    assertUsa(result);
                    assertGermany(result);
                    assertHongKong(result);
                }

                private static void assertSwitzerland(List<ServerEntry> result) {
                    assertEquals(20, result.get(3).serverId());
                    assertEquals(SWITZERLAND, result.get(3).cluster());
                    assertEquals(SWITZERLAND + DOT_DOMAIN_COM, result.get(3).dnsStatus());
                    assertEquals(Action.REMOVE, result.get(3).action());
                }

                private static void assertUsa(List<ServerEntry> result) {
                    assertEquals(6, result.get(2).serverId());
                    assertEquals(USA, result.get(2).cluster());
                    assertEquals(USA + DOT_DOMAIN_COM, result.get(2).dnsStatus());
                    assertEquals(Action.REMOVE, result.get(2).action());
                }

                private static void assertGermany(List<ServerEntry> result) {
                    assertEquals(3, result.get(0).serverId());
                    assertEquals(GERMANY, result.get(0).cluster());
                    assertEquals(GERMANY + DOT_DOMAIN_COM, result.get(0).dnsStatus());
                    assertEquals(Action.REMOVE, result.get(0).action());
                }

                private static void assertHongKong(List<ServerEntry> result) {
                    assertEquals(4, result.get(1).serverId());
                    assertEquals(HONG_KONG, result.get(1).cluster());
                    assertEquals(HONG_KONG + DOT_DOMAIN_COM, result.get(1).dnsStatus());
                    assertEquals(Action.REMOVE, result.get(1).action());
                }

            }

            @Nested
            class AddToAndRemoveFromRotationTest{

                @Test
                public void testMapAddToAndRemoveFromRotationEntries(){
                    //given
                    when(serverService.getServers()).thenReturn(
                            List.of(GENEVA_SERVER, LA_SERVER_1, LA_SERVER_2));

                    when(aRecordService.getARecords()).thenReturn(
                            List.of(LA_A_RECORD_1, LA_A_RECORD_2)
                    );

                    //when
                    var result = service.getEntryStore().serverEntries();

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
                when(serverService.getServers()).thenReturn(
                        List.of(LA_SERVER_1, LA_SERVER_2)
                );

                when(aRecordService.getARecords()).thenReturn(
                        List.of(LA_A_RECORD_1, LA_A_RECORD_2, XYZ_A_RECORD)
                );

                //when
                var result = service.getEntryStore().dnsEntries();

                //then
                assertEquals(3, result.size());

                assertEquals(LA + DOT_DOMAIN_COM, result.get(0).domainString());
                assertEquals(LA_IP_1, result.get(0).ip());
                assertEquals(LA_FRIENDLY_NAME_1, result.get(0).serverFriendlyName());
                assertEquals(LA_CLUSTER_NAME, result.get(0).clusterName());

                assertEquals(LA + DOT_DOMAIN_COM, result.get(1).domainString());
                assertEquals(LA_IP_2, result.get(1).ip());
                assertEquals(LA_FRIENDLY_NAME_2, result.get(1).serverFriendlyName());
                assertEquals(LA_CLUSTER_NAME, result.get(1).clusterName());

                assertEquals(XYZ + DOT_DOMAIN_COM, result.get(2).domainString());
                assertEquals(XYZ_IP, result.get(2).ip());
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
                when(serverService.getServers()).thenReturn(
                        List.of(GENEVA_SERVER, LA_SERVER_1, LA_SERVER_2));

                when(aRecordService.getARecords()).thenReturn(
                        List.of(LA_A_RECORD_1, LA_A_RECORD_2, XYZ_A_RECORD)
                );

                //when
                var entryStore = service.getEntryStore();
                var serverEntriesResult = entryStore.serverEntries();
                var dnsEntriesResult = entryStore.dnsEntries();

                //then

                //Server Page
                assertEquals(3, serverEntriesResult.size());
                assertUsa1(serverEntriesResult);
                assertUsa2(serverEntriesResult);
                assertSwitzerland(serverEntriesResult);

                //Dns Page
                assertEquals(3, dnsEntriesResult.size());
                assertEquals(LA + DOT_DOMAIN_COM, dnsEntriesResult.get(0).domainString());
                assertEquals(LA_IP_1, dnsEntriesResult.get(0).ip());
                assertEquals(LA_FRIENDLY_NAME_1, dnsEntriesResult.get(0).serverFriendlyName());
                assertEquals(LA_CLUSTER_NAME, dnsEntriesResult.get(0).clusterName());

                assertEquals(LA + DOT_DOMAIN_COM, dnsEntriesResult.get(1).domainString());
                assertEquals(LA_IP_2, dnsEntriesResult.get(1).ip());
                assertEquals(LA_FRIENDLY_NAME_2, dnsEntriesResult.get(1).serverFriendlyName());
                assertEquals(LA_CLUSTER_NAME, dnsEntriesResult.get(1).clusterName());

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
}
