package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.exception.ServerEntryException;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServerService {

    private final AwsR53Service awsR53Service;
    private final ServerRepository serverRepository;
    private final R53Properties r53Properties;

    private final Logger log = LoggerFactory.getLogger(ServerService.class);

    @Autowired
    public ServerService(ServerRepository serverRepository, AwsR53Service awsR53Service, R53Properties r53Properties) {
        this.serverRepository = serverRepository;
        this.awsR53Service = awsR53Service;
        this.r53Properties = r53Properties;
    }

    public List<ServerEntry> getServerEntries() {
        ListResourceRecordSetsResponse response = getListResourceRecordSetsResponse();

        List<ResourceRecordSet> aRecords = getANameRecords(response);
        List<ResourceRecordSet> cNameRecords = getCNameRecords(response);

        List<ServerEntry> serverEntries = List.of();
        List<ServerEntity> serverEntities = serverRepository.findAll();

        if(aRecords.isEmpty() && cNameRecords.isEmpty()) {
            serverEntries = serverEntities.stream().map(
                    entity -> new ServerEntry(
                            entity.getId(),
                            entity.getClusterSubdomain()
                    )
            ).collect(Collectors.toList());
            log.info("Found {} servers to load to R53", serverEntries.size());

            return serverEntries;

        } else if (aRecords.isEmpty()) {
            serverEntries = serverEntities.stream().map(
                    entity -> new ServerEntry(
                            entity.getId(),
                            getSubdomain(
                                    findResourceRecordSetByClusterSubdomain(
                                            cNameRecords,
                                            entity.getClusterSubdomain()
                                    )
                            )
                    )
            ).collect(Collectors.toList());
            log.info("Found {} servers to load to R53 with match CName records", serverEntries.size());
            return serverEntries;
        }

        return serverEntries;
    }

    private ResourceRecordSet findResourceRecordSetByClusterSubdomain(
            List<ResourceRecordSet> resourceRecordSets,
            String clusterSubdomain
    ){
        return resourceRecordSets.stream()
                .filter(
                        resourceRecordSet -> resourceRecordSet.resourceRecords().stream()
                        .anyMatch(record -> getSubdomain(record.value()).contains(clusterSubdomain)
                        )
                ).findFirst()
                .orElseThrow(
                        () ->new ServerEntryException(
                                String.format("Cannot find matching resource record from subdomain: [%s]",
                                        clusterSubdomain
                                )
                        )
                );
    }

    private List<ResourceRecordSet> getCNameRecords(ListResourceRecordSetsResponse response){
        return response.resourceRecordSets().stream().filter(recordSet -> recordSet.type().equals(RRType.CNAME))
                .collect(Collectors.toList());
    }

    private List<ResourceRecordSet> getANameRecords(ListResourceRecordSetsResponse response){
        return response.resourceRecordSets().stream().filter(recordSet -> recordSet.type().equals(RRType.A))
                .collect(Collectors.toList());
    }

    private ListResourceRecordSetsResponse getListResourceRecordSetsResponse(){
        return awsR53Service.getResourceRecordSets(r53Properties.hostedZoneId()).join();
    }

    private String getSubdomain(ResourceRecordSet resourceRecordSet){
        return getSubdomain(resourceRecordSet.name());
    }

    private String getSubdomain(String value){
        return value.split("\\.")[0];
    }
}
