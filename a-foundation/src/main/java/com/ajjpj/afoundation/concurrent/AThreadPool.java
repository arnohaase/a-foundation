package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


public interface AThreadPool {
    void submit (Runnable task);

    static AThreadPool wrap (Executor es) {
        return task -> es.execute (task);
    }

    AThreadPool SYNC_THREADPOOL = new AThreadPool () {
        @Override public void submit (Runnable code) {
            code.run ();
        }
    };


    /**
     * This method wraps an AThreadPool instance to provide the full ExecutorService API. This is both less efficient and less expressive than using an AThreadPool directly
     *  with AFutures added where needed, which is why this method intentionally adds syntactic overhead, but it exists nonetheless to facilitate migration and experimentation.<p>
     *
     * It is possible to combine both abstractions, e.g. using an ExecutorService for submitting work, and AThreadPoolWithAdmin to initiate and track shutdown.
     */
    static ExecutorService wrapAsExecutorService (AThreadPool pool) {
        return new ExecutorServiceWrapper (pool);
    }
}
