package org.example.dnsservice.model;

public enum Action {
  REMOVE("remove from rotation"),
  ADD("add to rotation");

  private final String description;

  Action(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
