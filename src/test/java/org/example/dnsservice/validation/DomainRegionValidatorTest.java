package org.example.dnsservice.validation;

import static org.example.dnsservice.util.TestUtil.FRANKFURT;
import static org.example.dnsservice.util.TestUtil.GERMANY;
import static org.example.dnsservice.util.TestUtil.LA;
import static org.example.dnsservice.util.TestUtil.NYC;
import static org.example.dnsservice.util.TestUtil.USA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@UnitTest
class DomainRegionValidatorTest {

  private DomainRegionValidator validator;

  @Mock private ConstraintValidatorContext context;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    validator = new DomainRegionValidator();
  }

  @Test
  public void testValidDomainRegionProperties_NoDuplicates() {
    // given
    var usa = new DomainRegion(USA, Set.of(LA, NYC));
    var germany = new DomainRegion(GERMANY, Set.of(FRANKFURT));

    var properties = new DomainRegionProperties();
    properties.setDomainRegions(Arrays.asList(germany, usa));

    // when
    var result = validator.isValid(properties, context);

    // then
    assertTrue(result);
  }

  @Test
  public void testInvalidDomainRegionProperties_WithDuplicates() {
    // given
    var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    var usa1 = new DomainRegion(USA, Set.of(LA, NYC));
    var usa2 = new DomainRegion(USA, Set.of(LA, NYC));
    var properties = new DomainRegionProperties();
    properties.setDomainRegions(Arrays.asList(usa1, usa2));

    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

    // when
    var result = validator.isValid(properties, context);

    // then
    verify(context, times(1)).disableDefaultConstraintViolation();
    assertFalse(result);
    verify(context, times(1)).buildConstraintViolationWithTemplate("Duplicate region codes found");
  }

  @Test
  public void testInvalidDomainRegions() {
    // given
    var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    var usa1 = new DomainRegion(USA + ".abc", Set.of(LA, NYC));
    var usa2 = new DomainRegion(USA, Set.of(LA, NYC));
    var properties = new DomainRegionProperties();
    properties.setDomainRegions(Arrays.asList(usa1, usa2));

    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

    // when
    var result = validator.isValid(properties, context);

    // then
    verify(context, times(1)).disableDefaultConstraintViolation();
    assertFalse(result);
    verify(context, times(1))
        .buildConstraintViolationWithTemplate("Invalid character found in region codes");
  }
}
