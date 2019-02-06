package com.amp.authorization.model.security.auth;

public enum TokenType {
  ACCESS("Access"),
  REFRESH("Refresh");

  private String name;

  TokenType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
