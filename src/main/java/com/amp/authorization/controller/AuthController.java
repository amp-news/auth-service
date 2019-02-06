package com.amp.authorization.controller;

import javax.validation.Valid;

import com.amp.authorization.model.dto.SignInRequestTO;
import com.amp.authorization.model.dto.AccountTO;
import com.amp.authorization.model.dto.AuthResponseTO;
import com.amp.authorization.model.dto.SignInRefreshRequestTO;
import com.amp.authorization.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/token-retrieve")
  @ResponseStatus(OK)
  public void token() {
  }

  @PostMapping("/sign-in")
  @ResponseStatus(OK)
  public AuthResponseTO signIn(@RequestBody @Valid SignInRequestTO accountCredentials) {
    return authService.signIn(accountCredentials);
  }

  @PostMapping("/sign-in-refresh")
  @ResponseStatus(OK)
  public AuthResponseTO signInRefresh(@RequestBody @Valid SignInRefreshRequestTO refreshRequest) {
    return authService.signInRefresh(refreshRequest);
  }

  @PostMapping("/sign-out")
  @ResponseStatus(OK)
  public void signOut() {
    authService.signOut();
  }

  @PostMapping("/sign-up")
  @ResponseStatus(OK)
  public void signUp(@RequestBody @Valid AccountTO account) {
    authService.signUp(account);
  }

  @GetMapping("/current")
  @ResponseStatus(OK)
  public AccountTO current() {
    return authService.current();
  }
}
