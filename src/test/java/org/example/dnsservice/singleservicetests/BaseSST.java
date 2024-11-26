package org.example.dnsservice.singleservicetests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"zonky.test.database.provider=Zonky"})
public abstract class BaseSST {

    @Autowired
    TestRestTemplateConfiguration.SingleServiceRestTemplate rest;

    @Autowired
    KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

}