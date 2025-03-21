package org.example.dnsservice.singleservicetests;

import java.util.concurrent.CompletableFuture;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.configuration.R53Properties;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.repository.ClusterRepository;
import org.example.dnsservice.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsResponse;

@TestPropertySource(properties = {"zonky.test.database.provider=Zonky"})
public abstract class BaseSST {

  public static final String BASE_URL = "/dns-service";
  @Autowired TestRestTemplateConfiguration.SingleServiceRestTemplate rest;

  @Autowired protected ServerRepository serverRepository;

  @Autowired protected ClusterRepository clusterRepository;

  @MockBean protected DomainRegionProperties domainRegionProperties;

  @MockBean protected R53Properties r53Properties;

  @MockBean protected Route53AsyncClient route53AsyncClient;

  protected ResultActions getDnsServiceHomePage() throws Exception {
    return rest.request()
        .withUri(BASE_URL)
        .withMethod("GET")
        .execute()
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  protected ResultActions clickRemoveFromRotationEndpoint(ServerEntity server) throws Exception {
    return rest.request()
        .withUri(BASE_URL + "/remove/" + server.getId())
        .withMethod("DELETE")
        .execute()
        .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
  }

  protected ResultActions clickAddToRotationEndpoint(ServerEntity server) throws Exception {
    return rest.request()
        .withUri(BASE_URL + "/add/" + server.getId())
        .withMethod("POST")
        .execute()
        .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
  }

  protected CompletableFuture<ChangeResourceRecordSetsResponse>
      getChangeResourceRecordSetsResponse() {
    return CompletableFuture.completedFuture(ChangeResourceRecordSetsResponse.builder().build());
  }
}
