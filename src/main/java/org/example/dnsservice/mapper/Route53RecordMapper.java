package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.service.AwsR53Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Component
public class Route53RecordMapper {

    private final AwsR53Service awsR53Service;

    private final R53Properties r53Properties;

    @Autowired
    public Route53RecordMapper(AwsR53Service awsR53Service, R53Properties r53Properties) {
        this.awsR53Service = awsR53Service;
        this.r53Properties = r53Properties;
    }

    public List<ARecord> getRoute53Records() {
        return getAResourceRecordSets().stream().flatMap(
            resourceRecordSet -> resourceRecordSet.resourceRecords().stream().map(
                    resourceRecord -> ARecord.of(resourceRecordSet,resourceRecord)
            )
        ).toList();
    }

    private List<ResourceRecordSet> getAResourceRecordSets(){
        return getListResourceRecordSetsResponse()
                .resourceRecordSets().stream().filter(
                        recordSet -> recordSet.type().equals(RRType.A)
                )
                .collect(toList());
    }

    private ListResourceRecordSetsResponse getListResourceRecordSetsResponse() {
        return awsR53Service.getResourceRecordSets(
                r53Properties.hostedZoneId()
        ).join();
    }
}
