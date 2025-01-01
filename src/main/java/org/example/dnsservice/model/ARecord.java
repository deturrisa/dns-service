package org.example.dnsservice.model;

import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

public record ARecord(
        String name,
        String ipAddress,
        String setIdentifier
){

    public static ARecord of(ResourceRecordSet resourceRecordSet, ResourceRecord resourceRecord) {
        return new ARecord(
                resourceRecordSet.name(),
                resourceRecord.value(),
                resourceRecordSet.setIdentifier()
        );
    }

    public String getDomainString(){
        return setIdentifier + name.substring(name.indexOf('.'));
    }
}
