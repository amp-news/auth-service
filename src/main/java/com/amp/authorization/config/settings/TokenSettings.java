package com.amp.authorization.config.settings;

import java.security.KeyPair;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.auth.token")
public class TokenSettings {

  private String issuer;
  private String keystorePath;
  private String keystorePassword;
  private String keyPairAlias;
  private Long maxAgeSeconds;
  private Long refreshAgeSeconds;

  public KeyPair getKeyPair() {
    final ClassPathResource classPathResource = new ClassPathResource(keystorePath);
    final KeyStoreKeyFactory keyStoreKeyFactory =
        new KeyStoreKeyFactory(classPathResource, keystorePassword.toCharArray());

    return keyStoreKeyFactory.getKeyPair(keyPairAlias);
  }
}
