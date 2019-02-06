package com.amp.authorization.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static java.time.Duration.ofHours;
import static java.time.Duration.ofSeconds;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Getter
@Setter
@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfig {

  private String host;
  private int port;
  private Connection connection;

  @Getter
  @Setter
  public static class Connection {
    private int timeOutSec;
    private int readTimeOutSec;
  }

  @Bean
  public CacheManager cacheManager() {
    RedisCacheConfiguration cacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .entryTtl(ofHours(1));

    return RedisCacheManager.builder(jedisConnectionFactory())
        .cacheDefaults(cacheConfiguration)
        .build();
  }

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
    return new JedisConnectionFactory(
        configuration,
        JedisClientConfiguration.builder()
            .connectTimeout(ofSeconds(connection.getTimeOutSec()))
            .readTimeout(ofSeconds(connection.getTimeOutSec()))
            .usePooling()
            .build());
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(jedisConnectionFactory());
    return template;
  }
}
