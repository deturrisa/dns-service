package org.example.dnsservice.service;

import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.model.ServerEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ServerEntryService {

    private final ServerService service;
    private final Route53RecordMapper mapper;

    private final Logger log = LoggerFactory.getLogger(ServerEntryService.class);

    @Autowired
    public ServerEntryService(ServerService service, Route53RecordMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public List<ServerEntry> getServerEntries() {

        List<ServerEntry> serverEntries = new ArrayList<>(service.getServers().stream().map(server ->
                mapper.getARecords().stream()
                        .filter(aRecord -> hasMatchingIpAddress(server, aRecord))
                        .findFirst()
                        .map(aRecord -> toRemoveFromRotationServerEntry(server, aRecord))
                        .orElseGet(() -> toAddToRotationServerEntry(server))
        ).toList());

        serverEntries.sort(Comparator.comparing(ServerEntry::serverId));

        return serverEntries;
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