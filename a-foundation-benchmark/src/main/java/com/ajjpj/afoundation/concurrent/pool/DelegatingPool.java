package com.ajjpj.afoundation.concurrent.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


/**
 * @author arno
 */
public class DelegatingPool implements APool {
    private final ExecutorService ec;

    public DelegatingPool (ExecutorService ec) {
        this.ec = ec;
    }

    @Override public <T> AFuture<T> submit (Callable<T> code) {
        return new WrappingAFuture<> (ec.submit (code));
    }

    @Override public void shutdown () throws InterruptedException {
        ec.shutdown ();
    }
}
