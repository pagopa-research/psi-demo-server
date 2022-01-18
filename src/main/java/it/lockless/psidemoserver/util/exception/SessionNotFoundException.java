package it.lockless.psidemoserver.util.exception;

public class SessionNotFoundException extends Exception {
    public SessionNotFoundException(String message) {
        super(message);
    }
    public SessionNotFoundException() {
        super();
    }
}
