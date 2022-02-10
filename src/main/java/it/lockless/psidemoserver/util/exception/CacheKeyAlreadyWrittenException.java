package it.lockless.psidemoserver.util.exception;

/**
 Exception thrown whenever the server is attempting to save a key that already exist, which is
 a behavior not admitted by the PsiCacheProvider interface offered by the PSI SDK.
 */
public class CacheKeyAlreadyWrittenException extends RuntimeException{
    public CacheKeyAlreadyWrittenException() {
        super("The key you are attempting to save already exists in the cache. This behavior is not admissible for implementations of PsiCacheProvider");
    }
}
