package com.amp.authorization.model.security.auth;

import java.util.Date;

public interface RefreshableToken {

  Long getPrincipalId();

  Date getTokenExpiresAt();

  String getToken();

  String getRefreshToken();
}
