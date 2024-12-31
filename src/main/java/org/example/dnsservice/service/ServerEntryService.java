package org.example.dnsservice.service;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        List<ServerEntry> serverEntries = new ArrayList<>();

        for (Server server : service.getServers()) {
            ARecord matchingRecord = null;

            for (ARecord aRecord : mapper.getARecords()) {
                 if (aRecord.setIdentifier().equals(server.clusterSubdomain())) {
                    matchingRecord = aRecord;
                    break;
                }
            }

            if (matchingRecord != null) {
                serverEntries.add(
                        new ServerEntry(
                                server.id(),
                                server.clusterRegion(),
                                matchingRecord.name()
                        )
                );
            } else {
                serverEntries.add(new ServerEntry(server.id(), server.clusterRegion()));
            }
        }

        return serverEntries;
    }

}