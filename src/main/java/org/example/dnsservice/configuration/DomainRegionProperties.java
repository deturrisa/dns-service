package org.example.dnsservice.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import jakarta.annotation.PostConstruct;
import java.util.List;

@PropertySource(value = "classpath:supported-domain-regions.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "supported-domain-regions")
@Configuration
public class DomainRegionProperties {
    private List<DomainRegion> domainRegions;
    private final Logger log = LoggerFactory.getLogger(DomainRegionProperties.class);

    public List<DomainRegion> getLocations() {
        return domainRegions;
    }

    public void setLocations(List<DomainRegion> domainRegions) {
        this.domainRegions = domainRegions;
    }

    @PostConstruct
    public void init() {
        log.info("Loading locations from {}", domainRegions);
    }
}

