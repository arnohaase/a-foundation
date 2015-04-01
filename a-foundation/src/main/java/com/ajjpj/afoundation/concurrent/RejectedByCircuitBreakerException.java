package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.TimeoutException;


/**
 * This is a special kind of {@link java.util.concurrent.TimeoutException} that is thrown by
 *  {@link ACircuitBreaker}s for tasks that are not even scheduled because the circuit is
 *  currently interrupted.<p>
 *
 * It appears in the same places a regular {@link java.util.concurrent.TimeoutException} would
 *  appear: It is passed into callbacks 'as is', while (blocking) calls to {@link AFuture#get()}
 *  will throw an {@link java.util.concurrent.ExecutionException} with a
 *  {@link RejectedByCircuitBreakerException} as its cause.
 *
 * @author arno
 */
public class RejectedByCircuitBreakerException extends TimeoutException {
    RejectedByCircuitBreakerException (String message) {
        super (message);
    }
}
