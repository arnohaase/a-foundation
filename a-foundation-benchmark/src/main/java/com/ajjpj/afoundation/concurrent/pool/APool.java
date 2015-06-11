package com.ajjpj.afoundation.concurrent.pool;

import com.ajjpj.afoundation.conc2.AFuture;

import java.util.concurrent.Callable;


/**
 * @author arno
 */
public interface APool {
    <T> AFuture<T> submit (Callable<T> code);
    void shutdown() throws InterruptedException;
}
