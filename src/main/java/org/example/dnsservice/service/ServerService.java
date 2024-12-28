package org.example.dnsservice.service;

import org.example.dnsservice.configuration.Location;
import org.example.dnsservice.configuration.ServerLocationProperties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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
        return serverRepository.findAll().stream().peek(this::logWarningIfNoMatchingCluster)
                .filter(this::hasConfiguredCluster)
                .map(entity -> new ServerEntry(
                        entity.getId(),
                        getCluster(entity)
                ))
                .toList();
    }

    private String getCluster(ServerEntity entity) {
        return getLocationBySubdomain(entity.getClusterSubdomain()).get().getCluster();
    }

    private boolean hasConfiguredCluster(ServerEntity entity) {
        return getLocationBySubdomain(entity.getClusterSubdomain()).isPresent();
    }

    private void logWarningIfNoMatchingCluster(ServerEntity entity) {
        if (hasNoConfiguredCluster(entity)){
                log.warn("Cluster domain not found for server: {}", entity);
        }
    }

    private boolean hasNoConfiguredCluster(ServerEntity entity) {
        return getLocationBySubdomain(entity.getClusterSubdomain()).isEmpty();
    }

    //TODO handle cluster not found
    private Optional<Location> getLocationBySubdomain(String domain){
        return properties.getLocations().stream().filter(
                location -> location.getDomains().contains(domain))
                .findFirst();
    }

}
