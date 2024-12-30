package org.example.dnsservice.validation;

import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import static org.example.dnsservice.util.TestUtil.TestData.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
class DomainRegionValidatorTest {

    private DomainRegionValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        validator = new DomainRegionValidator();
    }

    @Test
    public void testValidDomainRegionProperties_NoDuplicates() {
        // given
        DomainRegion usa = new DomainRegion(USA, Set.of(LA,NYC));
        DomainRegion germany = new DomainRegion(GERMANY, Set.of(FRANKFURT));

        DomainRegionProperties properties = new DomainRegionProperties();
        properties.setDomainRegions(Arrays.asList(germany, usa));

        // when
        boolean result = validator.isValid(properties, context);

        // then
        assertTrue(result);
    }

    @Test
    public void testInvalidDomainRegionProperties_WithDuplicates() {
        // given
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        DomainRegion usa1 = new DomainRegion(USA, Set.of(LA,NYC));
        DomainRegion usa2 = new DomainRegion(USA, Set.of(LA,NYC));
        DomainRegionProperties properties = new DomainRegionProperties();
        properties.setDomainRegions(Arrays.asList(usa1, usa2));

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        // when
        boolean result = validator.isValid(properties, context);

        // then
        verify(context, times(1)).disableDefaultConstraintViolation();
        assertFalse(result);
        verify(context, times(1)).buildConstraintViolationWithTemplate("Duplicate region codes found");
    }

    @Test
    public void testInvalidDomainRegions() {
        // given
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        DomainRegion usa1 = new DomainRegion(USA + ".abc", Set.of(LA,NYC));
        DomainRegion usa2 = new DomainRegion(USA, Set.of(LA,NYC));
        DomainRegionProperties properties = new DomainRegionProperties();
        properties.setDomainRegions(Arrays.asList(usa1, usa2));

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        // when
        boolean result = validator.isValid(properties, context);

        // then
        verify(context, times(1)).disableDefaultConstraintViolation();
        assertFalse(result);
        verify(context, times(1)).buildConstraintViolationWithTemplate("Invalid character found in region codes");
    }
}