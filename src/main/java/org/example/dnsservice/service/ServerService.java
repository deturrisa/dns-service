package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.configuration.ServerLocationProperties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.exception.ServerEntryException;
import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServerService {

    private final ServerRepository serverRepository;
    private final Route53RecordMapper mapper;
    private final ServerLocationProperties properties;

    private final Logger log = LoggerFactory.getLogger(ServerService.class);

    @Autowired
    public ServerService(ServerRepository serverRepository, Route53RecordMapper mapper, ServerLocationProperties properties) {
        this.serverRepository = serverRepository;
        this.mapper = mapper;
        this.properties = properties;
    }

    public List<ServerEntry> getServerEntries() {
        return serverRepository.findAll().stream().map( entity ->
                new ServerEntry(
                        entity.getId(),
                        getClusterByDomain(entity.getClusterSubdomain())
                )
        ).toList();
    }

    //TODO handle cluster not found
    private String getClusterByDomain(String domain){
        return properties.getLocations().stream().filter(
                location -> location.getDomains().contains(domain))
                .findFirst().get().getCluster();
    }

}
