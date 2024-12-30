package org.example.dnsservice.configuration;

import org.example.dnsservice.validation.UniqueDomainRegionCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import jakarta.annotation.PostConstruct;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@PropertySource(value = "classpath:supported-domain-regions.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "supported-domain-regions")
@Configuration
@UniqueDomainRegionCheck
@Validated
public class DomainRegionProperties {
    private List<DomainRegion> domainRegions;

    private final Logger log = LoggerFactory.getLogger(DomainRegionProperties.class);

    public List<DomainRegion> getDomainRegions() {
        return domainRegions;
    }

    public void setDomainRegions(List<DomainRegion> domainRegions) {
        this.domainRegions = domainRegions;
    }

    @PostConstruct
    public void init() {
        log.info("Loading locations from {}", domainRegions);
    }
}

