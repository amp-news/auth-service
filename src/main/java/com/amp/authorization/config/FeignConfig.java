package com.amp.authorization.config;

import com.amp.authorization.core.ErrorTODecoder;
import com.amp.authorization.core.ExternalResourceDecoder;
import feign.Contract;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Bean
  public Logger.Level feignLogger() {
    return Logger.Level.FULL;
  }

  @Bean
  public Contract feignContract() {
    return new Contract.Default();
  }

  @Bean
  public Decoder feignDecoder() {
    return new ExternalResourceDecoder(new JacksonDecoder());
  }

  @Bean
  public Encoder feignEncoder() {
    return new JacksonEncoder();
  }

  @Bean
  public ErrorTODecoder feignErrorDecoder() {
    return new ErrorTODecoder();
  }
}
