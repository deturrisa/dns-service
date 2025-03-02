package org.example.dnsservice.validation;

import static org.example.dnsservice.util.TestUtil.FRANKFURT;
import static org.example.dnsservice.util.TestUtil.GERMANY;
import static org.example.dnsservice.util.TestUtil.LA;
import static org.example.dnsservice.util.TestUtil.NYC;
import static org.example.dnsservice.util.TestUtil.USA;

import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

@UnitTest
public class LocalityCodesValidatorTest {

  @Mock private DomainRegionProperties domainRegionProperties;

  @Mock private ConstraintValidatorContext context;

  @InjectMocks private LocalityCodesValidator validator;

  @Test
  public void shouldReturnFalseIfDomainRegionsListIsEmpty() {
    // given
    Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(new ArrayList<>());
    var context = Mockito.mock(ConstraintValidatorContext.class);

    // when
    // then
    Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
  }

  @Test
  public void shouldReturnTrueIfLocalityCodesAreUnique() {
    // given
    var usa = new DomainRegion(USA, Set.of(LA, NYC));
    var germany = new DomainRegion(GERMANY, Set.of(FRANKFURT));

    Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(Arrays.asList(usa, germany));
    // when
    // then
    Assertions.assertTrue(validator.isValid(domainRegionProperties, context));
  }

  @Test
  public void shouldReturnFalseIfLocalityCodesAreNotUnique() {
    // given
    var validator = new LocalityCodesValidator();

    var duplicateLocalityCode = "duplicated_locality_code";

    var usa = new DomainRegion(USA, Set.of(LA, NYC, duplicateLocalityCode));
    var germany = new DomainRegion(GERMANY, Set.of(FRANKFURT, duplicateLocalityCode));

    Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(Arrays.asList(usa, germany));

    // when
    // then
    Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
  }

  @Test
  public void shouldReturnFalseIfLocalityCodesAreNullOrEmpty() {
    // given
    var validator = new LocalityCodesValidator();

    var emptyLocalityCodes = new DomainRegion(USA, Set.of());
    var germany = new DomainRegion(GERMANY, Set.of(FRANKFURT));

    Mockito.when(domainRegionProperties.getDomainRegions())
        .thenReturn(Arrays.asList(emptyLocalityCodes, germany));

    // when
    // then
    Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
  }

  @Test
  public void shouldReturnFalseIfInvalidLocalityCode() {
    // given
    var validator = new LocalityCodesValidator();

    var emptyLocalityCodes = new DomainRegion(USA, Set.of());
    var germany = new DomainRegion(GERMANY, Set.of(FRANKFURT + "something.else"));

    Mockito.when(domainRegionProperties.getDomainRegions())
        .thenReturn(Arrays.asList(emptyLocalityCodes, germany));

    // when
    // then
    Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
  }
}
