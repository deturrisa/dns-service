package org.example.dnsservice.mapper;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.model.ARecord;
import org.example.dnsservice.model.CNameRecord;
import org.example.dnsservice.model.Route53Record;
import org.example.dnsservice.service.AwsR53Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Route53RecordMapper {

    private final AwsR53Service awsR53Service;

    private final R53Properties r53Properties;

    @Autowired
    public Route53RecordMapper(AwsR53Service awsR53Service, R53Properties r53Properties) {
        this.awsR53Service = awsR53Service;
        this.r53Properties = r53Properties;
    }

    public List<Route53Record> getRoute53Records() {
        List<ResourceRecordSet> resourceRecordSets = getListResourceRecordSetsResponse()
                .resourceRecordSets();

        Stream<ResourceRecordSet> cNameRecordSets = resourceRecordSets.stream().filter(Route53RecordMapper::isCName);
        Stream<ResourceRecordSet> aNameRecordSets = resourceRecordSets.stream().filter(Route53RecordMapper::isAName);

        List<CNameRecord> cNameRecords = cNameRecordSets.flatMap(
                resourceRecordSet -> resourceRecordSet.resourceRecords().stream().map(
                        resourceRecord -> new CNameRecord(resourceRecordSet.name(), resourceRecord.value())
                )
        ).toList();

        List<ARecord> aRecords = aNameRecordSets.flatMap(
                resourceRecordSet -> resourceRecordSet.resourceRecords().stream().map(
                        resourceRecord ->{
                            ARecord aRecord = new ARecord(resourceRecordSet.name(), resourceRecord.value());
                            if(hasMatchingSubdomain(cNameRecords, aRecord)){
                                return cNameRecords.stream().map( cNameRecord ->
                                        new ARecord(resourceRecordSet.name(), resourceRecord.value(),cNameRecord)
                                ).toList();
                            }
                            return List.of(aRecord);
                        }
                )
        ).flatMap(List::stream).toList();

        return Stream.concat(cNameRecords.stream(), aRecords.stream()).toList();

    }

    private static boolean hasMatchingSubdomain(List<CNameRecord> cNameRecords, ARecord aRecord){
        List<String> cNameSubdomains = cNameRecords.stream().map(CNameRecord::getSubdomain).toList();
        return cNameSubdomains.contains(aRecord.getSubdomain());
    }

    private static boolean isAName(ResourceRecordSet resourceRecordSet) {
        return resourceRecordSet.type() == RRType.A;
    }

    private static boolean isCName(ResourceRecordSet resourceRecordSet) {
        return resourceRecordSet.type() == RRType.CNAME;
    }

    private ListResourceRecordSetsResponse getListResourceRecordSetsResponse(){
        return awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId()).join();
    }
}
