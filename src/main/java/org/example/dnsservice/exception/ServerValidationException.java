package org.example.dnsservice.exception;

public class ServerValidationException extends RuntimeException {
  public ServerValidationException(String message) {
    super(message);
  }
}
