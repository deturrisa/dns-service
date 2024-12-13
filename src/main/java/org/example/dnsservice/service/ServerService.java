package org.example.dnsservice.service;

import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.ServerEntry;
import org.example.dnsservice.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServerService {

    private DnsR53Client dnsR53Client;
    private ServerRepository serverRepository;
    private R53Properties r53Properties;

    private final Logger log = LoggerFactory.getLogger(ServerService.class);

    @Autowired
    public ServerService(ServerRepository serverRepository,
                         DnsR53Client dnsR53Client,
                         R53Properties r53Properties
    ) {
        this.serverRepository = serverRepository;
        this.dnsR53Client = dnsR53Client;
        this.r53Properties = r53Properties;
    }

    public List<ServerEntry> getServerEntries() {
        List<ResourceRecordSet> resourceRecordSets = getAAndCNameRecords();
        List<ServerEntry> serverEntries = new ArrayList<>();

        if(resourceRecordSets.isEmpty()) {
            List<ServerEntity> serverEntities = serverRepository.findAll();
            serverEntries = serverEntities.stream().map(
                    serverEntity -> new ServerEntry(serverEntity.getId(),serverEntity.getCluster().getSubdomain())
            ).collect(Collectors.toList());
            log.info("Found {} servers to load to R53", serverEntries.size());
        }


        return serverEntries;
    }

    private List<ResourceRecordSet> getAAndCNameRecords() {

        return dnsR53Client.getResourceRecordSets(r53Properties.hostedZoneId()).join()
        .resourceRecordSets().stream()
                .filter(recordSet -> recordSet.type().equals(RRType.A) || recordSet.type().equals(RRType.CNAME))
                .collect(Collectors.toList());
    }
}
