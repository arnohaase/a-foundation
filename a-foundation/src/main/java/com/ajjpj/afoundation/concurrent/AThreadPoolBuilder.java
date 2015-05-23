package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.*;


/**
 * This class provides an API for creating {@link AThreadPool} instances. The way to do that is to
 * <ol>
 * <li> create a new <code>AThreadPoolBuilder</code> instance,
 * <li> configure it by calling setter methods, and
 * <li> call one of the <code>create...</code> methods to actually create the thread pool.
 * </ol>
 *
 * Setter methods return the thread pool builder instance to provide a 'fluent API'.<p>
 *
 * There are three basic kinds of AThreadPools that can be built with this class. The following sections
 *  deal with them in turn. <p>
 *
 * <em>Fixed size</em> thread pools have a fixed number of threads, which are created on thread pool creation
 *  and will live until the thread pool is shut down. When a task is submitted and there is an idle thread, the
 *  task is started on that thread immediately. If there is no idle thread when a task is submitted, the task
 *  is parked in the thread pool's queue. Call {@link #buildFixedSize(int)} to create a fixed size thread pool.<p>
 *
 * <em>Dynamic size</em> thread pools are created with a number of <em>core</em> threads that is always maintained
 *  throughout the pool's life cycle. If there is an idling thread when a task is submitted, the task is started on
 *  that thread. If there is no idling thread, the pool will start a new thread for the task up to the <em>maximum</em>
 *  number of threads. If that number of threads has been reached and all threads are busy when a task is submitted,
 *  that task is rejected by throwing a {@link java.util.concurrent.RejectedExecutionException}. Dynamic size pools
 *  will never queue submitted tasks. Idling threads will be released after a timeout (see {@link #setReclaimTimeoutForUnusedThreads(long, java.util.concurrent.TimeUnit)}),
 *  but the pool's core size will always be maintained. Call {@link #buildDynamicSize(int, int)} to create a dynamic size thread pool.<p>
 *
 * <em>Synchronous</em> thread pools do not schedule submitted tasks to other threads at all, but execute them
 *  immediately on the caller's thread. They do however provide the error handling and callback semantics
 *  specified for {@link AThreadPool}s. Synchronous pools are useful mainly for functional testing. Call
 *  {@link #buildSynchronous()} to create a synchronous thread pool.
 *
 * @author arno
 */
public class AThreadPoolBuilder {
    private boolean interruptOnTimeout = false;

    private long unusedThreadThreshold = 10;
    private TimeUnit unusedThreadThresholdUnit = TimeUnit.MINUTES;

    private BlockingQueue<Runnable> workQueue = null; // null means 'default for the implementation'

    private ThreadFactory threadFactory = Executors.defaultThreadFactory ();
    private RejectedExecutionHandler rejectedExecutionHandler = defaultHandler;

    private static final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.AbortPolicy ();

    /**
     * This method specifies whether the thread pool should attempt to interrupt a running task when its timeout is reached. The default is <code>false</code> to be
     *  on the safe side.
     */
    public AThreadPoolBuilder setInterruptOnTimeout (boolean interrupt) {
        this.interruptOnTimeout = interrupt;
        return this;
    }

    /**
     * This method specifies the duration after which a dynamic size thread pool should release unused threads (down to its core size). The default is 10 minutes.
     */
    public AThreadPoolBuilder setReclaimTimeoutForUnusedThreads (long duration, TimeUnit timeUnit) {
        this.unusedThreadThreshold = duration;
        this.unusedThreadThresholdUnit = timeUnit;
        return this;
    }

    /**
     * This method specifies the thread pool's queue. The default is to use a {@link java.util.concurrent.LinkedBlockingQueue}. This parameter
     *  is only used by fixed size thread pools.
     */
    public AThreadPoolBuilder setWorkQueue (BlockingQueue<Runnable> queue) {
        this.workQueue = queue;
        return this;
    }

    /**
     * This method specifies the thread factory to be used by the thread pool.
     */
    public AThreadPoolBuilder setThreadFactory (ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * See {@link java.util.concurrent.ThreadPoolExecutor#setRejectedExecutionHandler(java.util.concurrent.RejectedExecutionHandler)}
     */
    public AThreadPoolBuilder setRejectedExecutionHandler (RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        return this;
    }

    /**
     * Creates a 'thread pool' that executes all submitted tasks in the caller's thread without maintaining either a queue or worker threads. It does however maintain
     *  error handling and callback semantics specified by {@link ATaskScheduler}. It is useful mainly for functional testing.
     */
    public AThreadPool buildSynchronous () {
        return new ASyncThreadPool ();
    }

//    public AThreadPool buildForkJoin (int size) {
//        return new AThreadPoolFjImpl (size, interruptOnTimeout);
//    }

    /**
     * Creates a fixed size thread pool of the given size.
     */
    public AThreadPool buildFixedSize (int size) {
        final BlockingQueue<Runnable> queue;
        if (workQueue != null) {
            queue = workQueue;
        }
        else {
            queue = new LinkedBlockingQueue<> ();
        }

        return new AThreadPoolImpl (size, size, 0, TimeUnit.MILLISECONDS, queue, threadFactory, rejectedExecutionHandler, interruptOnTimeout);
    }

    /**
     * Creates a thread pool that can grow and shrink dynamically. It always maintains a minimum of <code>coreSize</code> threads, allocating new threads up to a maximum
     *  number of <code>maxSize</code> if there are no idling threads when a task is submitted. When that limit is reached, new submissions are rejected by throwing
     *  {@link java.util.concurrent.RejectedExecutionException}s; dynamic size thread pools will never queue submitted tasks. Idling threads are released after the timeout
     *  specified with {@link #setReclaimTimeoutForUnusedThreads(long, java.util.concurrent.TimeUnit)}.
     */
    public AThreadPool buildDynamicSize (int coreSize, int maxSize) {
        return new AThreadPoolImpl (coreSize, maxSize, unusedThreadThreshold, unusedThreadThresholdUnit, new SynchronousQueue<Runnable> (), threadFactory, rejectedExecutionHandler, interruptOnTimeout);
    }
}
