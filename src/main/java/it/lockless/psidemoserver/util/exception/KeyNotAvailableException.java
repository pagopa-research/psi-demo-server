package it.lockless.psidemoserver.util.exception;

public class KeyNotAvailableException extends RuntimeException {
    public KeyNotAvailableException(String message) {
        super(message);
    }
    public KeyNotAvailableException() {
        super();
    }
}
