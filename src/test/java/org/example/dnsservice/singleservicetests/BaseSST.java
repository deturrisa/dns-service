package org.example.dnsservice.singleservicetests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.model.Server;
import org.example.dnsservice.repository.ClusterRepository;
import org.example.dnsservice.repository.ServerRepository;
import org.example.dnsservice.service.AwsR53Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@TestPropertySource(properties = {"zonky.test.database.provider=Zonky"})
public abstract class BaseSST {

    @Autowired
    TestRestTemplateConfiguration.SingleServiceRestTemplate rest;

    @Autowired
    KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected ServerRepository serverRepository;

    @Autowired
    protected ClusterRepository clusterRepository;

    @Autowired
    protected R53Properties r53Properties;

    @MockBean
    protected AwsR53Service awsR53Service;

    protected ResultActions getDnsServiceHomePage() throws Exception {
        String baseUrl = "/dns-service";
        return rest.request()
                .withUri(baseUrl + "/home")
                .withMethod("GET").execute()
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    protected ResultActions clickRemoveFromRotationEndpoint(ServerEntity server) throws Exception {
        String baseUrl = "/dns-service/";
        return rest.request()
                .withUri(baseUrl + "/remove/" + server.getId())
                .withMethod("POST").execute()
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}