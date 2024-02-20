package com.gj.hpm.annotation.impl;

import com.gj.hpm.annotation.MobilePhone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MobilePhoneImpl implements
    ConstraintValidator<MobilePhone, String> {

  @Override
  public void initialize(MobilePhone mobilePhone) {
  }

  @Override
  public boolean isValid(String mobilePhone,
      ConstraintValidatorContext cxt) {
    return mobilePhone != null && mobilePhone.matches("[0-9]+")
        && (mobilePhone.length() > 8) && (mobilePhone.length() < 14);
  }

}