package com.amp.authorization.model.security.auth;

import com.amp.authorization.model.AccountStatus;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthDetails extends UserDetails {

  Long getPrincipalId();
  String getRole();
  AccountStatus getStatus();
}
