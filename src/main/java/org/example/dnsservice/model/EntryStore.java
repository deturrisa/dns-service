package org.example.dnsservice.model;

import java.util.List;

public record EntryStore(List<ServerEntry> serverEntries, List<DnsEntry> dnsEntries) {}
