package com.amp.authorization.model.dto;

import java.util.Collection;
import java.util.Collections;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.amp.authorization.model.AccountStatus;
import com.amp.authorization.model.constraint.Nickname;
import com.amp.authorization.model.constraint.Password;
import com.amp.authorization.model.security.auth.AuthDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static com.amp.authorization.model.AccountStatus.ACTIVE;
import static com.amp.authorization.model.AccountStatus.INACTIVE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class AccountTO implements AuthDetails {

  @JsonIgnore
  private static final String DEFAULT_ROLE = "ROLE_JMP_USER";

  private Long id;

  @Nickname private String nickname;

  @Password private String password;

  @NotEmpty @Email private String email;

  @NotNull
  @Size(max = 100)
  private String firstName;

  @NotNull
  @Size(max = 100)
  private String lastName;

  private AccountStatus status = ACTIVE;
  private String role = DEFAULT_ROLE;

  @Override
  @JsonIgnore
  public Long getPrincipalId() {
    return id;
  }

  @Override
  @JsonIgnore
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return status != INACTIVE;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled() {
    return true;
  }

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(new SimpleGrantedAuthority(role));
  }
}
