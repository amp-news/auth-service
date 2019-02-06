package com.amp.authorization.repository.security.impl;

import com.amp.authorization.repository.security.BaseRedisHashRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRefreshTokenRepository extends BaseRedisHashRepository<String, Long> {

  public RedisRefreshTokenRepository() {
    super("REFRESH_TOKEN_STORE");
  }

  @Override
  public String load(Long userId) {
    return get(userId);
  }

  @Override
  public void save(Long userId, String refreshToken) {
    put(userId, refreshToken);
  }

  @Override
  public void remove(Long userId) {
    delete(userId);
  }
}
