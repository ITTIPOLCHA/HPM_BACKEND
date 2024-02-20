package com.gj.hpm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gj.hpm.annotation.impl.MobilePhoneImpl;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = MobilePhoneImpl.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MobilePhone {
    String message() default "{msg.general.err.mobile.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}