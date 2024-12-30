package org.example.dnsservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DomainRegionValidator.class)
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainRegionCheck {
    String message() default "Domains must be unique for each region.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
