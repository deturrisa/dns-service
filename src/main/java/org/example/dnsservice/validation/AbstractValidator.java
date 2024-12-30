package org.example.dnsservice.validation;

import org.example.dnsservice.configuration.DomainRegion;

import java.util.List;

public abstract class AbstractValidator {

    protected static boolean isNullOrEmpty(List<DomainRegion> domainRegions) {
        return domainRegions == null || domainRegions.isEmpty();
    }

    protected static boolean containsOnlyLowerCaseAtoZ(String value) {
        return value.matches("[a-z]+");
    }
}
