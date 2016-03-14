package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;


/**
 * @author arno
 */
public class ForkJoinForkingPool implements ABenchmarkPool {
    private final ForkJoinPool ec;

    public ForkJoinForkingPool (ForkJoinPool ec) {
        this.ec = ec;
    }

    @Override public <T> ABenchmarkFuture<T> submit (Callable<T> code) {
        if (Thread.currentThread () instanceof ForkJoinWorkerThread) {
            final ForkJoinTask<T> task = ForkJoinTask.adapt (code);
            task.fork ();
            return new WrappingAFuture<> (task);
        }

        return new WrappingAFuture<> (ec.submit (code));
    }

    @Override public void shutdown () throws InterruptedException {
        ec.shutdown ();
    }
}
