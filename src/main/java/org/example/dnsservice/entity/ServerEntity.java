package org.example.dnsservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "server")
public class ServerEntity implements Serializable {

  @Id private int id;

  @NotNull private String friendlyName;

  @Column(name = "ip_string", nullable = false, length = 39)
  private String ipString;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cluster_id", nullable = false)
  private ClusterEntity cluster;

  public ServerEntity(int id, String friendlyName, String ipString, ClusterEntity cluster) {
    this.id = id;
    this.friendlyName = friendlyName;
    this.ipString = ipString;
    this.cluster = cluster;
  }

  public ServerEntity() {}

  public String getClusterSubdomain() {
    return this.cluster.getSubdomain();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public @NotNull String getFriendlyName() {
    return friendlyName;
  }

  public void setFriendlyName(@NotNull String friendlyName) {
    this.friendlyName = friendlyName;
  }

  public String getIpString() {
    return ipString;
  }

  public void setIpString(String ipString) {
    this.ipString = ipString;
  }

  public ClusterEntity getCluster() {
    return cluster;
  }

  public void setCluster(ClusterEntity cluster) {
    this.cluster = cluster;
  }
}
