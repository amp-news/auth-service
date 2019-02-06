package com.amp.authorization.repository.security;

public interface RedisRepository<T, KEY> {

  T load(KEY key);

  void save(KEY key, T obj);

  void remove(KEY key);
}
