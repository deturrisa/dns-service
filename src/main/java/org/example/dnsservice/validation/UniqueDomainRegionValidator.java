package org.example.dnsservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.example.dnsservice.configuration.UniqueDomainRegionCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.stream.Collectors;

public class UniqueDomainRegionValidator
        implements ConstraintValidator<UniqueDomainRegionCheck, DomainRegionProperties> {

    private static final Logger log = LoggerFactory.getLogger(UniqueDomainRegionValidator.class) ;

    @Override
    public boolean isValid(DomainRegionProperties domainRegionProperties, ConstraintValidatorContext context) {
        Set<String> regionCodes =
                domainRegionProperties.getDomainRegions().
                        stream().map(DomainRegion::getRegionCode)
                        .collect(Collectors.toSet());

        boolean isValid = regionCodes.size() == domainRegionProperties.getDomainRegions().size();

        if (!isValid) {
            String message = "Duplicate region codes found";
            context.disableDefaultConstraintViolation();
            log.error(message);
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return isValid;
    }
}