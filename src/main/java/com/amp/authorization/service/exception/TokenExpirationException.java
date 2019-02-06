package com.amp.authorization.service.exception;

import com.amp.authorization.model.security.auth.TokenType;

import static java.lang.String.format;

public class TokenExpirationException extends RuntimeException {

  private static final String MSG = "%s token expired.";

  public TokenExpirationException(TokenType tokenType) {
    super(format(MSG, tokenType.getName()));
  }
}
