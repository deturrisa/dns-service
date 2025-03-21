package org.example.dnsservice.model;

import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

public record ARecord(String name, String ipAddress, String setIdentifier, Long weight, Long ttl) {

  public static ARecord of(ResourceRecordSet resourceRecordSet, ResourceRecord resourceRecord) {
    return new ARecord(
        resourceRecordSet.name(),
        resourceRecord.value(),
        resourceRecordSet.setIdentifier(),
        resourceRecordSet.weight(),
        resourceRecordSet.ttl());
  }

  public String getDomainString() {
    return setIdentifier + name.substring(name.indexOf('.'));
  }

  @Override
  public String toString() {
    return "{"
        + "\"name\":\""
        + name
        + "\","
        + "\"ipAddress\":\""
        + ipAddress
        + "\","
        + "\"setIdentifier\":\""
        + setIdentifier
        + "\","
        + "\"weight\":"
        + weight
        + ","
        + "\"ttl\":"
        + ttl
        + "}";
  }
}
