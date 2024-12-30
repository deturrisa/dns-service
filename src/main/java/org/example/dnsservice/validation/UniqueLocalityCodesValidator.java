package org.example.dnsservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueLocalityCodesValidator
    extends AbstractUniqueValidator
        implements ConstraintValidator<UniqueLocalityCodesCheck, DomainRegionProperties>{

    private static final Logger log = LoggerFactory.getLogger(UniqueLocalityCodesValidator.class) ;

    private static final String ERROR_DUPLICATE_LOCALITY_CODE = "Duplicate locality code: {}";
    private static final String ERROR_INVALID_REGION = "Locality codes are empty or could not be retrieved for region: {}";

    @Override
    public boolean isValid(DomainRegionProperties domainRegionProperties, ConstraintValidatorContext context) {
        List<DomainRegion> domainRegions = domainRegionProperties.getDomainRegions();
        if (isNullOrEmpty(domainRegions)) {
            log.error(ERROR_EMPTY_DOMAIN_REGIONS);
            return false;
        }

        Set<String> allLocalityCodes = new HashSet<>();
        return domainRegions.stream().allMatch(region -> validateRegion(region, allLocalityCodes));
    }

    private boolean validateRegion(DomainRegion region, Set<String> allLocalityCodes) {
        if (isNotNullOrEmpty(region)) {
            return region.getLocalityCodes().stream().allMatch(localityCode -> isUniqueLocalityCode(localityCode, allLocalityCodes));
        } else {
            log.error(ERROR_INVALID_REGION, region.getRegionCode());
            return false;
        }
    }

    private boolean isUniqueLocalityCode(String localityCode, Set<String> allLocalityCodes) {
        if (!allLocalityCodes.add(localityCode)) {
            log.error(ERROR_DUPLICATE_LOCALITY_CODE, localityCode);
            return false;
        }
        return true;
    }

    private static boolean isNotNullOrEmpty(DomainRegion region) {
        return region.getLocalityCodes() != null && !region.getLocalityCodes().isEmpty();
    }
}