package org.example.dnsservice.model;

public record DnsEntry(
    String domainString,
    String ip,
    String serverFriendlyName,
    String clusterName,
    String statusColour) {
  private static final String RED = "#ffcccc";
  private static final String TRANSPARENT = "transparent";

  public DnsEntry(String domainString, String ip, String serverFriendlyName, String clusterName) {
    this(domainString, ip, serverFriendlyName, clusterName, TRANSPARENT);
  }

  public DnsEntry(String domainString, String ip) {
    this(domainString, ip, "not found", "N/A", RED);
  }

  public DnsEntry trimDnsStatus() {
    if (domainString.endsWith(".")) {
      var trimmed = domainString.substring(0, domainString.length() - 1);
      return new DnsEntry(trimmed, ip, serverFriendlyName, clusterName, statusColour);
    }
    return this;
  }
}
