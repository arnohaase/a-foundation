package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AFunction0NoThrow;
import com.ajjpj.afoundation.function.AFunction1NoThrow;
import com.ajjpj.afoundation.function.AStatement1NoThrow;
import com.ajjpj.afoundation.util.AUnchecker;
import sun.misc.Contended;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author arno
 */
@Contended
public abstract class AThreadPoolImpl implements AThreadPoolWithAdmin {
    private static final int MAX_NUM_PRODUCER_AFFINITIES = 10_000;

    @SuppressWarnings ("unused")
    long p1, p2, p3, p4, p5, p6, p7;

    /**
     * This is a compile-time switch to completely remove all statistics gathering for the really paranoid
     */
    public static final boolean SHOULD_GATHER_STATISTICS = true;

    private final ASharedQueue[] sharedQueues;
    final LocalQueue[] localQueues;

    private final Map<Integer, Integer> producerToQueueAffinity = new ConcurrentHashMap<> ();
    private final ASharedQueueAffinityStrategy sharedQueueAffinityStrategy;
    private final AWorkerThreadLifecycleCallback workerThreadLifecycleCallback;

    final AtomicBoolean shutdown = new AtomicBoolean (false);
    final boolean checkShutdownOnSubmission;

    // padding in subclasses

    public AThreadPoolImpl (boolean isDaemon, AFunction0NoThrow<String> threadNameFactory, AStatement1NoThrow<Throwable> exceptionHandler,
                            int numThreads, int localQueueSize, int numSharedQueues, boolean checkShutdownOnSubmission, AFunction1NoThrow<AThreadPoolImpl, ASharedQueue> sharedQueueFactory,
                            int ownLocalFifoInterval, int numPrefetchLocal, int skipLocalWorkInterval, int switchSharedQueueInterval, ASharedQueueAffinityStrategy sharedQueueAffinityStrategy,
                            AWorkerThreadLifecycleCallback workerThreadLifecycleCallback) {
        this.sharedQueueAffinityStrategy = sharedQueueAffinityStrategy;
        this.workerThreadLifecycleCallback = workerThreadLifecycleCallback;
        if (numPrefetchLocal >= localQueueSize - 2) {
            throw new IllegalArgumentException ("prefetch number must be smaller than local queue size");
        }

        this.checkShutdownOnSubmission = checkShutdownOnSubmission;
        sharedQueues = new ASharedQueue[numSharedQueues];
        for (int i=0; i<numSharedQueues; i++) {
            sharedQueues[i] = sharedQueueFactory.apply (this);
        }

        final Set<Integer> sharedQueuePrimes = primeFactors (numSharedQueues);

        localQueues = new LocalQueue[numThreads];
        for (int i=0; i<numThreads; i++) {
            localQueues[i] = new LocalQueue (this, localQueueSize);
            final WorkerThread thread = new WorkerThread (ownLocalFifoInterval, skipLocalWorkInterval, switchSharedQueueInterval, numPrefetchLocal, localQueues[i], sharedQueues, this, i, prime (i, sharedQueuePrimes), exceptionHandler);
            //TODO onCreatedThread callback --> core affinity etc. --> ThreadLifecycleCallback: onPostStart, onPreFinish
            thread.setDaemon (isDaemon);
            thread.setName (threadNameFactory.apply ());
            localQueues[i].init (thread);

            workerThreadLifecycleCallback.onPreStart (thread);
        }

        for (LocalQueue queue: localQueues) {
            //noinspection ConstantConditions
            queue.thread.start ();
            workerThreadLifecycleCallback.onPostStart (queue.thread);
        }
    }

    /**
     * This method returns an approximation of statistical data for all worker threads since the pool was started. Updates of the statistical data is done without synchronization,
     *  so some or all of the data may be stale, and some numbers may be pretty outdated while others are very current, even for the same thread. For long-running pools however
     *  the data may be useful in analyzing behavior in general and performance anomalies in particular. Your mileage may vary, you have been warned! ;-)
     */
    @Override public AThreadPoolStatistics getStatistics() {
        final AWorkerThreadStatistics[] workerStats = new AWorkerThreadStatistics[localQueues.length];
        for (int i=0; i<localQueues.length; i++) {
            //noinspection ConstantConditions
            workerStats[i] = localQueues[i].thread.getStatistics ();
        }

        final ASharedQueueStatistics[] sharedQueueStats = new ASharedQueueStatistics[sharedQueues.length];
        for (int i=0; i<sharedQueues.length; i++) {
            sharedQueueStats[i] = new ASharedQueueStatistics (sharedQueues[i].approximateSize());
        }

        return new AThreadPoolStatistics (workerStats, sharedQueueStats);
    }

    static Set<Integer> primeFactors (int n) {
        final Set<Integer> result = new HashSet<> ();

        for (int i=2; i<=n; i++) {
            if (isPrime (i) && n%i == 0) {
                result.add (i);
            }
        }

        return result;
    }

    static int prime (int number, Set<Integer> exceptions) {
        int numPrevPrimes = 0;

        for (int candidate=1; candidate<Integer.MAX_VALUE; candidate++) {
            if (isPrime (candidate) && !exceptions.contains (candidate)) {
                if (numPrevPrimes >= number) {
                    return candidate;
                }
                numPrevPrimes += 1;
            }
        }
        return 1; // this should never happen, but '1' is a safe fallback
    }

    static boolean isPrime (int n) {
        for (int i=2; i<=n/2; i++) {
            if (n%i == 0) return false;
        }
        return true;
    }

    @Override public void submit (Runnable code) {
        // check to reject submissions after shutdown, but only if this check is enabled - it does incur a volatile read on each submission, after all
        if (checkShutdownOnSubmission && shutdown.get ()) {
            throw new IllegalStateException ("pool is already shut down");
        }

        final WorkerThread wt;
        final Thread curThread = Thread.currentThread ();
        if (curThread.getClass () == WorkerThread.class && (wt = (WorkerThread) curThread).pool == this) {
            if (SHOULD_GATHER_STATISTICS) wt.stat_numLocalSubmits += 1;
            try {
                wt.localQueue.push (code);
            }
            catch (RejectedExecutionException e) {
                sharedQueues[getSharedQueueForCurrentThread ()].push (code);
            }
        }
        else {
            sharedQueues[getSharedQueueForCurrentThread ()].push (code);
        }
    }

    private int getSharedQueueForCurrentThread() {
        final int key = System.identityHashCode (Thread.currentThread ());

        Integer result = producerToQueueAffinity.get (key);
        if (result == null) {
            if (producerToQueueAffinity.size () > MAX_NUM_PRODUCER_AFFINITIES) {
                // in the unusual situation that producers are transient, discard affinity data if the map gets too large
                producerToQueueAffinity.clear ();
            }

            result = sharedQueueAffinityStrategy.getSharedQueueIndex (Thread.currentThread (), sharedQueues.length);
            producerToQueueAffinity.put (key, result);
        }

        return result;
    }

    @Override public State getState () {
        if (! shutdown.get ()) return State.Running;

        for (LocalQueue q: this.localQueues) {
            if (q.thread.isAlive ()) return State.ShuttingDown;
        }

        return State.Down;
    }

    /**
     * This method shuts down the thread pool. The method finishes immediately, returning a separate AFuture for every worker thread. In order to combine them into a single
     *  AFuture for all worker threads, use {@code AFuture.lift()} on the result.
     */
    @Override public List<AFuture<Void>> shutdown (ShutdownMode shutdownMode) {
        if (! shutdown.compareAndSet (false, true)) {
            throw new IllegalStateException ("pool can be shut down only once");
        }

        if (shutdownMode == ShutdownMode.SkipUnstarted || shutdownMode == ShutdownMode.InterruptRunning) {
            for (ASharedQueue sharedQueue: sharedQueues) {
                sharedQueue.clear ();
            }

            for (LocalQueue queue: localQueues) {
                //noinspection StatementWithEmptyBody
                while (queue.popFifo () != null) {
                    // do nothing, just drain the queue
                }
            }

            if (shutdownMode == ShutdownMode.InterruptRunning) {
                for (LocalQueue queue: localQueues) {
                    queue.thread.interrupt ();
                }
            }
        }

        final List<AFuture<Void>> result = new ArrayList<> ();

        for (LocalQueue localQueue : localQueues) {
            final ASettableFuture<Void> f = ASettableFuture.create ();
            sharedQueues[0].push (() -> {
                workerThreadLifecycleCallback.onPreDie (localQueue.thread);
                throw new PoolShutdown (f);
            });
            UNSAFE.unpark (localQueue.thread);
            result.add (f);

            f.onComplete (AThreadPool.SYNC_THREADPOOL, x -> workerThreadLifecycleCallback.onPostDie (localQueue.thread));
        }

        return result;
    }


    abstract void onAvailableTask ();
    abstract void wakeUpWorker ();

    abstract void markWorkerAsIdle (long mask);
    abstract boolean markWorkerAsBusy (long mask);
    abstract boolean markWorkerAsBusyAndScanning (WorkerThread worker);
    abstract void unmarkScanning();

    //------------------ Unsafe stuff
    static final Unsafe UNSAFE;

    static {
        try {
            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            UNSAFE = (Unsafe) f.get (null);
        }
        catch (Exception e) {
            AUnchecker.throwUnchecked (e);
            throw new RuntimeException(); // for the compiler
        }
    }
}
