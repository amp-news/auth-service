package com.amp.authorization.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CloudAwareAuthorizationCodeTokenResponseClient
    implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

  private static final String HEADER_X_FORWARDED_HOST = "x-forwarded-host";
  private static final String HEADER_X_FORWARDED_PROTO = "x-forwarded-proto";
  private static final String HEADER_X_FORWARDED_PORT = "x-forwarded-port";
  private static final String HEADER_X_FORWARDED_PREFIX = "x-forwarded-prefix";

  private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

  @Override
  public OAuth2AccessTokenResponse getTokenResponse(
      OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest)
      throws OAuth2AuthenticationException {

    ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();

    // Build the authorization code grant request for the token endpoint
    AuthorizationCode authorizationCode =
        new AuthorizationCode(
            authorizationGrantRequest
                .getAuthorizationExchange()
                .getAuthorizationResponse()
                .getCode());
    URI redirectUri =
        rewriteRedirectToProxyURI(
            toURI(
                authorizationGrantRequest
                    .getAuthorizationExchange()
                    .getAuthorizationRequest()
                    .getRedirectUri()));
    AuthorizationGrant authorizationCodeGrant =
        new AuthorizationCodeGrant(authorizationCode, redirectUri);
    URI tokenUri = toURI(clientRegistration.getProviderDetails().getTokenUri());

    // Set the credentials to authenticate the client at the token endpoint
    ClientID clientId = new ClientID(clientRegistration.getClientId());
    Secret clientSecret = new Secret(clientRegistration.getClientSecret());
    ClientAuthentication clientAuthentication;
    if (ClientAuthenticationMethod.POST.equals(
        clientRegistration.getClientAuthenticationMethod())) {
      clientAuthentication = new ClientSecretPost(clientId, clientSecret);
    } else {
      clientAuthentication = new ClientSecretBasic(clientId, clientSecret);
    }

    TokenResponse tokenResponse;
    try {
      // Send the Access Token request
      TokenRequest tokenRequest =
          new TokenRequest(tokenUri, clientAuthentication, authorizationCodeGrant);
      HTTPRequest httpRequest = tokenRequest.toHTTPRequest();
      httpRequest.setAccept(MediaType.APPLICATION_JSON_VALUE);
      httpRequest.setConnectTimeout(30000);
      httpRequest.setReadTimeout(30000);
      tokenResponse = TokenResponse.parse(httpRequest.send());
    } catch (ParseException pe) {
      OAuth2Error oauth2Error =
          new OAuth2Error(
              INVALID_TOKEN_RESPONSE_ERROR_CODE,
              "An error occurred parsing the Access Token response: " + pe.getMessage(),
              null);
      throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), pe);
    } catch (IOException ioe) {
      throw new AuthenticationServiceException(
          "An error occurred while sending the Access Token Request: " + ioe.getMessage(), ioe);
    }

    if (!tokenResponse.indicatesSuccess()) {
      TokenErrorResponse tokenErrorResponse = (TokenErrorResponse) tokenResponse;
      ErrorObject errorObject = tokenErrorResponse.getErrorObject();
      OAuth2Error oauth2Error =
          new OAuth2Error(
              errorObject.getCode(),
              errorObject.getDescription(),
              (errorObject.getURI() != null ? errorObject.getURI().toString() : null));
      throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
    }

    AccessTokenResponse accessTokenResponse = (AccessTokenResponse) tokenResponse;

    String accessToken = accessTokenResponse.getTokens().getAccessToken().getValue();
    OAuth2AccessToken.TokenType accessTokenType = null;
    if (OAuth2AccessToken.TokenType.BEARER
        .getValue()
        .equalsIgnoreCase(accessTokenResponse.getTokens().getAccessToken().getType().getValue())) {
      accessTokenType = OAuth2AccessToken.TokenType.BEARER;
    }
    long expiresIn = accessTokenResponse.getTokens().getAccessToken().getLifetime();

    Set<String> scopes;
    if (CollectionUtils.isEmpty(accessTokenResponse.getTokens().getAccessToken().getScope())) {
      scopes =
          new LinkedHashSet<>(
              authorizationGrantRequest
                  .getAuthorizationExchange()
                  .getAuthorizationRequest()
                  .getScopes());
    } else {
      scopes =
          new LinkedHashSet<>(
              accessTokenResponse.getTokens().getAccessToken().getScope().toStringList());
    }

    Map<String, Object> additionalParameters =
        new LinkedHashMap<>(accessTokenResponse.getCustomParameters());

    return OAuth2AccessTokenResponse.withToken(accessToken)
        .tokenType(accessTokenType)
        .expiresIn(expiresIn)
        .scopes(scopes)
        .additionalParameters(additionalParameters)
        .build();
  }

  private URI rewriteRedirectToProxyURI(URI redirectURI) {
    final UriComponentsBuilder newURIBuilder = UriComponentsBuilder.newInstance();

    final HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    final String proxyHost = request.getHeader(HEADER_X_FORWARDED_HOST);
    final String proxySchema = request.getHeader(HEADER_X_FORWARDED_PROTO);
    final String proxyPort = request.getHeader(HEADER_X_FORWARDED_PORT);
    final String proxyPrefix = request.getHeader(HEADER_X_FORWARDED_PREFIX);

    if (!StringUtils.isEmpty(proxyHost)) {
      newURIBuilder
          .scheme(proxySchema)
          .host(proxyHost.replace(":" + proxyPort, ""))
          .port(proxyPort)
          .path(proxyPrefix + redirectURI.getPath())
          .query(redirectURI.getQuery());
    }

    return newURIBuilder.build().toUri();
  }

  private static URI toURI(String uriStr) {
    try {
      return new URI(uriStr);
    } catch (Exception ex) {
      throw new IllegalArgumentException("An error occurred parsing URI: " + uriStr, ex);
    }
  }
}
