package org.example.dnsservice.model;

public record ServerEntry(Integer serverId, String cluster, String dnsStatus) {

    private final static String DEFAULT_DNS_STATUS = "NONE";

    public String getEndpoint(){
        if(action() == Action.ADD){
            return "/add/" + serverId;
        }
        return "/remove/" + serverId;
    }

    public ServerEntry(Integer serverId, String cluster){
        this(serverId, cluster, DEFAULT_DNS_STATUS);
    }

    public Action action() {
        if(dnsStatus.equals(DEFAULT_DNS_STATUS)){
            return Action.ADD;
        }
        return Action.REMOVE;
    }
}
