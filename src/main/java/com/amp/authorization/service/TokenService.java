package com.amp.authorization.service;

import javax.servlet.http.HttpServletRequest;

import com.amp.authorization.model.security.auth.RefreshableToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import io.jsonwebtoken.Claims;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;

public interface TokenService {

  @Nullable
  String retrieveToken(HttpServletRequest token);

  RefreshableToken generateToken(Authentication authentication);

  Claims getTokenClaims(String token);

  AccessTokenType getAccessTokenType();
}
