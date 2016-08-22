package com.ajjpj.afoundation.concurrent;


import com.ajjpj.afoundation.collection.immutable.ATry;

/**
 * This exception is used by {@link ATry#inverse()} } or {@link AFuture#inverse()} to express that the original AFuture completed successfully.
 */
public class SuccessfulCompletionException extends RuntimeException {
    private final Object originalResult;

    public SuccessfulCompletionException (Object originalResult) {
        this.originalResult = originalResult;
    }

    /**
     * @return the 'success' value result
     */
    public Object getOriginalResult () {
        return originalResult;
    }

    @Override public synchronized Throwable fillInStackTrace () {
        return this;
    }
}
