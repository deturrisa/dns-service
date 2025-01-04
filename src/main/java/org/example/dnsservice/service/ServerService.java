package org.example.dnsservice.service;

import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.exception.ARecordValidationException;
import org.example.dnsservice.exception.ServerValidationException;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import static org.example.dnsservice.util.ErrorCodes.ServerErrors.ERROR_DUPLICATE_IP_ADDRESSES;
import static org.example.dnsservice.util.ErrorCodes.ServerErrors.ERROR_INVALID_SUBDOMAIN;
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
        List<ServerEntity> entities = repository.findAll();

        validateSubdomains(entities);
        validateUniqueIpAddresses(entities);

        return entities.stream()
                .filter(this::isValidIPAddress)
                .filter(this::isSupportedClusterSubdomain)
                .map(entity -> Server.of(entity, getRegionCode(entity)))
                .toList();
    }

    private String getRegionCode(ServerEntity entity) {
        return properties.getDomainRegions().stream()
                .filter(domainRegion ->
                        domainRegion.getLocalityCodes()
                                .contains(entity.getCluster().getSubdomain())
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Expected domain region to be present but was not found"))
                .getRegionCode();
    }

    private static void validateSubdomains(List<ServerEntity> entities){
        for(ServerEntity entity : entities){
            if(!hasValidSubdomain(entity)){
                throw new ServerValidationException(ERROR_INVALID_SUBDOMAIN);
            }
        }
    }

    private static boolean hasValidSubdomain(ServerEntity entity) {
        return entity.getCluster().getSubdomain().matches("[a-z]+");
    }

    private static void validateUniqueIpAddresses(List<ServerEntity> entities){
        if(getDistinctIpAddresses(entities) != entities.size()){
            throw new ServerValidationException(ERROR_DUPLICATE_IP_ADDRESSES);
        }
    }

    private static long getDistinctIpAddresses(List<ServerEntity> entities) {
        return entities.stream().map(ServerEntity::getIpString).distinct().count();
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
