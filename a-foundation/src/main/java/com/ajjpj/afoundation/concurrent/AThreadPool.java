package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.Executor;


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
}
