package com.amp.authorization.repository.security;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

public abstract class BaseRedisHashRepository<T, KEY> implements RedisRepository<T, KEY> {

  private final String storeName;

  @Resource(name = "redisTemplate")
  private HashOperations<String, KEY, T> mapOperations;

  @Autowired private RedisTemplate<String, Object> template;

  public BaseRedisHashRepository(String storeName) {
    this.storeName = storeName;
  }

  protected final void delete(KEY hashKey) {
    this.mapOperations.delete(storeName, hashKey);
  }

  @Nullable
  protected T get(KEY hashKey) {
    return this.mapOperations.get(storeName, hashKey);
  }

  protected void put(KEY hashKey, T value) {
    this.mapOperations.put(storeName, hashKey, value);
  }

  protected void putIfAbsent(KEY hashKey, T value) {
    this.mapOperations.putIfAbsent(storeName, hashKey, value);
  }

  protected Set<KEY> keys() {
    return this.mapOperations.keys(storeName);
  }

  protected List<T> values() {
    return this.mapOperations.values(storeName);
  }

  protected Map<KEY, T> entries() {
    return this.mapOperations.entries(storeName);
  }

  protected Long size() {
    return this.mapOperations.size(storeName);
  }
}
