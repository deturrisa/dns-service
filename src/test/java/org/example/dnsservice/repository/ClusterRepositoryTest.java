package org.example.dnsservice.repository;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.example.dnsservice.configuration.DatabaseConfiguration;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureEmbeddedDatabase
@Import({DatabaseConfiguration.class})
public class ClusterRepositoryTest {

  @Autowired ClusterRepository clusterRepository;

  @BeforeEach
  void setUp() {
    var clusterEntity = new ClusterEntity(5, "Geneva", "ge");

    var serverEntity = new ServerEntity(20, "my-web-1", "9.9.9.9", clusterEntity);

    clusterEntity.addServer(serverEntity);
    clusterRepository.save(clusterEntity);
  }

  @Test
  public void findAll_ExistingClusterAndServerEntities_ReturnsServerEntity() {
    Assertions.assertNotNull(clusterRepository.findById(5));
  }
}
