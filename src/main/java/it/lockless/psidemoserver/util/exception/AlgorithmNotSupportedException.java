package it.lockless.psidemoserver.util.exception;

public class AlgorithmNotSupportedException extends RuntimeException {
    public AlgorithmNotSupportedException(String message) {
        super(message);
    }
    public AlgorithmNotSupportedException() {
        super();
    }
}
