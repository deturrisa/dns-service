package org.example.dnsservice.exception;

public class ServerEntryException extends RuntimeException {
    public ServerEntryException(String message) {
        super(message);
    }

    public ServerEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}
