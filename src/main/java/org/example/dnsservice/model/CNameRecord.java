package org.example.dnsservice.model;

public class CNameRecord extends Route53Record{

    private final String resourceRecordValue;

    public CNameRecord(String resourceRecordSetName, String resourceRecordValue) {
        super(resourceRecordSetName, resourceRecordValue);
        this.resourceRecordValue = resourceRecordValue;
    }

    @Override
    public String getSubdomain() {
        return resourceRecordValue.split("\\.")[0];
    }
}