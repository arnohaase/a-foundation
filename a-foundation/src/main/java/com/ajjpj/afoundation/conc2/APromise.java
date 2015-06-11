package com.ajjpj.afoundation.conc2;

/**
 * @author arno
 */
public interface APromise<T> {
    AFuture<T> asFuture();

    boolean tryCompleteSuccessfully (T value);
    void completeSuccessfully (T value);

    boolean tryCompleteExceptionally (Throwable throwable);
    void completeExceptionally (Throwable throwable);

    boolean tryComplete (ATry<T> result);
    void complete (ATry<T> result);

    void completeWith (AFuture<T> f);
}
