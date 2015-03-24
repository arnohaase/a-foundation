package com.ajjpj.abase.concurrent;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * This interface adds lifecycle methods to {@link ATaskScheduler}.
 *
 * @author arno
 */
public interface AThreadPool extends ATaskScheduler {
    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.<p>
     *
     * This method does not wait for previously submitted tasks to
     * complete execution.  Use {@link #awaitTermination awaitTermination}
     * to do that.<p>
     *
     * See {@link java.util.concurrent.ExecutorService#shutdown ExecutorService.shutdown()}.
     */
    void shutdown ();

    /**
     * Attempts to stop all actively executing tasks, halts the
     *  processing of waiting tasks, and returns a list of the tasks
     *  that were awaiting execution.<p>
     *
     * This method does not wait for actively executing tasks to
     *  terminate.  Use {@link #awaitTermination awaitTermination} to
     *  do that.<p>
     *
     * Currently running tasks will by cancelled on a best-effort basis
     *  by calling {@link Thread#interrupt}, and any task that fails to
     *  respond to interrupts may never terminate.
     *
     * See {@link java.util.concurrent.ExecutorService#shutdownNow ExecutorService.shutdownNow()}.
     *
     * @return list of tasks that never commenced execution
     */
    List<Runnable> shutdownNow ();

    /**
     * Returns {@code true} if this executor has been shut down. See {@link java.util.concurrent.ExecutorService#isShutdown ExecutorService.isShutDown()}.
     *
     * @return {@code true} if this executor has been shut down
     */
    boolean isShutdown ();

    /**
     * Returns {@code true} if all tasks have completed following shut down.
     *  Note that {@link #isTerminated} is never {@code true} unless
     *  either {@link #shutdown} or {@link #shutdownNow} was called first. See
     *  {@link java.util.concurrent.ExecutorService#isTerminated ExecutorService.isTerminated()}.
     *
     * @return {@code true} if all tasks have completed following shut down
     */
    boolean isTerminated ();

    /**
     * Blocks until all tasks have completed execution after a shutdown
     *  request, or the timeout occurs, or the current thread is
     *  interrupted, whichever happens first. See {@link java.util.concurrent.ExecutorService#awaitTermination ExecutorService.awaitTermination()}.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return {@code true} if this executor terminated and
     *         {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    boolean awaitTermination (long timeout, TimeUnit unit) throws InterruptedException;
}


