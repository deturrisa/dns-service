package org.example.dnsservice.validation;

import static org.example.dnsservice.util.ErrorCodes.ServerErrors.ERROR_EMPTY_DOMAIN_REGIONS;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Collectors;
import org.example.dnsservice.configuration.DomainRegion;
import org.example.dnsservice.configuration.DomainRegionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainRegionValidator extends AbstractValidator
    implements ConstraintValidator<DomainRegionCheck, DomainRegionProperties> {

  private static final Logger log = LoggerFactory.getLogger(DomainRegionValidator.class);

  @Override
  public boolean isValid(
      DomainRegionProperties domainRegionProperties, ConstraintValidatorContext context) {
    var domainRegions = domainRegionProperties.getDomainRegions();
    if (isNullOrEmpty(domainRegions)) {
      log.error(ERROR_EMPTY_DOMAIN_REGIONS);
      return false;
    }

    var regionCodes =
        domainRegionProperties.getDomainRegions().stream()
            .map(DomainRegion::getRegionCode)
            .collect(Collectors.toSet());

    var containsUniqueRegions =
        regionCodes.size() == domainRegionProperties.getDomainRegions().size();
    var containsOnlyLowerCaseAtoZ =
        regionCodes.stream().allMatch(AbstractValidator::containsOnlyLowerCaseAtoZ);

    if (!containsUniqueRegions) {
      var message = "Duplicate region codes found";
      context.disableDefaultConstraintViolation();
      log.error(message);
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    if (!containsOnlyLowerCaseAtoZ) {
      var message = "Invalid character found in region codes";
      context.disableDefaultConstraintViolation();
      log.error(message);
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
    return containsUniqueRegions && containsOnlyLowerCaseAtoZ;
  }
}
