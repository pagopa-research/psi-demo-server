package it.lockless.psidemoserver.util.exception;

/**
 Exception thrown whenever the client is attempting to use a keySize not supported by the SDK for the selected algorithm.
 */
public class AlgorithmInvalidKeyException extends Exception {
    public AlgorithmInvalidKeyException(String message) {
        super(message);
    }
}
