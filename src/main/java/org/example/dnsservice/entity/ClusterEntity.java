package org.example.dnsservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "cluster")
public class ClusterEntity implements Serializable {

  @Id private int id;

  @NotNull private String name;

  @Column(name = "subdomain", nullable = false)
  private String subdomain;

  @OneToMany(
      mappedBy = "cluster",
      cascade = {CascadeType.ALL},
      fetch = FetchType.LAZY)
  @JsonManagedReference
  private List<ServerEntity> servers = new LinkedList<>();

  public ClusterEntity(int id, String name, String subdomain) {
    this.id = id;
    this.name = name;
    this.subdomain = subdomain;
  }

  public ClusterEntity() {}

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public @NotNull String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  public String getSubdomain() {
    return subdomain;
  }

  public void addServer(ServerEntity server) {
    this.servers.add(server);
    server.setCluster(this);
  }
}
