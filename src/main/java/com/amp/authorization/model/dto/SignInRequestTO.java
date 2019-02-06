package com.amp.authorization.model.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import com.amp.authorization.model.constraint.Password;
import lombok.Data;

@Data
public class SignInRequestTO {

  @NotEmpty
  @Email
  private String email;

  @Password
  private String password;
}
