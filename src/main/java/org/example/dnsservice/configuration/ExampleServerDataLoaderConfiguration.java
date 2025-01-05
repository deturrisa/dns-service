package org.example.dnsservice.configuration;

import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.repository.ClusterRepository;
import org.example.dnsservice.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

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

        @Autowired
        private ClusterRepository clusterRepository;

        @Autowired
        private ServerRepository serverRepository;

        @PostConstruct
        public void loadData() {
            ClusterEntity laClusterEntity = new ClusterEntity(1,"Los Angeles","la");
            ClusterEntity genevaClusterEntity = new ClusterEntity(5,"Geneva","ge");

            ServerEntity laServer1 = new ServerEntity(1,"ubiq-1","123.123.123.123", laClusterEntity);
            ServerEntity laServer2 = new ServerEntity(2,"ubiq-2","125.125.125.125", laClusterEntity);
            ServerEntity genevaSever = new ServerEntity(20,"something", "192.1.1.1", genevaClusterEntity);

            clusterRepository.saveAll(List.of(laClusterEntity, genevaClusterEntity));
            serverRepository.saveAll(List.of(laServer1, laServer2, genevaSever));
        }
    }

}

