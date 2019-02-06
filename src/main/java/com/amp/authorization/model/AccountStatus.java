package com.amp.authorization.model;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountStatus {
  ACTIVE("Active"),
  INACTIVE("Inactive");

  private String name;

  AccountStatus(String name) {
    this.name = name;
  }

  @JsonValue
  public String getDisplayName() {
    return this.name;
  }

  @JsonCreator
  public static AccountStatus fromName(String name) {
    return Stream.of(AccountStatus.values())
        .filter(accountStatus -> accountStatus.getDisplayName().equals(name))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
