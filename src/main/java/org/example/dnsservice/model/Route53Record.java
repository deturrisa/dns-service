package org.example.dnsservice.model;

public abstract class Route53Record {

    public String resourceRecordSetName;
    public String resourceRecordValue;

    public Route53Record(String resourceRecordSetName, String resourceRecordValue) {
        this.resourceRecordSetName = resourceRecordSetName;
        this.resourceRecordValue = resourceRecordValue;
    }

    public String getResourceRecordSetName() {
        return resourceRecordSetName;
    }

    public String getResourceRecordValue() {
        return resourceRecordValue;
    }

    public abstract String getSubdomain();
}
