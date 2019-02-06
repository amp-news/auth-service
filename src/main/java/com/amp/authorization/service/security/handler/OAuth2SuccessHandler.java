package com.amp.authorization.service.security.handler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amp.authorization.model.security.auth.RefreshableToken;
import com.amp.authorization.model.security.auth.external.OAuth2AuthenticationWrapper;
import com.amp.authorization.repository.security.RedisRepository;
import com.amp.authorization.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private static final String TOKEN_REDIRECT_PATH = "/auth/token-retrieve";

  private TokenService tokenService;
  private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
  private RedisRepository<String, Long> refreshTokenRepository;

  @Autowired
  public OAuth2SuccessHandler(
      TokenService tokenService,
      OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
      RedisRepository<String, Long> refreshTokenRepository) {
    this.tokenService = tokenService;
    this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    final OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
    final OAuth2AccessToken accessToken =
        oAuth2AuthorizedClientService
            .loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(), authToken.getName())
            .getAccessToken();

    final RefreshableToken token =
        tokenService.generateToken(new OAuth2AuthenticationWrapper(authToken, accessToken));

    final String refreshToken = token.getRefreshToken();
    refreshTokenRepository.save(token.getPrincipalId(), refreshToken);

    /*
      Currently removing google client with token after successful oath processing.
      No integrations subsequently used.
    */
    oAuth2AuthorizedClientService.removeAuthorizedClient(
        authToken.getAuthorizedClientRegistrationId(), authToken.getName());

    response.sendRedirect(getRedirectURL(request, token));
  }

  private String getRedirectURL(HttpServletRequest request, RefreshableToken token)
      throws IOException {
    try {
      final URI requestURI = new URI(request.getRequestURL().toString());
      return new StringBuilder()
          .append(requestURI.getScheme())
          .append("://")
          .append(requestURI.getHost())
          .append(":")
          .append(requestURI.getPort())
          .append(request.getContextPath())
          .append(TOKEN_REDIRECT_PATH)
          .append(buildQueryPart(token))
          .toString();
    } catch (URISyntaxException e) {
      throw new IOException("Cannot write redirect URI", e);
    }
  }

  private String buildQueryPart(RefreshableToken token) {
    return new StringBuilder("?")
        .append("access_token=")
        .append(token.getToken())
        .append("&access_token_type=")
        .append(tokenService.getAccessTokenType().getValue())
        .append("&access_token_expires_at=")
        .append(token.getTokenExpiresAt())
        .append("&refresh_token=")
        .append(token.getRefreshToken())
        .toString();
  }
}
