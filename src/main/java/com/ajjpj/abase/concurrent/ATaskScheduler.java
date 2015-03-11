package com.ajjpj.abase.concurrent;

import com.ajjpj.abase.function.AFunction1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * @author arno
 */
public interface ATaskScheduler {
    <T> AFuture<T> submit (Callable<T> task, long timeout, TimeUnit timeoutUnit);
    <T> AFuture<T> submit (Runnable task, T result, long timeout, TimeUnit timeoutUnit);
    <T,R,E extends Exception> List<AFuture<R>> submitAll                 (List<T> params, AFunction1<T, Callable<R>, E> taskFunction, long timeout, TimeUnit timeoutUnit) throws E;
    <T,R,E extends Exception> List<AFuture<R>> submitAllWithDefaultValue (List<T> params, AFunction1<T, Callable<R>, E> taskFunction, long timeout, TimeUnit timeoutUnit, R defaultValue) throws E;
}
