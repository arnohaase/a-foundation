package com.ajjpj.abase.concurrent;

import com.ajjpj.abase.collection.tuples.ATuple2;
import com.ajjpj.abase.collection.tuples.ATuple3;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.AStatement1NoThrow;
import com.ajjpj.abase.function.AStatement2NoThrow;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * An instance of <code>AFuture</code> represents a result that may or may not be available yet. Its methods can be
 *  categorized as follows:
 * <ul>
 *     <li> Interaction with the underlying computation. A call to {@link #cancel(boolean)} cancels that computation unless it is already finished, while {@link #isCancelled()}
 *           and {@link #isFinished()} check if the computation was cancelled or finished in whatever way, respectively.
 *     <li> Blocking access to the result. Calling {@link #get()} with or without a timeout will return the result, blocking until it becomes available if necessary. This is
 *           pretty crude, and it wastes resources by blocking a thread. However it makes for straightforward code - but you should consider registering a callback instead.
 *     <li> Registering callbacks. Calling {@link #onSuccess(com.ajjpj.abase.function.AStatement1NoThrow)}, {@link #onFailure(com.ajjpj.abase.function.AStatement1NoThrow)} or
 *           {@link #onFinished(com.ajjpj.abase.function.AStatement2NoThrow)} registers a callback that is triggered when the future completes successfully, fails (i.e. the task
 *           threw an exception or timed out}, or finished either way. Using these callbacks is the preferred way of dealing with results because it does not block any threads.
 *           Callbacks are guaranteed to be executed even if they are registered after the future's computation is finished.
 *     <li> Creating a new <code>AFuture</code> base on this future's result without waiting for it to finish first. You can provide a default value in case this future's
 *           computation fails or times out ({@link #withDefaultValue(Object)}), or you can transform the result {@link #mapSync(com.ajjpj.abase.function.AFunction1) synchronously}
 *           or {@link #mapAsync(com.ajjpj.abase.function.AFunction1, long, java.util.concurrent.TimeUnit) asynchronously}. You can also call one of the <code>zip</code> methods
 *           to combine {@link #zip(AFuture) two} or {@link #zip(AFuture, AFuture) more} <code>AFuture</code>s into one.<p>
 *          This way of working with futures as if they were actual values, but without blocking any threads to wait for intermediate results is one of the key benefits of
 *           using {@link AThreadPool} and <code>AFuture</code>. {@link AFutureHelper} contains more methods for working with <code>AFuture</code> instances.
 * </ul>
 *
 * @author arno
 */
public interface AFuture<T> {
    /**
     * This method cancels the underlying computation unless it is finished already. If the task is not yet running, this method ensures that it never will.
     * @param mayInterruptIfRunning determines whether the underlying thread pool should attempt to interrupt the worker thread if the underlying task is currently running.
     */
    boolean cancel (boolean mayInterruptIfRunning);

    /**
     * @return true if and only if this future was cancelled.
     */
    boolean isCancelled ();

    /**
     * @return true if and only if this future's task is finished, be it through cancellation, success, failure or timeout.
     */
    boolean isFinished ();

    /**
     * @return a new <code>AFuture</code> with a lifecycle that is bound to this <code>AFuture</code>'s. The only difference is that if this future fails, the returned
     *  future will instead finish successfully with the provided <code>defaultValue</code>.
     */
    AFuture<T> withDefaultValue (T defaultValue);

    /**
     * This method returns the future's result, performing a blocking wait until it becomes available. While this waiting looks like it is unbounded, it is actually bounded
     *  by the underlying task's timeout. Nonetheless, this method should be used with extreme care because it can block the caller thread for a potentially very long time.
     *
     * @throws java.lang.InterruptedException if the underlying task was interrupted
     * @throws java.util.concurrent.ExecutionException if the underlying task failed. The actual exception causing the failure is wrapped in the <code>ExecutionException</code>.
     */
    T get () throws InterruptedException, ExecutionException;

    /**
     * This method returns the future's result, performing a blocking wait until it becomes available or this method's timeout is reached. This method should be used with extreme
     *  care because it can block the caller thread.
     *
     * @throws java.lang.InterruptedException if the underlying task was interrupted
     * @throws java.util.concurrent.ExecutionException if the underlying task failed. The actual exception causing the failure is wrapped in the <code>ExecutionException</code>.
     * @throws java.util.concurrent.TimeoutException if this method's timeout was reached
     */
    T get(long timeout, TimeUnit timeoutUnit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Registers a callback that is called if and when the future completes successfully.<p>
     *
     * It is permissible to register several callbacks, in which case they are all called sequentially. Callbacks are guaranteed to be called, even if they are registered
     *  after a future finished.<p>
     *
     * Callbacks are called in a 'borrowed' thread, i.e. either the task's thread (if they are registered before the task is finished) or the caller's thread (if they are
     *  registered after the task is finished). They should typically do only safe and fast work in place, submitting anything else as separate tasks.
     */
    void onSuccess (AStatement1NoThrow<T> callback);

    /**
     * Registers a callback that is called if and when the future fails, i.e. the task throws an exception or times out. The callback receives the cause of failure as a
     *  parameter.<p>
     *
     * It is permissible to register several callbacks, in which case they are all called sequentially. Callbacks are guaranteed to be called, even if they are registered
     *  after a future finished.<p>
     *
     * Callbacks are called in a 'borrowed' thread, i.e. either the task's thread (if they are registered before the task is finished) or the caller's thread (if they are
     *  registered after the task is finished). They should typically do only safe and fast work in place, submitting anything else as separate tasks.
     */
    void onFailure (AStatement1NoThrow<Throwable> callback);

    /**
     * Registers a callback that is called when a future finishes - either successfully or by failing. On success, the callback receives the future's value as its first parameter
     *  while the second parameter is <code>null</code>. On failure, the first paramter is <code>null</code> while the second paramter contains the cause of failure. The
     *  canonical way to distinguish between the two is to compare the second parameter with <code>null</code>.<p>
     *
     * It is permissible to register several callbacks, in which case they are all called sequentially. Callbacks are guaranteed to be called, even if they are registered
     *  after a future finished.<p>
     *
     * Callbacks are called in a 'borrowed' thread, i.e. either the task's thread (if they are registered before the task is finished) or the caller's thread (if they are
     *  registered after the task is finished). They should typically do only safe and fast work in place, submitting anything else as separate tasks.
     */
    void onFinished (AStatement2NoThrow<T, Throwable> callback);

    /**
     * Creates a new <code>AFuture</code> instance with a lifecycle that is bound to this <code>AFuture</code>'s lifecycle, applying a given function <code>f</code> to the result.
     *  This method does for futures what {@link com.ajjpj.abase.collection.immutable.ACollection#map(com.ajjpj.abase.function.AFunction1)} does for collecctions.<p>
     *
     * This method causes the transformation function to be applied in the original future's thread and is thus outside of timeout checks. For fast operations, this saves
     *  scheduling overhead. For non-trivial transformations, use {@link #mapAsync(com.ajjpj.abase.function.AFunction1, long, java.util.concurrent.TimeUnit) mapAsync} instead.
     */
    <U, E extends Exception> AFuture<U> mapSync (AFunction1<T, U, E> f);

    /**
     * Creates a new <code>AFuture</code>. If this future fails, the newly created future fails as well. If this future completes successfully, a new task is scheduled, applying
     *  the transformation function to this future's result. This method does for futures what
     *  {@link com.ajjpj.abase.collection.immutable.ACollection#map(com.ajjpj.abase.function.AFunction1)} does for collecctions.<p>
     *
     * Scheduling is done in this future's underlying thread pool, using the timeout parameter.
     */
    <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f, long timeout, TimeUnit timeoutUnit);

    /**
     * Creates a new <code>AFuture</code>. If this future fails, the newly created future fails as well. If this future completes successfully, a new task is scheduled, applying
     *  the transformation function to this future's result. This method does for futures what
     *  {@link com.ajjpj.abase.collection.immutable.ACollection#map(com.ajjpj.abase.function.AFunction1)} does for collecctions.<p>
     *
     * Scheduling is done in the explicitly provided thread pool, using the timeout parameter.
     */
    <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f, ATaskScheduler threadPool, long timeout, TimeUnit timeoutUnit);

    /**
     * Combines this <code>AFuture</code> with another <code>AFuture</code> into a single future with an {@link com.ajjpj.abase.collection.tuples.ATuple2} of the two values. If either
     *  of the original futures fails, the newly created future fails. If both futures succeed, the newly created future succeeds with the combined value.
     */
    <U> AFuture<ATuple2<T,U>> zip (AFuture<U> other);

    /**
     * Combines this <code>AFuture</code> with two other <code>AFuture</code>s into a single future with an {@link com.ajjpj.abase.collection.tuples.ATuple3} of the three values.
     *  If one of the original futures fails, the newly created future fails. If all three futures succeed, the newly created future succeeds with the combined value.<p>
     *
     * If you want to combine more than three futures into a single future, use {@link AFutureHelper#lift(java.util.Collection)}.
     */
    <U,V> AFuture<ATuple3<T,U,V>> zip (AFuture<U> other1, AFuture<V> other2);
}
