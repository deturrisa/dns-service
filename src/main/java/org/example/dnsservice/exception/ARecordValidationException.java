package org.example.dnsservice.exception;

public class ARecordValidationException extends RuntimeException {
  public ARecordValidationException(String message) {
    super(message);
  }
}
