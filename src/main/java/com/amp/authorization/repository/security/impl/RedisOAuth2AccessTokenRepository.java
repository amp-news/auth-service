package com.amp.authorization.repository.security.impl;

import com.amp.authorization.model.security.auth.external.OAuth2AccessTokenWrapper;
import com.amp.authorization.repository.security.BaseRedisHashRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RedisOAuth2AccessTokenRepository
    extends BaseRedisHashRepository<OAuth2AccessTokenWrapper, String> {

  public RedisOAuth2AccessTokenRepository() {
    super("AUTH_CLIENT_STORE");
  }

  @Override
  public OAuth2AccessTokenWrapper load(String clientId) {
    return get(clientId);
  }

  @Override
  public void save(String clientId, OAuth2AccessTokenWrapper token) {
    put(clientId, token);
  }

  @Override
  public void remove(String clientId) {
    delete(clientId);
  }
}
