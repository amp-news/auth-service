package com.amp.authorization.model.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SignInRefreshRequestTO {

  @NotNull private String refreshToken;
}
