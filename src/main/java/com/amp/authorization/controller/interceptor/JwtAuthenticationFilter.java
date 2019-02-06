package com.amp.authorization.controller.interceptor;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amp.authorization.model.security.auth.AuthDetailsImplLite;
import com.amp.authorization.service.TokenService;
import com.amp.authorization.service.proxy.AccountService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static com.amp.authorization.model.AccountStatus.fromName;
import static com.amp.authorization.model.Claim.USER_EMAIL;
import static com.amp.authorization.model.Claim.USER_ROLE;
import static com.amp.authorization.model.Claim.USER_STATUS;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private TokenService tokenService;
  private AccountService accountService;

  @Autowired
  public JwtAuthenticationFilter(TokenService tokenService, AccountService accountService) {
    this.tokenService = tokenService;
    this.accountService = accountService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String jwt = tokenService.retrieveToken(request);
    if (Objects.nonNull(jwt)) {
      final Authentication authentication = getAuthentication(request, jwt);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private Authentication getAuthentication(HttpServletRequest request, String jwt) {
    final Claims claims = tokenService.getTokenClaims(jwt);

    final AuthDetailsImplLite authDetailsImplLite = new AuthDetailsImplLite();
    authDetailsImplLite.setPrincipalId(Long.valueOf(claims.getSubject()));
    authDetailsImplLite.setUsername(claims.get(USER_EMAIL.getName(), String.class));
    authDetailsImplLite.setPassword(jwt);
    authDetailsImplLite.setStatus(fromName(claims.get(USER_STATUS.getName(), String.class)));
    authDetailsImplLite.setRole(claims.get(USER_ROLE.getName(), String.class));

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            authDetailsImplLite, authDetailsImplLite.getPassword(), authDetailsImplLite.getAuthorities());
    authentication.setDetails(getAuthenticationDetails(request));

    return authentication;
  }

  private WebAuthenticationDetails getAuthenticationDetails(HttpServletRequest request) {
    return new WebAuthenticationDetailsSource().buildDetails(request);
  }
}
