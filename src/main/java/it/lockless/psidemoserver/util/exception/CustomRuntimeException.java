package it.lockless.psidemoserver.util.exception;

public class CustomRuntimeException extends RuntimeException {
    public CustomRuntimeException() {
        super("Unexpected condition");
    }

    public CustomRuntimeException(String message) {
        super(message);
    }

    public CustomRuntimeException(Exception e) {
        super(e);
    }
}
