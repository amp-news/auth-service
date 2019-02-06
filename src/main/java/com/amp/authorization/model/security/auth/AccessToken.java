package com.amp.authorization.model.security.auth;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static com.amp.authorization.model.Claim.TOKEN_TYPE;
import static com.amp.authorization.model.security.auth.TokenType.ACCESS;
import static com.amp.authorization.model.security.auth.TokenType.REFRESH;

@Getter
@ToString
@EqualsAndHashCode
public class AccessToken implements RefreshableToken {

  private String compactedAccessToken;
  private String compactedRefreshToken;

  private String issuer;
  private String subject;
  private Date tokenIssuedAt;
  private Date tokenExpiresAt;
  private Date refreshTokenExpiresAt;
  private Map<String, Object> claims;

  private SignatureAlgorithm signatureAlgorithm;
  private Key key;

  public AccessToken(
      String issuer,
      String subject,
      Date tokenIssuedAt,
      Date tokenExpiresAt,
      Date refreshTokenExpiresAt,
      Map<String, Object> claims,
      SignatureAlgorithm signatureAlgorithm,
      Key key) {
    this.issuer = issuer;
    this.subject = subject;
    this.tokenIssuedAt = tokenIssuedAt;
    this.tokenExpiresAt = tokenExpiresAt;
    this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    this.claims = claims;
    this.signatureAlgorithm = signatureAlgorithm;
    this.key = key;
  }

  @Override
  public Long getPrincipalId() {
    return Long.valueOf(subject);
  }

  @Override
  public Date getTokenExpiresAt() {
    return tokenExpiresAt;
  }

  @Override
  public String getToken() {
    if (Objects.isNull(this.compactedAccessToken)) {
      this.compactedAccessToken = compactJWT(ACCESS);
    }

    return this.compactedAccessToken;
  }

  @Override
  public String getRefreshToken() {
    if (Objects.isNull(this.compactedRefreshToken)) {
      this.compactedRefreshToken = compactJWT(REFRESH);
    }

    return this.compactedRefreshToken;
  }

  private String compactJWT(TokenType type) {
    return Jwts.builder()
        .setIssuer(this.issuer)
        .setSubject(this.subject)
        .setIssuedAt(this.tokenIssuedAt)
        .setExpiration(type == ACCESS ? this.tokenExpiresAt : this.refreshTokenExpiresAt)
        .addClaims(getTokenClaims(type))
        .signWith(signatureAlgorithm, key)
        .compact();
  }

  private Map<String, Object> getTokenClaims(TokenType tokenType) {
    final Map<String, Object> tokenClaims = new HashMap<>(this.claims);
    tokenClaims.put(TOKEN_TYPE.getName(), tokenType.name());

    return tokenClaims;
  }
}
