package org.example.dnsservice.configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.repository.ClusterRepository;
import org.example.dnsservice.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Configuration
public class ExampleServerDataLoaderConfiguration {

  @Bean
  @Profile("local")
  public LocalDataLoader localDataLoader() {
    return new LocalDataLoader();
  }

  @Component
  @Profile("local")
  public static class LocalDataLoader {

    @Autowired private ClusterRepository clusterRepository;

    @Autowired private ServerRepository serverRepository;

    @PostConstruct
    public void loadData() {
      var laClusterEntity = new ClusterEntity(1, "Los Angeles", "la");
      var genevaClusterEntity = new ClusterEntity(5, "Geneva", "ge");

      var laServer1 = new ServerEntity(1, "ubiq-1", "123.123.123.123", laClusterEntity);
      var laServer2 = new ServerEntity(2, "ubiq-2", "125.125.125.125", laClusterEntity);
      var genevaSever = new ServerEntity(20, "something", "192.1.1.1", genevaClusterEntity);

      clusterRepository.saveAll(List.of(laClusterEntity, genevaClusterEntity));
      serverRepository.saveAll(List.of(laServer1, laServer2, genevaSever));
    }
  }
}
