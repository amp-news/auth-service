package com.amp.authorization.model.security.auth.external;

import javax.security.auth.Subject;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2AuthenticationWrapper extends AbstractAuthenticationToken {

  private OAuth2AuthenticationToken authenticationToken;
  private OAuth2AccessToken accessToken;

  public OAuth2AuthenticationWrapper(
      OAuth2AuthenticationToken authenticationToken, OAuth2AccessToken accessToken) {
    super(authenticationToken.getAuthorities());
    this.authenticationToken = authenticationToken;
    this.accessToken = accessToken;
  }

  public OAuth2AccessToken getAccessToken() {
    return accessToken;
  }

  @Override
  public Object getCredentials() {
    return authenticationToken.getCredentials();
  }

  @Override
  public OAuth2User getPrincipal() {
    return authenticationToken.getPrincipal();
  }

  @Override
  public boolean implies(Subject subject) {
    return authenticationToken.implies(subject);
  }
}
