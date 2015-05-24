package com.ajjpj.afoundation.concurrent.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;


/**
 * @author arno
 */
public interface APool {
    <T> AFuture<T> submit (Callable<T> code);
    void shutdown() throws InterruptedException;
}
