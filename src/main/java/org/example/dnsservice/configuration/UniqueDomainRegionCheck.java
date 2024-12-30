package org.example.dnsservice.configuration;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.dnsservice.validation.UniqueDomainRegionValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueDomainRegionValidator.class)
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueDomainRegionCheck {
    String message() default "Domains must be unique for each region.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
