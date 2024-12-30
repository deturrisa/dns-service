package org.example.dnsservice.validation;

import org.example.dnsservice.configuration.DomainRegion;

import java.util.List;

public abstract class AbstractUniqueValidator {

    protected static final String ERROR_EMPTY_DOMAIN_REGIONS = "Domain regions are empty or could not be retrieved";

    protected static boolean isNullOrEmpty(List<DomainRegion> domainRegions) {
        return domainRegions == null || domainRegions.isEmpty();
    }
}
