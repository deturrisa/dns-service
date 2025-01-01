package org.example.dnsservice.service;

import org.example.dnsservice.mapper.Route53RecordMapper;
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

    private final ServerService service;
    private final Route53RecordMapper mapper;

    private final Logger log = LoggerFactory.getLogger(EntryStoreService.class);

    @Autowired
    public EntryStoreService(ServerService service, Route53RecordMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public EntryStore getEntryStore(){
        List<Server> servers = service.getServers();
        List<ARecord> aRecords = mapper.getARecords();

        return new EntryStore(
                getServerEntries(aRecords,servers),
                getDnsEntries(aRecords,servers)
        );
    }

    private List<DnsEntry> getDnsEntries(List<ARecord> aRecords, List<Server> servers) {
        return aRecords.stream().map(aRecord ->
                servers.stream()
                        .filter(server ->  hasMatchingIpAddress(server, aRecord))
                        .findFirst()
                        .map(server -> toDnsEntry(aRecord, server))
                        .orElseGet(() -> toNotFoundDnsEntry(aRecord))
        ).toList();
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

        serverEntries.sort(Comparator.comparing(ServerEntry::serverId));

        return serverEntries;
    }

    private static DnsEntry toDnsEntry(ARecord aRecord, Server server) {
        return new DnsEntry(
                aRecord.getDomainString(),
                server.ipAddress(),
                server.friendlyName(),
                server.clusterName()
        );
    }

    private static DnsEntry toNotFoundDnsEntry(ARecord aRecord) {
        return new DnsEntry(
                aRecord.getDomainString(),
                aRecord.ipAddress()
        );
    }

    private static ServerEntry toRemoveFromRotationServerEntry(Server server, ARecord aRecord) {
        return new ServerEntry(server.id(), server.regionSubdomain(), aRecord.name());
    }

    private static ServerEntry toAddToRotationServerEntry(Server server) {
        return new ServerEntry(server.id(), server.regionSubdomain());
    }

    private static boolean hasMatchingIpAddress(Server server, ARecord aRecord) {
        return aRecord.ipAddress().equals(server.ipAddress());
    }
}