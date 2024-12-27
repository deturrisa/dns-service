package org.example.dnsservice.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import jakarta.annotation.PostConstruct;
import java.util.List;

@PropertySource(value = "classpath:supported-locations.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "supported-locations")
@Configuration
public class ServerLocationProperties {
    private List<Location> locations;
    private final Logger log = LoggerFactory.getLogger(ServerLocationProperties.class);

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @PostConstruct
    public void init() {
        log.info("Loading locations from {}", locations);
    }
}

