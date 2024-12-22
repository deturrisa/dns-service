package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.SupportedCountryCode;
import org.example.dnsservice.service.AwsR53Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Route53RecordMapper {

    private final AwsR53Service awsR53Service;

    private final R53Properties r53Properties;

    //TODO resolve this programatically
    private static final String DOMAIN_COM = "domain.com.";

    @Autowired
    public Route53RecordMapper(AwsR53Service awsR53Service, R53Properties r53Properties) {
        this.awsR53Service = awsR53Service;
        this.r53Properties = r53Properties;
    }

    public List<ARecord> getRoute53Records() {
        return getANameRecords().stream().flatMap(
            resourceRecordSet -> resourceRecordSet.resourceRecords().stream().map(
                    resourceRecord -> createARecord(resourceRecordSet, resourceRecord)
            )
        ).toList();
    }

    private static ARecord createARecord(ResourceRecordSet resourceRecordSet, ResourceRecord resourceRecord) {
        return new ARecord(
                resourceRecordSet.name(),
                getCountryDomain(resourceRecordSet),
                resourceRecord.value()
        );
    }

    private static String getCountryDomain(ResourceRecordSet resourceRecordSet) {
        return SupportedCountryCode
                .valueOf(
                        resourceRecordSet.geoLocation().countryCode()
                ).getSubdomain().concat(".").concat(DOMAIN_COM);
    }


    private List<ResourceRecordSet> getANameRecords(){
        return awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId()).join().resourceRecordSets().stream().filter(recordSet -> recordSet.type().equals(RRType.A))
                .collect(Collectors.toList());
    }
}
