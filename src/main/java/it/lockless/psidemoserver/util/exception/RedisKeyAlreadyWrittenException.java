package it.lockless.psidemoserver.util.exception;

public class RedisKeyAlreadyWrittenException extends RuntimeException{
    public RedisKeyAlreadyWrittenException() {
        super("The key you are attempting to save already exists in the cache. This behavior is not admissible for implementations of PsiCacheProvider");
    }
}
