package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


/**
 * This is the actual thread pool API, allowing tasks to be submitted. Use {@link AThreadPoolBuilder} to create instances
 *  of AThreadPool, or {@link AThreadPool#wrap} to create a facade for any {@link Executor}. <p>
 * There is a rich API for thread pool administration (i.e. shutting it down, or monitoring scheduling details), which is
 *  intentionally separated from AThreadPool which only deals with thread scheduling. Most application code should after
 *  all not be able to shut down a thread pool, so why hand them the API to do that? See {@link AThreadPoolWithAdmin}
 *  for the admin API.<p>
 * AThreadPool's API is intentionally 'bare bones'. {@link AFuture} provides a rich API built on top of AThreadPool,
 *  see {@link AThreadPool#submit} for details.
 */
public interface AThreadPool {
    /**
     * This method submits a task for scheduling and execution in this thread pool. It is AThreadPool's only method, and
     *  it intentional that there is no way for the submitted task to return any value, or notify callers of success
     *  or failure.<p>
     * There is a strict separation between scheduling and execution - which is AThreadPool's responsibility - and
     *  result handling, for which there is {@link AFuture}, and {@link AFuture#submit} in particular. There are several
     *  reasons why this separation was introduced:
     *  <ul>
     *      <li> Simpler yet flexible client code: Fusing the future handling into the thread pool itself (as {@link ExecutorService does}
     *            makes simple applications a little more obvious to write, but real-world code using transformations and other
     *            stuff with futures becomes less obvious to read.
     *      <li> Clean implementations: The separation was quite straight-forward, making both the thread pool and the future
     *            handling code simpler and better readable.
     *      <li> Efficiency: Providing result handling incurs a small but noticeable performance penalty even if it is not
     *            actually used, and separating that from the actual thread pool allows more efficient execution of tasks that
     *            are 'fire and forget'.
     *      <li> Interoperability: Separating the two APIs allows AFuture's rich API to be used with any {@link Executor},
     *            and AThreadPools to be used as {@link ExecutorService}s with minimal adaption overhead.
     *  </ul>
     */
    void submit (Runnable task);

    /**
     * This method wraps an {@link Executor} in an AThreadPool API. This allows application code to be written against
     *  AThreadPool API and to use {@link AFuture} even when using {@link Executor} implementations, e.g. from the
     *  {java.util.concurrent}.
     */
    static AThreadPool wrap (Executor es) {
        return es::execute;
    }

    /**
     * This is a 'thread pool' implementation that executes tasks by executing them immediately, in the caller's thread. This
     *  is obviously not intended as an actual thread pool for application code, and a lot of concurrent application code
     *  would not even work with it. But it can be useful for small pieces of code that are guaranteed to never block, and
     *  for testing.
     */
    AThreadPool SYNC_THREADPOOL = new AThreadPool () {
        @Override public void submit (Runnable code) {
            code.run ();
        }
    };

    /**
     * This method wraps an AThreadPool instance to provide the full {@link ExecutorService} API. This is both less efficient and less expressive than using an AThreadPool directly
     *  with AFutures added where needed, which is why this method intentionally adds syntactic overhead, but it exists nonetheless to facilitate migration and experimentation.<p>
     *
     * It is possible to combine both abstractions, e.g. using an {@link ExecutorService} for submitting work, and {@link AThreadPoolWithAdmin} to initiate and track shutdown.
     */
    static ExecutorService wrapAsExecutorService (AThreadPool pool) {
        return new ExecutorServiceWrapper (pool);
    }
}
