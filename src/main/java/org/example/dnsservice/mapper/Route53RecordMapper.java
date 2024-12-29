package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.configuration.ServerLocationProperties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.service.AwsR53Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final ServerLocationProperties serverLocationProperties;

    private final Logger log = LoggerFactory.getLogger(Route53RecordMapper.class);

    @Autowired
    public Route53RecordMapper(AwsR53Service awsR53Service,
                               R53Properties r53Properties,
                               ServerLocationProperties serverLocationProperties
    ) {
        this.awsR53Service = awsR53Service;
        this.r53Properties = r53Properties;
        this.serverLocationProperties = serverLocationProperties;
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
                .resourceRecordSets().stream()
                .filter(this::isServerLocationSupportedARecord)
                .collect(toList());
    }

    private boolean isServerLocationSupportedARecord(ResourceRecordSet resourceRecordSet) {
        return resourceRecordSet.type().equals(RRType.A) && isServerLocationSupported(resourceRecordSet);
    }

    private boolean isServerLocationSupported(ResourceRecordSet resourceRecordSet) {
        return serverLocationProperties.getLocations()
                .stream()
                .filter(location -> location.getCluster().equals(getSubdomain(resourceRecordSet)))
                .findFirst()
                .map(location -> location.getDomains().stream()
                        .anyMatch(domain -> domain.equals(resourceRecordSet.setIdentifier())))
                .orElseGet(() -> {
                    log.warn("Resource record is not supported within the context of this application" +
                            "cluster: [{}] , subdomain: [{}] ", resourceRecordSet.name(), resourceRecordSet.setIdentifier());
                    return false;
                });
    }

    private String getSubdomain(ResourceRecordSet recordSet) {
        return recordSet.name().split("\\.")[0];
    }

    private ListResourceRecordSetsResponse getListResourceRecordSetsResponse() {
        return awsR53Service.getResourceRecordSets(
                r53Properties.hostedZoneId()
        ).join();
    }
}
