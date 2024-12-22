package org.example.dnsservice.model;

public class ARecord extends Route53Record {

    private final String resourceRecordSetName;

    private CNameRecord cNameRecord;

    public ARecord(String resourceRecordSetName, String resourceRecordValue) {
        super(resourceRecordSetName, resourceRecordValue);
        this.resourceRecordSetName = resourceRecordSetName;
    }

    public ARecord(String resourceRecordSetName, String resourceRecordValue, CNameRecord cNameRecord) {
        super(resourceRecordSetName, resourceRecordValue);
        this.resourceRecordSetName = resourceRecordSetName;
        this.cNameRecord = cNameRecord;
    }

    @Override
    public String getSubdomain() {
        return resourceRecordSetName.split("\\.")[0];
    }

    public void setCNameRecord(CNameRecord cNameRecord) {
        this.cNameRecord = cNameRecord;
    }

    public CNameRecord getCNameRecord() {
        return cNameRecord;
    }
}
