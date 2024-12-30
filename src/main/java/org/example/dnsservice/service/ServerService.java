package org.example.dnsservice.service;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.mapper.Route53RecordMapper;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServerService {

    private final ServerRepository serverRepository;
    private final Route53RecordMapper mapper;
    private final DomainRegionProperties properties;

    private final Logger log = LoggerFactory.getLogger(ServerService.class);

    @Autowired
    public ServerService(ServerRepository serverRepository, Route53RecordMapper mapper, DomainRegionProperties properties) {
        this.serverRepository = serverRepository;
        this.mapper = mapper;
        this.properties = properties;
    }

    public List<ServerEntry> getServerEntries() {
        return serverRepository.findAll().stream()
                .peek(this::logWarningIfNoMatchingCluster)
                .filter(this::hasConfiguredCluster)
                .map(this::toServerEntry)
                .toList();
    }

    private ServerEntry toServerEntry(ServerEntity entity) {
        return getARecords().stream()
                .filter(aRecord -> hasMatchingIpAddress(entity, aRecord))
                .findFirst()
                .map(aRecord -> toMatching53ServerEntry(entity, aRecord))
                .orElseGet(() -> toNonMatchingR53ServerEntry(entity));
    }

    private ServerEntry toNonMatchingR53ServerEntry(ServerEntity entity) {
        return new ServerEntry(
                entity.getId(),
                getCluster(entity)
        );
    }

    private ServerEntry toMatching53ServerEntry(ServerEntity entity, ARecord aRecord) {
        return new ServerEntry(
                entity.getId(),
                getCluster(entity),
                aRecord.name()
        );
    }

    private static boolean hasMatchingIpAddress(ServerEntity entity, ARecord aRecord) {
        return Objects.equals(entity.getClusterSubdomain(), aRecord.setIdentifier()) &&
                Objects.equals(entity.getIpString(), aRecord.ipAddress());
    }

    private List<ARecord> getARecords() {
        return mapper.getRoute53Records();
    }

    private String getCluster(ServerEntity entity) {
        return getLocationBySubdomain(entity.getClusterSubdomain())
                .map(DomainRegion::getRegionCode)
                .orElseThrow(() -> new IllegalStateException("Cluster not found for subdomain: " + entity.getClusterSubdomain()));
    }

    private boolean hasConfiguredCluster(ServerEntity entity) {
        return getLocationBySubdomain(entity.getClusterSubdomain()).isPresent();
    }

    private void logWarningIfNoMatchingCluster(ServerEntity entity) {
        if (hasNoConfiguredCluster(entity)) {
            log.warn("Cluster domain not found for server: {}", entity);
        }
    }

    private boolean hasNoConfiguredCluster(ServerEntity entity) {
        return getLocationBySubdomain(entity.getClusterSubdomain()).isEmpty();
    }

    private Optional<DomainRegion> getLocationBySubdomain(String domain) {
        return properties.getLocations().stream()
                .filter(domainRegion -> domainRegion.getLocalityCodes().contains(domain))
                .findFirst();
    }
}