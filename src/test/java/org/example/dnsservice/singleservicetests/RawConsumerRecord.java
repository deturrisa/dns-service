package org.example.dnsservice.singleservicetests;

import java.util.Map;

public record RawConsumerRecord<V>(
        Map<String,String> headers,
        V payload
){}
