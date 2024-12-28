package org.example.dnsservice.configuration;

import java.util.List;

public class Location {
    private String cluster;
    private List<String> domains;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }
    @Override public String toString() {
        return "Location{" + "cluster='" + cluster + '\'' + ", domains=" + domains + '}';
    }
}
