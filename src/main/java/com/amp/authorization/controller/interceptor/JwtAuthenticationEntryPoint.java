package com.amp.authorization.controller.interceptor;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amp.authorization.model.dto.ErrorTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final String AUTH_ERROR = "Not authorized. ";
  private ObjectMapper objectMapper;

  public JwtAuthenticationEntryPoint() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.findAndRegisterModules();
  }

  @Override
  public void commence(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      AuthenticationException authException)
      throws IOException {
    httpServletResponse.sendError(UNAUTHORIZED.value());
    httpServletResponse.addHeader("Content-Type", "application/json; charset=UTF-8");

    objectMapper.writeValue(
        httpServletResponse.getOutputStream(),
        new ErrorTO(AUTH_ERROR + authException.getMessage(), UNAUTHORIZED));
  }
}
