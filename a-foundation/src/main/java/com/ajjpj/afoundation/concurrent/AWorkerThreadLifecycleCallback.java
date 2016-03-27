package com.ajjpj.afoundation.concurrent;


/**
 * This interface's callbacks are invoked for every worker thread at the specified times. The pre and post start methods are guaranteed
 *  to be invoked for all threads before any work can be submitted to the thread pool. The pre and post die methods are guaranteed to
 *  be invoked after the worker's last work item was finished processing.
 */
public interface AWorkerThreadLifecycleCallback {
    AWorkerThreadLifecycleCallback DEFAULT = new AWorkerThreadLifecycleCallback () {};

    default void onPreStart (Thread workerThread) {}
    default void onPostStart (Thread workerThread) {}

    default void onPreDie (Thread workerThread) {}
    default void onPostDie (Thread workerThread) {}
}
