package com.ajjpj.afoundation.concurrent.pool.a;

/**
 * @author arno
 */
public interface ALocalSubmissionQueue {
    Runnable SHUTDOWN = () -> {}; //TODO this should have package visibility (?)

    /**
     * called only from the owning thread
     */
    void submit (Runnable task);

    /**
     * called from any thread
     */
    void submitShutdown();
}
