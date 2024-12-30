package org.example.dnsservice.model;

import org.example.dnsservice.entity.ServerEntity;

public record Server (
    Integer id,
    Integer clusterId,
    String clusterName,
    String clusterRegion,
    String clusterSubdomain,
    String friendlyName,
    String ipAddress
){

    public static Server of(ServerEntity entity, String regionCode){
        return new Server(
                entity.getId(),
                entity.getCluster().getId(),
                entity.getCluster().getName(),
                regionCode,
                entity.getCluster().getSubdomain(),
                entity.getFriendlyName(),
                entity.getIpString()
        );
    }
}