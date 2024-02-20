package com.gj.hpm.annotation.impl;

import java.util.Arrays;

import com.gj.hpm.annotation.FixedValue;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FixedValueImpl implements ConstraintValidator<FixedValue, String> {

    private String[] acceptedValues;

    @Override
    public void initialize(FixedValue constraintAnnotation) {
        acceptedValues = constraintAnnotation.acceptedValues();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Arrays.asList(acceptedValues).contains(value);
    }
}
