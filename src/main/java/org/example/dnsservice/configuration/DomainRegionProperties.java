package org.example.dnsservice.configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.example.dnsservice.validation.DomainRegionCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@PropertySource(
    value = "classpath:supported-domain-regions.yml",
    factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "supported-domain-regions")
@Configuration
@DomainRegionCheck
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
