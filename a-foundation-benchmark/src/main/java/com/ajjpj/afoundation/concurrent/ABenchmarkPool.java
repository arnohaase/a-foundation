package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.Callable;


/**
 * @author arno
 */
public interface ABenchmarkPool {
    default void submit (Runnable code) {
        submit (() -> {
            code.run ();
            return null;
        });
    }

    default AThreadPoolStatistics getStatistics () {
        return new AThreadPoolStatistics (new AWorkerThreadStatistics[0], new ASharedQueueStatistics[0]);
    }

    <T> ABenchmarkFuture<T> submit (Callable<T> code);
    void shutdown () throws InterruptedException;
}
