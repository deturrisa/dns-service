package org.example.dnsservice.model;

import org.example.dnsservice.entity.ServerEntity;

public record Server (
    Integer id,
    Integer clusterId,
    String clusterName,
    String clusterSubdomain,
    String friendlyName,
    String ipAddress
){

    public static Server of(ServerEntity entity){
        return new Server(
                entity.getId(),
                entity.getCluster().getId(),
                entity.getCluster().getName(),
                entity.getCluster().getSubdomain(),
                entity.getFriendlyName(),
                entity.getIpString()
        );
    }
}