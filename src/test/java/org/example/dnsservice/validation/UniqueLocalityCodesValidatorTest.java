package org.example.dnsservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.util.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.example.dnsservice.util.TestUtil.TestData.*;

@UnitTest
public class UniqueLocalityCodesValidatorTest {

    @Mock
    private DomainRegionProperties domainRegionProperties;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private UniqueLocalityCodesValidator validator;

    @Test
    public void shouldReturnFalseIfDomainRegionsListIsEmpty() {
        //given
        Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(new ArrayList<>());
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        //when
        //then
        Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
    }

    @Test
    public void shouldReturnTrueIfLocalityCodesAreUnique() {
        //given
        DomainRegion usa = new DomainRegion(USA, Set.of(LA,NYC));
        DomainRegion germany = new DomainRegion(GERMANY, Set.of(FRANKFURT));


        Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(Arrays.asList(usa, germany));
        //when
        //then
        Assertions.assertTrue(validator.isValid(domainRegionProperties, context));
    }

    @Test
    public void shouldReturnFalseIfLocalityCodesAreNotUnique() {
        //given
        UniqueLocalityCodesValidator validator = new UniqueLocalityCodesValidator();

        String duplicateLocalityCode = "duplicated_locality_code";

        DomainRegion usa = new DomainRegion(USA, Set.of(LA,NYC, duplicateLocalityCode));
        DomainRegion germany = new DomainRegion(GERMANY, Set.of(FRANKFURT, duplicateLocalityCode));

        Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(Arrays.asList(usa, germany));

        //when
        //then
        Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
    }

    @Test
    public void shouldReturnFalseIfLocalityCodesAreNullOrEmpty() {
        //given
        UniqueLocalityCodesValidator validator = new UniqueLocalityCodesValidator();

        DomainRegion emptyLocalityCodes = new DomainRegion(USA, Set.of());
        DomainRegion germany = new DomainRegion(GERMANY, Set.of(FRANKFURT));

        Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(Arrays.asList(emptyLocalityCodes, germany));

        //when
        //then
        Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
    }

    @Test
    public void shouldReturnFalseIfInvalidLocalityCode() {
        //given
        UniqueLocalityCodesValidator validator = new UniqueLocalityCodesValidator();

        DomainRegion emptyLocalityCodes = new DomainRegion(USA, Set.of());
        DomainRegion germany = new DomainRegion(GERMANY, Set.of(FRANKFURT + "something.else"));

        Mockito.when(domainRegionProperties.getDomainRegions()).thenReturn(Arrays.asList(emptyLocalityCodes, germany));

        //when
        //then
        Assertions.assertFalse(validator.isValid(domainRegionProperties, context));
    }
}