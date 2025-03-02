package org.example.dnsservice.service;

import org.example.dnsservice.exception.ServerValidationException;
import org.example.dnsservice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EntryStoreService {

    private final ServerService serverService;
    private final ARecordService aRecordService;

    private static final Logger log = LoggerFactory.getLogger(EntryStoreService.class);

    @Autowired
    public EntryStoreService(ServerService serverService, ARecordService aRecordService) {
        this.serverService = serverService;
        this.aRecordService = aRecordService;
    }

    public void addToRotation(Integer serverId) {
        var servers = serverService.getServers();

        var server = getServerById(serverId, servers);

        aRecordService.addServer(server);

        log.info("Successfully added to R53, A Record with IP Address: {}", server.ipAddress());
    }

    public void removeFromRotation(Integer serverId) {
        var servers = serverService.getServers();

        var server = getServerById(serverId, servers);

        aRecordService.removeServer(server);

        log.info("Successfully deleted from R53, A Record with IP Address: {}", server.ipAddress());
    }

    public EntryStore getEntryStore(){
        var servers = serverService.getServers();
        var aRecords = aRecordService.getARecords();

        log.info("Loaded {} A Records and {} Servers", aRecords.size(), servers.size());

        return getEntryStore(aRecords, servers);
    }

    private EntryStore getEntryStore(List<ARecord> aRecords, List<Server> servers) {
        return new EntryStore(
                getServerEntries(aRecords, servers),

                getDnsEntries(aRecords, servers)
        );
    }

    private List<DnsEntry> getDnsEntries(List<ARecord> aRecords, List<Server> servers) {
        var dnsEntries = aRecords.stream().map(aRecord ->
                servers.stream()
                        .filter(server -> hasMatchingIpAddress(server, aRecord))
                        .findFirst()
                        .map(server -> toDnsEntry(aRecord, server))
                        .orElseGet(() -> toNotFoundDnsEntry(aRecord))
        ).toList();

        log.info("Loaded {} DNS entries", dnsEntries.size());

        return dnsEntries;
    }

    private List<ServerEntry> getServerEntries(List<ARecord> aRecords, List<Server> servers) {
        List<ServerEntry> serverEntries = new ArrayList<>(
                servers.stream().map(server ->
                    aRecords.stream()
                            .filter(aRecord -> hasMatchingIpAddress(server, aRecord))
                            .findFirst()
                            .map(aRecord -> toRemoveFromRotationServerEntry(server, aRecord))
                            .orElseGet(() -> toAddToRotationServerEntry(server))
            ).toList()
        );

        log.info("Loaded {} servers", serverEntries.size());

        serverEntries.sort(Comparator.comparing(ServerEntry::serverId));

        return serverEntries;
    }

    private static DnsEntry toDnsEntry(ARecord aRecord, Server server) {
        log.info("Loaded: {} {}", aRecord.toString(), server.toString());
        return new DnsEntry(
                aRecord.getDomainString(),
                server.ipAddress(),
                server.friendlyName(),
                server.clusterName()
        );
    }

    private static DnsEntry toNotFoundDnsEntry(ARecord aRecord) {
        log.info("Loaded ARecord without known server in db: {}", aRecord.toString());
        return new DnsEntry(
                aRecord.getDomainString(),
                aRecord.ipAddress()
        );
    }

    private static ServerEntry toRemoveFromRotationServerEntry(Server server, ARecord aRecord) {
        log.info("Loaded server to remove from rotation: {}", server.toString());
        return new ServerEntry(server.id(), server.regionSubdomain(), aRecord.name());
    }

    private static ServerEntry toAddToRotationServerEntry(Server server) {
        log.info("Loaded server to add to rotation: {}", server.toString());
        return new ServerEntry(server.id(), server.regionSubdomain());
    }

    private static boolean hasMatchingIpAddress(Server server, ARecord aRecord) {
        return aRecord.ipAddress().equals(server.ipAddress());
    }

    private static Server getServerById(Integer serverId, List<Server> servers) {
        return servers.stream()
                .filter(it -> it.id().equals(serverId))
                .findFirst()
                .orElseThrow(() -> new ServerValidationException("Server with ID " + serverId + " not found"));
    }
}