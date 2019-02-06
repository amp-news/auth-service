package com.amp.authorization.model.security.auth.external;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amp.authorization.model.AccountStatus;
import com.amp.authorization.model.dto.AccountTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static com.amp.authorization.model.AccountStatus.ACTIVE;

@Data
public class GoogleOauth2User implements OAuth2User {

  @JsonIgnore private Long localId;
  private String sub;
  private String name;
  private String email;

  @JsonIgnore private AccountStatus status = ACTIVE;
  @JsonIgnore private String role = "ROLE_JMP_USER";

  @JsonProperty("given_name")
  private String firstName;

  @JsonProperty("family_name")
  private String lastName;

  @JsonProperty("profile")
  private String profileUrl;

  @JsonProperty("picture")
  private String profilePicture;

  @JsonIgnore private Map<String, Object> attributes = new HashMap<>();

  @JsonIgnore private List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(role);

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  public String getRole() {
    return role;
  }

  @Override
  public Map<String, Object> getAttributes() {
    if (attributes.isEmpty()) {
      attributes.put("sub", this.sub);
      attributes.put("name", this.name);
      attributes.put("firstName", this.firstName);
      attributes.put("lastName", this.lastName);
      attributes.put("email", this.email);
      attributes.put("status", this.status.getDisplayName());
      attributes.put("profile", this.profileUrl);
      attributes.put("picture", this.profilePicture);
    }

    return attributes;
  }

  @Override
  public String getName() {
    return email;
  }

  public String getNickname() {
    return String.format("%s_%s", firstName, lastName);
  }

  public void updateFromLocal(AccountTO account) {
    this.localId = account.getId();
    this.firstName = account.getFirstName();
    this.lastName = account.getLastName();
    this.status = account.getStatus();
    this.role = account.getRole();
  }
}
