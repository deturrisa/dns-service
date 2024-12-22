package org.example.dnsservice.model;

public record ARecord(
        String cityDomain,
        String countryDomain,
        String ipAddress
){}
