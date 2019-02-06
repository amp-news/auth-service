package com.amp.authorization.service.impl;

import com.amp.authorization.service.proxy.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

  private AccountService accountService;

  @Autowired
  public AuthUserDetailsService(AccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return accountService
        .loadByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("There is no user with email " + email));
  }
}
