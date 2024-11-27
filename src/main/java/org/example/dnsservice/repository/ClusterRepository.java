package org.example.dnsservice.repository;

import org.example.dnsservice.entity.ClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterRepository extends JpaRepository<ClusterEntity, Integer> {

}