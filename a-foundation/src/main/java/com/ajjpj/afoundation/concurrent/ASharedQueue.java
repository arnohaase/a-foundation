package com.ajjpj.afoundation.concurrent;


/**
 * @author arno
 */
public interface ASharedQueue {
    void push (Runnable task);

    /**
     * Returns a task from this queue or null, if there is none, pre-fetching a configured number of task to the given LocalQueue. This method is
     *  only called from the local queue's worker thread.
     */
    Runnable popFifo (LocalQueue localQueue);

    /**
     * for shutdown only
     */
    void clear();

    /**
     * This is an optional method, used only for providing statistics data to aid monitoring and debugging. Implementations should therefore
     *  not require any compromise in regular push and pop performance.
     *
     * @return an approximation to the queue's current size
     */
    int approximateSize ();
}
