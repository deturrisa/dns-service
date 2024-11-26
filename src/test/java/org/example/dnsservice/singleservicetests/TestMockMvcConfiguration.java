package org.example.dnsservice.singleservicetests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

@Configuration
public class TestMockMvcConfiguration {

    public static class SingleServiceMockMvc {
        private final MockMvc mockMvc;


        public SingleServiceMockMvc(MockMvc mockMvc) {
            this.mockMvc = mockMvc;
        }

        public Request request() {
            return new Request(mockMvc);
        }
    }

    public static class Request {
        private static final Logger log = LoggerFactory.getLogger(Request.class) ;
        private final MockMvc mvc;
        private String uri;
        private String method;
        private Map<String, String> headers = Collections.emptyMap();
        private String body;
        private Map<String, String> params = Collections.emptyMap();

        public Request(MockMvc mvc) {
            this.mvc = mvc;
        }

        public ResultActions execute() throws Exception {
            Map<String, String> requestHeaders = new java.util.HashMap<>(headers);
            requestHeaders.put("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<> (body, new HttpHeaders(toMultiValueMap(requestHeaders)));

            //TODO log http request

            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),uri)
                            .headers (new HttpHeaders (toMultiValueMap(requestHeaders)))
                            .queryParams(toMultiValueMap(params))
                            .content(body)
            );

            //TODO log http result actions

            return resultActions;
    }

        private static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, V> map) {
            MultiValueMap<K, V> result = new LinkedMultiValueMap<>() ;
            result.setAll (map);
            return result;
        }

    }
}
