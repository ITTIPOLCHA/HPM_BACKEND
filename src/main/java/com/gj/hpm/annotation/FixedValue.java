package com.gj.hpm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gj.hpm.annotation.impl.FixedValueImpl;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = { FixedValueImpl.class })
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface FixedValue {
    String message() default "Invalid value. Accepted values are: line, email";

    String[] acceptedValues() default { "line", "email" };

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
