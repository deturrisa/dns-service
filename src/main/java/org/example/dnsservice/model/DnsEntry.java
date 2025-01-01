package org.example.dnsservice.model;

public record DnsEntry(
        String domainString,
        String ip,
        String serverFriendlyName,
        String clusterName
) {
}
