package it.lockless.psidemoserver.util.exception;

/**
 Exception thrown whenever the client is attempting to use an algorithm not supported by the sdk.
 */
public class AlgorithmNotSupportedException extends RuntimeException {
    public AlgorithmNotSupportedException(String message) {
        super(message);
    }
    public AlgorithmNotSupportedException() {
        super();
    }
}
