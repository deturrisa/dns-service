package org.example.dnsservice.service;

import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import static org.example.dnsservice.util.IPAddressUtil.IPAddressRegexPattern.IPV4_PATTERN;
import static org.example.dnsservice.util.IPAddressUtil.IPAddressRegexPattern.IPV6_PATTERN;

@Service
public class ServerService {

    private final ServerRepository repository;
    private final DomainRegionProperties properties;

    private static final Logger log = LoggerFactory.getLogger(ServerService.class) ;
    @Autowired
    public ServerService(ServerRepository repository, DomainRegionProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public List<Server> getServers() {
        return repository.findAll().stream()
                .filter(this::isValidIPAddress)
                .filter(this::isSupportedClusterSubdomain)
                .map(Server::of).toList();
    }

    private boolean isSupportedClusterSubdomain(ServerEntity entity){
        boolean isSupportedClusterSubdomain =
                properties.getDomainRegions().stream().anyMatch(
                    domainRegion -> domainRegion.getLocalityCodes()
                            .contains(entity.getCluster().getSubdomain())
        );

        if(!isSupportedClusterSubdomain){
            log.warn("Cluster subdomain not supported for server id: {} " +
                            "This record will be ignored",
                    entity.getId()
            );
        }

        return isSupportedClusterSubdomain;
    }

    private boolean isValidIPAddress(ServerEntity entity){
        boolean isValidIPAddress = IPV4_PATTERN.matcher(entity.getIpString()).matches() ||
                IPV6_PATTERN.matcher(entity.getIpString()).matches();

        if(!isValidIPAddress){
            log.warn("Invalid IP address for server id: {}. This record will be ignored",
                    entity.getId());
        }

        return isValidIPAddress;
    }
}
