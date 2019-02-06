package com.amp.authorization.model.security.auth.external;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

import lombok.Data;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@Data
public class OAuth2AccessTokenWrapper implements Serializable {

  private transient OAuth2AccessToken accessToken;

  public OAuth2AccessTokenWrapper(OAuth2AccessToken accessToken) {
    this.accessToken = accessToken;
  }

  private void writeObject(final ObjectOutputStream out) throws IOException {
    out.writeUTF(this.accessToken.getTokenValue());
    out.writeObject(this.accessToken.getIssuedAt());
    out.writeObject(this.accessToken.getExpiresAt());
    out.writeObject(this.accessToken.getScopes());
  }

  @SuppressWarnings("unchecked")
  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    final String tokenValue = in.readUTF();
    final Instant issuedAt = (Instant) in.readObject();
    final Instant expiresAt = (Instant) in.readObject();
    final Set<String> scopes = (Set<String>) in.readObject();

    this.accessToken = new OAuth2AccessToken(BEARER, tokenValue, issuedAt, expiresAt, scopes);
  }
}
