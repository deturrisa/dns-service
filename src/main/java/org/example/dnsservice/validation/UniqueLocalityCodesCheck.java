package org.example.dnsservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueLocalityCodesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueLocalityCodesCheck {
    String message() default "Locality codes must be unique across all domain regions.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}