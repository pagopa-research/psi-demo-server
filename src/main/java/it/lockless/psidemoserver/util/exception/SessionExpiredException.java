package it.lockless.psidemoserver.util.exception;

/**
 Exception thrown whenever the client is attempting to work on an expired session.
 */
public class SessionExpiredException extends Exception {
    public SessionExpiredException(String message) {
        super(message);
    }
    public SessionExpiredException() {
        super();
    }
}
