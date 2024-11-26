package org.example.dnsservice.singleservicetests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.fail;

@Configuration
public class TestRestTemplateConfiguration {

    @Bean
    public SingleServiceRestTemplate getSingleServiceRestTemplate(MockMvc mockMvc){
        return new SingleServiceRestTemplate(mockMvc);
    }

    public static class SingleServiceRestTemplate{
        private final MockMvc mvc;

        public SingleServiceRestTemplate(MockMvc mockMvc){
            this.mvc = mockMvc;
        }

        public Request request(){
            return new Request(mvc);
        }

    }

    public static class Request {
        private static final Logger log = LoggerFactory.getLogger(Request.class);
        private final MockMvc mvc;
        private String uri;
        private String method;
        private Map<String, String> headers = Collections.emptyMap();
        private String body = "";
        private Map<String, String> params = Collections.emptyMap();

        public Request(MockMvc mvc) {
            this.mvc = mvc;
        }

        public ResultActions execute(){
            Map<String, String> requestHeaders = new java.util.HashMap<>(headers);
            requestHeaders.put("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<> (body, new HttpHeaders(toMultiValueMap(requestHeaders)));

            log.info ("### Sending HTTP request: \nuri: {}\nmethod: {}\nheaders: {}" ,
                    uri, method, entity.getHeaders());

            try{
                ResultActions resultActions = mvc.perform(
                        MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),uri)
                                .headers (new HttpHeaders (toMultiValueMap(requestHeaders)))
                                .queryParams(toMultiValueMap(params))
                                .content(body)
                );

                MockHttpServletResponse response = resultActions.andReturn().getResponse();
                log.info("### Received HTTP response: \ncode: {} \nresponse : {}",
                        response.getStatus(),response);
                return resultActions;
            } catch (Exception e) {
                fail("Exception occurred during mock mvc HTTP request: " + e.getMessage());
                return null;
            }

        }

        private static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, V> map) {
            MultiValueMap<K, V> result = new LinkedMultiValueMap<>() ;
            result.setAll (map);
            return result;
        }

        public Request withMethod(String method) {
            this.method = method;
            return this;
        }

        public Request withParams(Map.Entry<String, String> params) {
            this.params.put(params.getKey(), params.getValue());
            return this;
        }

        public Request withUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Request withBody(String body) {
            this.body = body;
            return this;
        }

    }
}

