package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AFunction1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * A TaskScheduler can schedule tasks to threads, i.e. it represents a thread pool. While similar to an <code>ExecutorService</code>, it however requires an explicit timeout
 *  to be specified for every task. The timeout is measured from the moment the task is <em>submitted</em>. If the timeout is reached before the task is <em>completed</em>,
 *  the task fails at that moment, triggering failure callbacks on the returned {@link AFuture AFuture}. <p>
 *
 * The result of submitting a task is an {@link AFuture AFuture} instance. While similar in principle to {@link java.util.concurrent.Future java.util.concurrent.Future}, it supports functional
 *  and non-blocking programming.
 *
 * @author arno
 */
public interface ATaskScheduler {
    /**
     * Submits a task with a given timeout.
     */
    <T> AFuture<T> submit (Callable<T> task,        long timeout, TimeUnit timeoutUnit);

    /**
     * Submits a task without a result, and a separate result value. If the task finishes without an exception and before the timeout occurs, the explicit result value is
     *  bound to the resulting future.
     */
    <T> AFuture<T> submit (Runnable task, T result, long timeout, TimeUnit timeoutUnit);

    /**
     * This is a convenience method for submitting a list of task based on a list of input values. Each task consists of applying the <code>taskFunction</code> parameter
     *  to one of the input values.<p>
     *
     * Put differently, this method loops over the param values. For each of them, it submits a <code>Callable</code> that applies the <code>taskFunction</code> to that
     *  value.<p>
     *
     * The timeout applies to every submitted task separately.
     */
    <T,R,E extends Exception> List<AFuture<R>> submitAll (List<T> params, AFunction1<T, R, E> taskFunction, long timeout, TimeUnit timeoutUnit);

    /**
     * This is a convenience method that is simular to <code>submitAll</code>. The only difference is that this method provides every returned future with a default value as
     *  if calling {@link AFuture#withDefaultValue future.withDefaultValue(defaultValue)} on each of the returned futures.<p>
     *
     * Put differently, the returned futures will never fail but return the default value whenever they would otherwise have failed.
     */
    <T,R,E extends Exception> List<AFuture<R>> submitAllWithDefaultValue (List<T> params, AFunction1<T, R, E> taskFunction, long timeout, TimeUnit timeoutUnit, R defaultValue);
}
