package org.example.dnsservice.model;

public record DnsEntry(
        String domainString,
        String ip,
        String serverFriendlyName,
        String clusterName
) {
    public DnsEntry(String domainString, String ip){
        this(domainString, ip, "not found", "N/A");
    }
}
