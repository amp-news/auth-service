package com.amp.authorization.service.impl;

import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.NotNull;

import com.amp.authorization.model.dto.AccountTO;
import com.amp.authorization.model.dto.AuthResponseTO;
import com.amp.authorization.model.dto.SignInRefreshRequestTO;
import com.amp.authorization.model.dto.SignInRequestTO;
import com.amp.authorization.model.security.auth.AuthDetails;
import com.amp.authorization.model.security.auth.RefreshableToken;
import com.amp.authorization.repository.security.RedisRepository;
import com.amp.authorization.service.AuthService;
import com.amp.authorization.service.TokenService;
import com.amp.authorization.service.exception.TokenExpirationException;
import com.amp.authorization.service.exception.UnauthorizedException;
import com.amp.authorization.service.proxy.AccountService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.amp.authorization.model.security.auth.TokenType.REFRESH;
import static java.lang.Long.valueOf;

@Service
public class AuthServiceImpl implements AuthService {

  private AccountService accountService;
  private TokenService tokenService;
  private PasswordEncoder passwordEncoder;
  private AuthenticationManager authenticationManager;
  private RedisRepository<String, Long> refreshTokenRepository;

  @Autowired
  public AuthServiceImpl(
      AccountService accountService,
      TokenService tokenService,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      RedisRepository<String, Long> refreshTokenRepository) {
    this.accountService = accountService;
    this.tokenService = tokenService;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  public AuthResponseTO signIn(@NotNull SignInRequestTO accountCredentials) {
    final Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                accountCredentials.getEmail(), accountCredentials.getPassword()));

    if (!authentication.isAuthenticated()) {
      throw new BadCredentialsException("Bad credentials.");
    }

    return processAuthentication(authentication);
  }

  @Override
  public AuthResponseTO signInRefresh(@NotNull SignInRefreshRequestTO refreshRequest) {
    final Claims claims = tokenService.getTokenClaims(refreshRequest.getRefreshToken());
    final Long principalId = valueOf(claims.getSubject());

    final String refreshToken = refreshTokenRepository.load(principalId);
    if (!refreshToken.equals(refreshRequest.getRefreshToken())) {
      throw new InsufficientAuthenticationException("Invalid refresh token provided.");
    }

    if (Instant.now().isAfter(claims.getExpiration().toInstant())) {
      throw new TokenExpirationException(REFRESH);
    }

    final AccountTO accountTO = accountService.loadById(principalId);
    final Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            accountTO, accountTO.getPassword(), accountTO.getAuthorities());

    return processAuthentication(authentication);
  }

  @Override
  public void signOut() {
    refreshTokenRepository.remove(getCurrentAuthPrincipal().getPrincipalId());
  }

  @Override
  public void signUp(@NotNull AccountTO account) {
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    accountService.save(account);
  }

  @Override
  public AccountTO current() {
    return accountService.loadById(getCurrentAuthPrincipal().getPrincipalId());
  }

  private AuthDetails getCurrentAuthPrincipal() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("No current authentication found in the context.");
    }

    return (AuthDetails) authentication.getPrincipal();
  }

  private AuthResponseTO processAuthentication(Authentication authentication) {
    final RefreshableToken token = tokenService.generateToken(authentication);

    final String refreshToken = token.getRefreshToken();
    refreshTokenRepository.save(token.getPrincipalId(), refreshToken);

    return new AuthResponseTO(
        tokenService.getAccessTokenType().getValue(),
        token.getToken(),
        refreshToken,
        token.getTokenExpiresAt());
  }
}
