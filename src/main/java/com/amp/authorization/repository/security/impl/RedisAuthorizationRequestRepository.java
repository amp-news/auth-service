package com.amp.authorization.repository.security.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amp.authorization.repository.security.BaseRedisHashRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Repository;

import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

@Repository
public class RedisAuthorizationRequestRepository extends BaseRedisHashRepository<byte[], byte[]>
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final int AUTH_REQUEST_COOKIE_VALIDITY_SEC = 60;
  private static final String AUTH_REQUEST_COOKIE = "AUTH_REQ";
  private static final String HASH_ALGORITHM = "SHA-256";

  private final Base64.Encoder encoder = Base64.getEncoder();
  private final Base64.Decoder decoder = Base64.getDecoder();

  @Autowired
  private RedisTemplate<String, Object> template;

  public RedisAuthorizationRequestRepository() {
    super("AUTHORIZATION_REQUEST_STORE");
  }

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    byte[] serializedRequest = load(decoder.decode(retrieveRequestId(request.getCookies())));
    return (OAuth2AuthorizationRequest) deserialize(serializedRequest);
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    byte[] serializedRequest = serialize(authorizationRequest);
    byte[] requestId = generateRequestId(serializedRequest);

    save(requestId, serializedRequest);

    response.addCookie(buildAuthRequestCookie(request, encoder.encodeToString(requestId)));
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
    final OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
    // TODO: fix issue with entries/keys
    if (authorizationRequest != null) {
      this.remove(decoder.decode(retrieveRequestId(request.getCookies())));
    }

    return authorizationRequest;
  }

  private byte[] generateRequestId(byte[] serializedRequest) {
    byte[] requestId = new byte[0];

    try {
      MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
      requestId = digest.digest(serializedRequest);
    }
    catch (NoSuchAlgorithmException e) {
      System.out.println("Invalid algorithm name: " + HASH_ALGORITHM);
    }

    return requestId;
  }

  private String retrieveRequestId(Cookie[] cookies) {
    final Optional<Cookie> requestCookie =
        Stream.of(cookies)
            .filter(cookie -> cookie.getName().equals(AUTH_REQUEST_COOKIE))
            .findFirst();

    return requestCookie.isPresent() ? requestCookie.get().getValue() : "";
  }

  private Cookie buildAuthRequestCookie(HttpServletRequest request, String authRequestId) {
    Cookie authRequestCookie = new Cookie(AUTH_REQUEST_COOKIE, authRequestId);
    authRequestCookie.setMaxAge(AUTH_REQUEST_COOKIE_VALIDITY_SEC);
    authRequestCookie.setSecure(false);
    authRequestCookie.setPath("/");

    return authRequestCookie;
  }

  @Override
  public byte[] load(byte[] requestId) {
    return get(requestId);
  }

  @Override
  public void save(byte[] requestId, byte[] request) {
    put(requestId, request);
  }

  @Override
  public void remove(byte[] requestId) {
    delete(requestId);
  }
}
