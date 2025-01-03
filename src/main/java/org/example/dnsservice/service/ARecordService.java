package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.exception.ARecordValidationException;
import org.example.dnsservice.model.ARecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import java.util.List;
import static java.util.stream.Collectors.toList;
import static org.example.dnsservice.util.ErrorCodes.ServerErrors.ERROR_DUPLICATE_IP_ADDRESSES;

@Component
public class ARecordService {

    private final AwsR53Service awsR53Service;

    private final R53Properties r53Properties;

    private final DomainRegionProperties domainRegionProperties;

    private final Logger log = LoggerFactory.getLogger(ARecordService.class);

    @Autowired
    public ARecordService(AwsR53Service awsR53Service,
                          R53Properties r53Properties,
                          DomainRegionProperties domainRegionProperties
    ) {
        this.awsR53Service = awsR53Service;
        this.r53Properties = r53Properties;
        this.domainRegionProperties = domainRegionProperties;
    }

    public List<ARecord> getARecords() {
        List<ARecord> aRecords = getAResourceRecordSets().stream()
                .filter(this::isValidDomain).flatMap(
                    resourceRecordSet -> resourceRecordSet.resourceRecords()
                                .stream().map(
                                     resourceRecord -> ARecord.of(resourceRecordSet, resourceRecord)
                    )
                ).toList();

        validateUniqueIpAddresses(aRecords);

        return aRecords;
    }

    private boolean isValidDomain(ResourceRecordSet resourceRecordSet) {
        boolean isValidDomain = resourceRecordSet.name().matches(
                "^(?!-)[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})\\.?$"
        );

        if(!isValidDomain){
            log.warn("Domain is not in format {subdomain_1}.{subdomain_2}.{domain}.com." +
                    "Skipping mapping for resource record set {}", resourceRecordSet.setIdentifier());
        }

        return isValidDomain;
    }

    private static void validateUniqueIpAddresses(List<ARecord> records){
        if(getDistinctIpAddresses(records) != records.size()){
            throw new ARecordValidationException(ERROR_DUPLICATE_IP_ADDRESSES);
        }
    }

    private static long getDistinctIpAddresses(List<ARecord> records) {
        return records.stream().map(ARecord::ipAddress).distinct().count();
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
        return domainRegionProperties.getDomainRegions()
                .stream()
                .filter(location -> location.getRegionCode().equals(getSubdomain(resourceRecordSet)))
                .findFirst()
                .map(location -> location.getLocalityCodes().stream()
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
