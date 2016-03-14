package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.concurrent.jdk.j9new.ForkJoinPool;
import com.ajjpj.afoundation.concurrent.jdk.j9new.ForkJoinTask;
import com.ajjpj.afoundation.concurrent.jdk.j9new.ForkJoinWorkerThread;

import java.util.concurrent.Callable;


/**
 * @author arno
 */
public class J9NewForkingPool implements ABenchmarkPool {
    private final ForkJoinPool ec;

    public J9NewForkingPool (ForkJoinPool ec) {
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
