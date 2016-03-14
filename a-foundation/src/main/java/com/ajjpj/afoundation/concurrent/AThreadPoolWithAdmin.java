package com.ajjpj.afoundation.concurrent;

import java.util.Collections;
import java.util.List;


public interface AThreadPoolWithAdmin extends AThreadPool {
    enum State { Running, ShuttingDown, Down}
    enum ShutdownMode { ExecuteSubmitted, SkipUnstarted, InterruptRunning }

    AThreadPoolStatistics getStatistics ();

    State getState ();
    List<AFuture<Void>> shutdown (ShutdownMode shutdownMode);



    //TODO static wrap (ExecutorService)

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
