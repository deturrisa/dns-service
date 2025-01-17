package org.example.dnsservice.model;

import org.example.dnsservice.entity.ServerEntity;
import software.amazon.awssdk.services.route53.model.ResourceRecord;

public record Server (
    Integer id,
    Integer clusterId,
    String clusterName,
    String regionSubdomain,
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

    public ResourceRecord toResourceRecord(){
        return ResourceRecord.builder().value(ipAddress).build();
    }

    public String getResourceRecordSetName(String hostedZoneName){
        return regionSubdomain + "." + hostedZoneName;
    }

    @Override
    public String toString() {
        return "Server{" +
                "id=" + id +
                ", clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", regionSubdomain='" + regionSubdomain + '\'' +
                ", clusterSubdomain='" + clusterSubdomain + '\'' +
                ", friendlyName='" + friendlyName + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}