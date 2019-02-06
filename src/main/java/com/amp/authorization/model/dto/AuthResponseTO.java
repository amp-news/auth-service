package com.amp.authorization.model.dto;

import java.util.Date;

import lombok.Data;

@Data
public class AuthResponseTO {

  private final String accessTokenType;
  private final String accessToken;
  private final String refreshToken;
  private final Date accessTokenExpiresAt;
}
