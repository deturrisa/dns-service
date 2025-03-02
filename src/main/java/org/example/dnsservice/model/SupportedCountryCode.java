package org.example.dnsservice.model;

public enum SupportedCountryCode {
  US("US"),
  DE("DE"),
  HK("HK"),
  CH("CH");

  private final String countryCode;

  SupportedCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getSubdomain() {
    return switch (this) {
      case HK -> "hongkong";
      case CH -> "switzerland";
      case US -> "usa";
      case DE -> "germany";
    };
  }
}
