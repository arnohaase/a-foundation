package com.ajjpj.afoundation.concurrent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;


class ExecutorServiceWrapper extends AbstractExecutorService {
    private final AThreadPool pool;
    private final AThreadPoolWithAdmin poolWithAdmin;
    private final AtomicReference<AFuture<?>> shutdown = new AtomicReference<> ();

    ExecutorServiceWrapper (AThreadPool pool) {
        this.pool = pool;
        if (pool instanceof AThreadPoolWithAdmin) {
            this.poolWithAdmin = (AThreadPoolWithAdmin) pool;
        }
        else {
            this.poolWithAdmin = null;
        }
    }

    @Override public void execute (Runnable command) {
        pool.submit (command);
    }

    //------------------- shutdown handling below this point ---------------------

    @Override public void shutdown () {
        if (poolWithAdmin == null) throw new UnsupportedOperationException ("shutdown only supported for AThreadPoolWithAdmin");
        shutdown.set (AFuture.lift (AThreadPool.SYNC_THREADPOOL, poolWithAdmin.shutdown (AThreadPoolWithAdmin.ShutdownMode.ExecuteSubmitted)));
    }

    @Override public List<Runnable> shutdownNow () {
        if (poolWithAdmin == null) throw new UnsupportedOperationException ("shutdown only supported for AThreadPoolWithAdmin");
        shutdown.set (AFuture.lift (AThreadPool.SYNC_THREADPOOL, poolWithAdmin.shutdown (AThreadPoolWithAdmin.ShutdownMode.SkipUnstarted)));
        System.err.println ("Work may have been lost - keeping track of unfinished work on pool shutdown is not supported by AThreadPool");
        return Collections.emptyList ();
    }

    @Override public boolean isShutdown () {
        return shutdown.get() != null;
    }

    @Override public boolean isTerminated () {
        return isShutdown() && shutdown.get().isComplete ();
    }

    @Override public boolean awaitTermination (long timeout, TimeUnit unit) throws InterruptedException {
        if (!isShutdown ()) return false;

        try {
            shutdown.get().await (timeout, unit);
            return true;
        }
        catch (TimeoutException e) {
            return false;
        }
    }
}
