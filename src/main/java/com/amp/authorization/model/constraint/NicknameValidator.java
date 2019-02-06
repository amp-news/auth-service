package com.amp.authorization.model.constraint;

import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NicknameValidator implements ConstraintValidator<Nickname, String> {

  private static final String USERNAME_PATTERN = "[a-zA-Z0-9_-]{1,100}";

  @Override
  public boolean isValid(String nickname, ConstraintValidatorContext context) {
    return !Objects.nonNull(nickname) || !nickname.isEmpty() && nickname.matches(USERNAME_PATTERN);
  }
}
