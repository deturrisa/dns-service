package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.SupportedCountryCode;
import org.example.dnsservice.service.AwsR53Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

import java.util.List;
import java.util.stream.Collectors;

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
        ListResourceRecordSetsResponse response = getListResourceRecordSetsResponse();

        return getAResourceRecordSets(response).stream().flatMap(
            resourceRecordSet -> resourceRecordSet.resourceRecords().stream().map(
                    resourceRecord -> new ARecord(
                            resourceRecordSet.name(),
                            getCountryDomain(resourceRecordSet,response),
                            resourceRecord.value(),
                            resourceRecordSet.setIdentifier()
                    )
            )
        ).toList();
    }

    private static String getCountryDomain(
            ResourceRecordSet resourceRecordSet,
            ListResourceRecordSetsResponse response
    ) {
        String hostedZoneName = getHostedZoneName(response);

        return SupportedCountryCode
                .valueOf(
                        resourceRecordSet.geoLocation().countryCode()
                ).getSubdomain().concat(".").concat(hostedZoneName);
    }

    private static String getHostedZoneName(ListResourceRecordSetsResponse response) {
        //TODO handle exception
        return response.resourceRecordSets().stream().filter(
                recordSet -> recordSet.type().equals(RRType.SOA)
        ).map(ResourceRecordSet::name).findFirst().orElse(null);
    }


    private List<ResourceRecordSet> getAResourceRecordSets(ListResourceRecordSetsResponse response){
        return response
                .resourceRecordSets().stream().filter(
                        recordSet -> recordSet.type().equals(RRType.A)
                )
                .collect(Collectors.toList());
    }

    private ListResourceRecordSetsResponse getListResourceRecordSetsResponse() {
        return awsR53Service.getResourceRecordSets(
                r53Properties.hostedZoneId()
        ).join();
    }
}
