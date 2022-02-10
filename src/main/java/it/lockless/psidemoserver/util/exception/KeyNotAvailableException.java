package it.lockless.psidemoserver.util.exception;

/**
 Exception thrown whenever the server is trying to retrieve a key actually not stored by the keyStore service.
 */
public class KeyNotAvailableException extends RuntimeException {
    public KeyNotAvailableException(String message) {
        super(message);
    }
    public KeyNotAvailableException() {
        super();
    }
}
