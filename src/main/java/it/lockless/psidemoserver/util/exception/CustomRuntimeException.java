package it.lockless.psidemoserver.util.exception;

/**
 Generic runtime exception thrown when an unexpected and unclassified event happens during the execution.
 */
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
