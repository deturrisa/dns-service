package org.example.dnsservice.validation;

import java.util.List;
import org.example.dnsservice.configuration.DomainRegion;

public abstract class AbstractValidator {

  protected static boolean isNullOrEmpty(List<DomainRegion> domainRegions) {
    return domainRegions == null || domainRegions.isEmpty();
  }

  protected static boolean containsOnlyLowerCaseAtoZ(String value) {
    return value.matches("[a-z]+");
  }
}
