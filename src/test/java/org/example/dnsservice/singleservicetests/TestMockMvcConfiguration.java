package org.example.dnsservice.singleservicetests;

import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
    private static final Logger log = LoggerFactory.getLogger(Request.class);
    private final MockMvc mvc;
    private String uri;
    private String method;
    private Map<String, String> headers = Collections.emptyMap();
    private String body;
    private Map<String, String> params = Collections.emptyMap();

    public Request(MockMvc mvc) {
      this.mvc = mvc;
    }

    private static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, V> map) {
      MultiValueMap<K, V> result = new LinkedMultiValueMap<>();
      result.setAll(map);
      return result;
    }
  }
}
