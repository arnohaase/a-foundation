package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.Callable;


public class AThreadPoolAdapter implements ABenchmarkPool {
    final AThreadPoolWithAdmin inner;

    public AThreadPoolAdapter (AThreadPoolWithAdmin inner) {
        this.inner = inner;
    }

    @Override public void submit (Runnable code) {
        inner.submit (code);
    }

    @Override public AThreadPoolStatistics getStatistics () {
        return inner.getStatistics ();
    }

    @Override public <T> ABenchmarkFuture<T> submit (Callable<T> code) {
        final SettableFutureTask<T> f = new SettableFutureTask<> (code);
        final WrappingAFuture<T> result = new WrappingAFuture<> (f);

        submit (() -> {
            try {
                f.set (code.call ());
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
        });


        return result;
    }

    @Override public void shutdown () throws InterruptedException {
        //TODO blocking wait
        inner.shutdown (AThreadPoolWithAdmin.ShutdownMode.ExecuteSubmitted);
    }
}
