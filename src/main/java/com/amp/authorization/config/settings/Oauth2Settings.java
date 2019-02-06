package com.amp.authorization.config.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.oauth2.client")
public class Oauth2Settings {

  private final Map<String, Registration> registration = new HashMap<>();
  private String baseUri;

  @Getter
  @Setter
  public static class Registration {
    private String provider;
    private String clientId;
    private String clientSecret;
    private Set<String> scope;
    private String clientName;
  }
}
