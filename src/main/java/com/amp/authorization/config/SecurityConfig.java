package com.amp.authorization.config;

import com.amp.authorization.config.settings.Oauth2Settings;
import com.amp.authorization.controller.interceptor.JwtAuthenticationEntryPoint;
import com.amp.authorization.controller.interceptor.JwtAuthenticationFilter;
import com.amp.authorization.model.security.auth.external.GoogleOauth2User;
import com.amp.authorization.repository.security.impl.RedisAuthorizationRequestRepository;
import com.amp.authorization.service.impl.AuthUserDetailsService;
import com.amp.authorization.service.impl.CloudAwareAuthorizationCodeTokenResponseClient;
import com.amp.authorization.service.security.RedisOauth2AuthorizedClientService;
import com.amp.authorization.service.security.handler.OAuth2FailureHandler;
import com.amp.authorization.service.security.handler.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@EnableWebSecurity
@Order(HIGHEST_PRECEDENCE)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final int PASSWORD_ENCODING_STRENGTH = 8;
  private static final String GOOGLE_REGISTRATION_ID = "google";

  @Autowired private AuthUserDetailsService authUserDetailsService;

  @Autowired private JwtAuthenticationFilter authenticationFilter;

  @Autowired private JwtAuthenticationEntryPoint authenticationEntryPoint;

  @Autowired private Oauth2Settings oauth2Settings;

  @Autowired private OAuth2SuccessHandler oAuth2SuccessHandler;

  @Autowired private OAuth2FailureHandler oAuth2FailureHandler;

  @Autowired private RedisAuthorizationRequestRepository redisAuthorizationRequestRepository;

  @Autowired private RedisOauth2AuthorizedClientService redisOauth2AuthorizedClientService;

  @Autowired private CloudAwareAuthorizationCodeTokenResponseClient authorizationCodeTokenResponseClient;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(PASSWORD_ENCODING_STRENGTH);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    return new InMemoryClientRegistrationRepository(googleClientRegistration());
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(authUserDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  public void configure(WebSecurity web) {
    web.debug(true);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .disable()
        .csrf()
        .disable()
        .httpBasic()
        .disable()
        .formLogin()
        .disable();

    http.authorizeRequests()
        .antMatchers("/auth/**", "/login/oauth2/code/**", "/actuator/**")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(authenticationEntryPoint);

    http.oauth2Login()
        .authorizationEndpoint()
        .baseUri(oauth2Settings.getBaseUri())
        .authorizationRequestRepository(redisAuthorizationRequestRepository)
        .and()
        .authorizedClientService(redisOauth2AuthorizedClientService)
        .userInfoEndpoint()
        .customUserType(GoogleOauth2User.class, "google")
        .and()
        .tokenEndpoint()
        .accessTokenResponseClient(authorizationCodeTokenResponseClient)
        .and()
        .successHandler(oAuth2SuccessHandler)
        .failureHandler(oAuth2FailureHandler);

    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
  }

  private ClientRegistration googleClientRegistration() {
    final Oauth2Settings.Registration googleProperties =
        oauth2Settings.getRegistration().get(GOOGLE_REGISTRATION_ID);

    return CommonOAuth2Provider.GOOGLE
        .getBuilder(googleProperties.getClientName())
        .clientId(googleProperties.getClientId())
        .clientSecret(googleProperties.getClientSecret())
        .scope(googleProperties.getScope().toArray(new String[0]))
        .build();
  }
}
