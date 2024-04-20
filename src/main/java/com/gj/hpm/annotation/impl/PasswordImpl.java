package com.gj.hpm.annotation.impl;

import com.gj.hpm.annotation.Password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordImpl implements
    ConstraintValidator<Password, String> {

  @Override
  public void initialize(Password password) {
  }

  @Override
  public boolean isValid(String Password,
      ConstraintValidatorContext cxt) {
    return (Password.length() > 8) && (Password.length() <= 14);
  }

}