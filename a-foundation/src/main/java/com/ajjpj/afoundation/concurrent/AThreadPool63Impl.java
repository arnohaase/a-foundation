package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AFunction0NoThrow;
import com.ajjpj.afoundation.function.AFunction1NoThrow;
import com.ajjpj.afoundation.function.AStatement1NoThrow;
import com.ajjpj.afoundation.util.AUnchecker;


/**
 * This concrete AThreadPool implementation can handle up to 63 worker threads, optimizing 'scanning' and 'idle' handling
 *  by using a single 64-bit bit set for them.
 */
public class AThreadPool63Impl extends AThreadPoolImpl {
    public AThreadPool63Impl (boolean isDaemon, AFunction0NoThrow<String> threadNameFactory, AStatement1NoThrow<Throwable> exceptionHandler,
                              int numThreads, int localQueueSize, int numSharedQueues, boolean checkShutdownOnSubmission,
                              AFunction1NoThrow<AThreadPoolImpl, ASharedQueue> sharedQueueFactory,
                              int ownLocalFifoInterval, int numPrefetchLocal, int skipLocalWorkInterval, int switchSharedQueueInterval,
                              ASharedQueueAffinityStrategy sharedQueueAffinityStrategy, AWorkerThreadLifecycleCallback workerThreadLifecycleCallback) {
        super (isDaemon, threadNameFactory, exceptionHandler, numThreads, localQueueSize, numSharedQueues, checkShutdownOnSubmission, sharedQueueFactory, ownLocalFifoInterval, numPrefetchLocal, skipLocalWorkInterval, switchSharedQueueInterval, sharedQueueAffinityStrategy, workerThreadLifecycleCallback);

        if (numThreads > 63) {
            throw new IllegalArgumentException ("This implementation supports a maximum of 63 worker threads");
        }
    }

    static final long MASK_IDLE_THREAD_SCANNING = Long.MIN_VALUE; // top-most bit reserved to signify 'scanning'

    /**
     * This long is a bit set with the indexes of threads that are currently idling. All idling threads are guaranteed to be in this set,
     *  but some threads may be marked as idle though they are still settling down or otherwise not quite idle. All modifications are
     *  done as CAS via UNSAFE.<p>
     * This mechanism allows an optimization when threads are unparked: only threads marked as idle need to be unparked, and only one
     *  volatile read is required rather than one per worker thread.
     */
    @SuppressWarnings({"unused"})
    private volatile long idleThreads = 0;

    /**
     * padding at the end of the subclass to prevent false sharing
     */
    @SuppressWarnings ("unused")
    long q1, q2, q3, q4, q5, q6, q7;


    @Override void onAvailableTask () {
        long idleBitMask = UNSAFE.getLongVolatile (this, OFFS_IDLE_THREADS);
        if ((idleBitMask & MASK_IDLE_THREAD_SCANNING) != 0L) {
            // some other thread is scanning, so there is no need to wake another thread
            return;
        }
        doWakeUpWorker (idleBitMask);
    }

    @Override void wakeUpWorker () {
        long idleBitMask = UNSAFE.getLongVolatile (this, OFFS_IDLE_THREADS);
        doWakeUpWorker (idleBitMask);
    }

    private void doWakeUpWorker (long idleBitMask) {
        if ((idleBitMask & ~MASK_IDLE_THREAD_SCANNING) == 0L) {
            // all threads are busy already
            return;
        }

        for (LocalQueue localQueue : localQueues) {
            if ((idleBitMask & 1L) != 0) {
                //noinspection ConstantConditions
                if (markWorkerAsBusyAndScanning (localQueue.thread)) {
                    // wake up the worker only if no-one else woke up the thread in the meantime
                    UNSAFE.unpark (localQueue.thread);
                }
                // even if someone else woke up the thread in the meantime, at least one thread is scanning --> we can safely abort here
                break;
            }
            idleBitMask = idleBitMask >> 1;
        }
    }

    @Override void markWorkerAsIdle (int block, long mask) {
        long prev, after;
        do {
            prev = UNSAFE.getLongVolatile (this, OFFS_IDLE_THREADS);
            after = prev | mask;
        }
        while (! UNSAFE.compareAndSwapLong (this, OFFS_IDLE_THREADS, prev, after));
    }

    @Override boolean markWorkerAsBusy (int block, long mask) {
        long prev, after;
        do {
            prev = UNSAFE.getLongVolatile (this, OFFS_IDLE_THREADS);
            if ((prev & mask) == 0) {
                // someone else woke up the thread in the meantime
                return false;
            }

            after = prev & ~mask;
        }
        while (! UNSAFE.compareAndSwapLong (this, OFFS_IDLE_THREADS, prev, after));

        return true;
    }

    @Override boolean markWorkerAsBusyAndScanning (WorkerThread worker) {
        final long mask = worker.idleThreadMask;
        long prev, after;
        do {
            prev = UNSAFE.getLongVolatile (this, OFFS_IDLE_THREADS);
            if ((prev & mask) == 0L) {
                // someone else woke up the thread concurrently --> it is scanning now, and there is no need to wake it up or change the 'idle' mask
                return false;
            }

            after = prev & ~mask;
            after = after | MASK_IDLE_THREAD_SCANNING;
        }
        while (! UNSAFE.compareAndSwapLong (this, OFFS_IDLE_THREADS, prev, after));
        return true;
    }

    @Override void unmarkScanning() {
        long prev, after;
        do {
            prev = UNSAFE.getLongVolatile (this, OFFS_IDLE_THREADS);
            after = prev & ~MASK_IDLE_THREAD_SCANNING;
            if (prev == after) {
                return;
            }
        }
        while (! UNSAFE.compareAndSwapLong (this, OFFS_IDLE_THREADS, prev, after));
    }



    private static final long OFFS_IDLE_THREADS;

    static {
        try {
            OFFS_IDLE_THREADS = UNSAFE.objectFieldOffset (AThreadPool63Impl.class.getDeclaredField ("idleThreads"));
        }
        catch (Exception e) {
            AUnchecker.throwUnchecked (e);
            throw new RuntimeException(); // for the compiler
        }
    }

}
