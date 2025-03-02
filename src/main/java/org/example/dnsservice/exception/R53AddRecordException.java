package org.example.dnsservice.exception;

public class R53AddRecordException extends RuntimeException {
  public R53AddRecordException(String message) {
    super(message);
  }
}
