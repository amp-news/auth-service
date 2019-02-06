package com.amp.authorization.model.constraint;

import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

  private static final String PASSWORD_PATTERN =
      "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!]).{8,20})";

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    return Objects.nonNull(password) && password.matches(PASSWORD_PATTERN);
  }
}
