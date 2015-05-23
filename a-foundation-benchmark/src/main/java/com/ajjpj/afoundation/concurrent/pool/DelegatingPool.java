package com.ajjpj.afoundation.concurrent.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * @author arno
 */
public class DelegatingPool implements Pool {
    private final ExecutorService ec;

    public DelegatingPool (ExecutorService ec) {
        this.ec = ec;
    }

    @Override public <T> Future<T> submit (Callable<T> code) {
        return ec.submit (code);
    }

    @Override public void shutdown () throws InterruptedException {
        ec.shutdown ();
    }
}
