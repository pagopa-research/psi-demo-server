package it.lockless.psidemoserver.util.exception;

/**
 Exception thrown whenever the client provides a sessionId not stored by the server.
 */
public class SessionNotFoundException extends Exception {
    public SessionNotFoundException(String message) {
        super(message);
    }
    public SessionNotFoundException() {
        super();
    }
}
