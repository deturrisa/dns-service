package org.example.dnsservice.service;

import static org.example.dnsservice.util.ErrorCodes.ServerErrors.ERROR_DUPLICATE_IP_ADDRESSES;
import static org.example.dnsservice.util.ErrorCodes.ServerErrors.ERROR_INVALID_SUBDOMAIN;
import static org.example.dnsservice.util.TestUtil.GENEVA;
import static org.example.dnsservice.util.TestUtil.HONG_KONG;
import static org.example.dnsservice.util.TestUtil.LA;
import static org.example.dnsservice.util.TestUtil.NYC;
import static org.example.dnsservice.util.TestUtil.SWITZERLAND;
import static org.example.dnsservice.util.TestUtil.USA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.entity.ClusterEntity;
import org.example.dnsservice.entity.ServerEntity;
import org.example.dnsservice.exception.ServerValidationException;
import org.example.dnsservice.repository.ServerRepository;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@UnitTest
class ServerServiceTest {

  @Mock private DomainRegionProperties properties;

  @Mock private ServerRepository serverRepository;

  @InjectMocks private ServerService service;

  private final ClusterEntity clusterEntity = new ClusterEntity(5, "Geneva", GENEVA);

  private final ServerEntity serverEntity =
      new ServerEntity(2, "my-web-2", "9.9.9.9", clusterEntity);

  @Nested
  public class Filtering {

    @BeforeEach
    public void setUp() {
      when(properties.getDomainRegions())
          .thenReturn(
              List.of(
                  new DomainRegion(USA, Set.of(LA, NYC)),
                  new DomainRegion(SWITZERLAND, Set.of(GENEVA)),
                  new DomainRegion(HONG_KONG, Set.of(HONG_KONG))));
    }

    @Test
    public void testShouldFilterInvalidIpAddresses() {
      // given
      var invalidIpAddressEntity =
          new ServerEntity(1, "my-web-1", "invalid_ip_address", clusterEntity);

      when(serverRepository.findAll()).thenReturn(List.of(serverEntity, invalidIpAddressEntity));

      // when
      var result = service.getServers();

      // then
      assertEquals(1, result.size());
      assertEquals(5, result.get(0).clusterId());
      assertEquals("Geneva", result.get(0).clusterName());
      assertEquals(SWITZERLAND, result.get(0).regionSubdomain());
      assertEquals(GENEVA, result.get(0).clusterSubdomain());
      assertEquals(2, result.get(0).id());
      assertEquals("my-web-2", result.get(0).friendlyName());
      assertEquals("9.9.9.9", result.get(0).ipAddress());
    }

    @Test
    public void testShouldFilterSubdomainNotMatchingLocalityCodes() {
      // given
      var nonMatchingLocalityCodeClusterEntity = new ClusterEntity(2, "abc", "xyz");

      var nonMatchingLocalityCodeServerEntity =
          new ServerEntity(3, "my-web-1", "1.1.1.1", nonMatchingLocalityCodeClusterEntity);

      when(serverRepository.findAll())
          .thenReturn(List.of(serverEntity, nonMatchingLocalityCodeServerEntity));

      // when
      var result = service.getServers();

      // then
      assertEquals(1, result.size());
      assertEquals(5, result.get(0).clusterId());
      assertEquals("Geneva", result.get(0).clusterName());
      assertEquals(SWITZERLAND, result.get(0).regionSubdomain());
      assertEquals(GENEVA, result.get(0).clusterSubdomain());
      assertEquals(2, result.get(0).id());
      assertEquals("my-web-2", result.get(0).friendlyName());
      assertEquals("9.9.9.9", result.get(0).ipAddress());
    }
  }

  @Nested
  public class Validation {
    @Test
    public void testShouldThrowServerValidationExceptionIfDuplicateIpAddresses() {
      // given
      var duplicatedIpServerEntity =
          new ServerEntity(3, "my-web-1", serverEntity.getIpString(), clusterEntity);

      when(serverRepository.findAll()).thenReturn(List.of(serverEntity, duplicatedIpServerEntity));

      // when
      // then
      assertThrows(
          ServerValidationException.class,
          () -> service.getServers(),
          ERROR_DUPLICATE_IP_ADDRESSES);
    }

    @Test
    public void testShouldThrowServerValidationExceptionIfSpecialCharactersInSubdomain() {
      // given
      var clusterEntity = new ClusterEntity(5, "Geneva", "ge.");

      var serverEntity =
          new ServerEntity(
              3, "my-web-1", ServerServiceTest.this.serverEntity.getIpString(), clusterEntity);

      when(serverRepository.findAll()).thenReturn(List.of(serverEntity));

      // when
      // then
      assertThrows(
          ServerValidationException.class, () -> service.getServers(), ERROR_INVALID_SUBDOMAIN);
    }
  }
}
