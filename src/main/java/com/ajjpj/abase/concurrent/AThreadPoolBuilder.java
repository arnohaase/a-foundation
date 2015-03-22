package com.ajjpj.abase.concurrent;

import java.util.concurrent.*;


/**
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

    public AThreadPoolBuilder setInterruptOnTimeout (boolean interrupt) {
        this.interruptOnTimeout = interrupt;
        return this;
    }

    public AThreadPoolBuilder setReclaimTimeoutForUnusedThreads (long duration, TimeUnit timeUnit) {
        this.unusedThreadThreshold = duration;
        this.unusedThreadThresholdUnit = timeUnit;
        return this;
    }

    public AThreadPoolBuilder setWorkQueue (BlockingQueue<Runnable> queue) {
        this.workQueue = queue;
        return this;
    }

    public AThreadPoolBuilder setThreadFactory (ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public AThreadPoolBuilder setRejectedExecutionHandler (RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        return this;
    }

    public AThreadPool buildSynchronous () {
        return new ASyncThreadPool ();
    }

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

    public AThreadPool buildDynamicSize (int coreSize, int maxSize) {
        return new AThreadPoolImpl (coreSize, maxSize, unusedThreadThreshold, unusedThreadThresholdUnit, new SynchronousQueue<Runnable> (), threadFactory, rejectedExecutionHandler, interruptOnTimeout);
    }
}
