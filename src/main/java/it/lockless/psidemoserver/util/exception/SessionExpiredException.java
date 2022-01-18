package it.lockless.psidemoserver.util.exception;

public class SessionExpiredException extends Exception {
    public SessionExpiredException(String message) {
        super(message);
    }
    public SessionExpiredException() {
        super();
    }
}
