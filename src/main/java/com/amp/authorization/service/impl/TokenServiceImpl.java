package com.amp.authorization.service.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.amp.authorization.config.settings.TokenSettings;
import com.amp.authorization.model.security.auth.AccessToken;
import com.amp.authorization.model.security.auth.AuthDetails;
import com.amp.authorization.model.security.auth.external.GoogleOauth2User;
import com.amp.authorization.model.security.auth.external.OAuth2AuthenticationWrapper;
import com.amp.authorization.model.security.auth.RefreshableToken;
import com.amp.authorization.service.TokenService;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static com.amp.authorization.model.Claim.USER_EMAIL;
import static com.amp.authorization.model.Claim.USER_ROLE;
import static com.amp.authorization.model.Claim.USER_STATUS;
import static com.nimbusds.oauth2.sdk.token.AccessTokenType.BEARER;
import static io.jsonwebtoken.SignatureAlgorithm.RS512;
import static java.lang.String.valueOf;
import static java.util.Date.from;
import static org.springframework.util.StringUtils.hasText;

@Service
public class TokenServiceImpl implements TokenService {

  private AccessTokenType tokenType = BEARER;
  private TokenSettings tokenSettings;

  @Autowired
  public TokenServiceImpl(TokenSettings tokenSettings) {
    this.tokenSettings = tokenSettings;
  }

  @Override
  public @Nullable String retrieveToken(HttpServletRequest request) {
    String token = null;

    String bearerToken = request.getHeader("Authorization");
    if (hasText(bearerToken) && bearerToken.startsWith(tokenType.getValue())) {
      token = bearerToken.replace(tokenType.getValue(), "").trim();
    }

    return token;
  }

  @Override
  public Claims getTokenClaims(String token) {
    return Jwts.parser()
        .setSigningKey(tokenSettings.getKeyPair().getPrivate())
        .parseClaimsJws(token)
        .getBody();
  }

  @Override
  public AccessTokenType getAccessTokenType() {
    return tokenType;
  }

  @Override
  public RefreshableToken generateToken(Authentication authentication) {
    RefreshableToken token;
    if (authentication instanceof UsernamePasswordAuthenticationToken) {
      token = generateAuthToken((UsernamePasswordAuthenticationToken) authentication);
    } else if (authentication instanceof OAuth2AuthenticationWrapper) {
      token = generateAuthToken((OAuth2AuthenticationWrapper) authentication);
    } else {
      throw new IllegalArgumentException("Bad Authentication type provided.");
    }

    return token;
  }

  private RefreshableToken generateAuthToken(UsernamePasswordAuthenticationToken authToken) {
    final AuthDetails authDetails = (AuthDetails) authToken.getPrincipal();

    final Instant issuedAt = Instant.now();
    final Instant willExpireAt = issuedAt.plusSeconds(tokenSettings.getMaxAgeSeconds());
    final Instant refreshWillExpireAt = issuedAt.plusSeconds(tokenSettings.getRefreshAgeSeconds());

    final Map<String, Object> claims = new HashMap<>();
    claims.put(USER_EMAIL.getName(), authDetails.getUsername());
    claims.put(USER_STATUS.getName(), authDetails.getStatus());
    claims.put(USER_ROLE.getName(), authDetails.getRole());

    return new AccessToken(
        tokenSettings.getIssuer(),
        valueOf(authDetails.getPrincipalId()),
        from(issuedAt),
        from(willExpireAt),
        from(refreshWillExpireAt),
        claims,
        RS512,
        tokenSettings.getKeyPair().getPrivate());
  }

  private RefreshableToken generateAuthToken(OAuth2AuthenticationWrapper authToken) {
    final GoogleOauth2User principal = (GoogleOauth2User) authToken.getPrincipal();

    final Instant issuedAt = Instant.now();
    final Instant willExpireAt = issuedAt.plusSeconds(tokenSettings.getMaxAgeSeconds());
    final Instant refreshWillExpireAt = issuedAt.plusSeconds(tokenSettings.getRefreshAgeSeconds());

    final Map<String, Object> claims = new HashMap<>();
    claims.put(USER_EMAIL.getName(), principal.getEmail());
    claims.put(USER_STATUS.getName(), principal.getStatus());
    claims.put(USER_ROLE.getName(), principal.getRole());

    return new AccessToken(
        tokenSettings.getIssuer(),
        valueOf(principal.getLocalId()),
        from(issuedAt),
        from(willExpireAt),
        from(refreshWillExpireAt),
        claims,
        RS512,
        tokenSettings.getKeyPair().getPrivate());
  }
}
