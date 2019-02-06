package com.amp.authorization.service;

import javax.validation.constraints.NotNull;

import com.amp.authorization.model.dto.SignInRequestTO;
import com.amp.authorization.model.dto.AccountTO;
import com.amp.authorization.model.dto.AuthResponseTO;
import com.amp.authorization.model.dto.SignInRefreshRequestTO;

public interface AuthService {

  AuthResponseTO signIn(@NotNull SignInRequestTO accountCredentials);

  AuthResponseTO signInRefresh(@NotNull SignInRefreshRequestTO refreshRequest);

  void signOut();

  void signUp(@NotNull AccountTO account);

  AccountTO current();
}
