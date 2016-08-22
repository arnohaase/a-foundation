package com.ajjpj.afoundation.concurrent;

import java.util.Collections;
import java.util.List;


/**
 * This interface provides methods for shutting down and monitoring an {@link AThreadPool}. It is separated from
 *  the 'submit' API to allow an {@link AThreadPool} to be passed around without giving all parts of an application
 *  access to the admin API.<p>
 */
public interface AThreadPoolWithAdmin extends AThreadPool {
    /**
     * This enum represents the possible states of an AThreadPool
     */
    enum State { Running, ShuttingDown, Down}

    /**
     * This enum represents the strategies for dealing with existing tasks during shutdown.
     */
    enum ShutdownMode { ExecuteSubmitted, SkipUnstarted, InterruptRunning }

    /**
     * This method provides detailed statistics on thread usage and scheduling internals. It is written to be called
     *  during normal operations with minimal impact on performance.
     */
    AThreadPoolStatistics getStatistics ();

    /**
     * This method returns the thread pool's current state. While the returned value may be outdated by the time it is
     *  processed, it can still give some indication.
     */
    State getState ();

    /**
     * This method triggers a shutdown of the thread pool, causing its state to transition to 'ShuttingDown' immediately
     *  and on to 'Down' once all threads have completed.
     *
     * @param shutdownMode determines how tasks are handled that were submitted before the call to shutdown(), but not
     *                     yet completely executed.
     *                     <ul>
     *                     <li> ExecuteSubmitted delays shutdown until all previously submitted tasks were completely
     *                           executed. This is the most conservative approach to shutdown.
     *                     <li> SkipUnstarted finishes all currently running tasks, but discards all tasks that
     *                           were submitted but not yet started.
     *                     </ul> InterruptRunning discards all tasks that were not yet started, and attempts to
     *                           {@link Thread#interrupt interrupt} all tasks that are currently running. This is the
     *                           most aggressive approach to shutdown.
     *
     * @return This method returns a list of {@link AFuture AFutures}, one for each of the thread pool's threads. This
     *         allows application code to react individually to each thread's termination, or to combine them into
     *         a single AFuture using {@link AFuture#lift} and react to the entire pool's successful termination.
     */
    List<AFuture<Void>> shutdown (ShutdownMode shutdownMode);

    /**
     * This method adds a non-functional 'dummy' admin API to any given AThreadPool. This probably has limited usefulness
     *  in application code, but it allows e.g. wrapped {@link java.util.concurrent.Executor} instances to be used
     *  in code that uses the admin API.
     */
    static AThreadPoolWithAdmin withDummyAdminApi (AThreadPool pool) {
        if (pool instanceof AThreadPoolWithAdmin) {
            return (AThreadPoolWithAdmin) pool;
        }

        return new AThreadPoolWithAdmin () {
            @Override public AThreadPoolStatistics getStatistics () {
                return AThreadPoolStatistics.NONE;
            }

            @Override public State getState () {
                return State.Running;
            }

            @Override public List<AFuture<Void>> shutdown (ShutdownMode shutdownMode) {
                return Collections.emptyList ();
            }

            @Override public void submit (Runnable task) {
                pool.submit (task);
            }
        };
    }
}
