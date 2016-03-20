package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AStatement1NoThrow;
import com.ajjpj.afoundation.util.AUnchecker;
import sun.misc.Contended;
import sun.misc.Unsafe;

import java.lang.reflect.Field;


/**
 * @author arno
 */
@Contended
class WorkerThread extends Thread {
    final LocalQueue localQueue;               // accessed only from this thread
    private final ASharedQueue[] sharedQueues; // accessed only from this thread
    private final LocalQueue[] allLocalQueues; // accessed only from this thread
    final AThreadPoolImpl pool;                // accessed only from this thread
    private final int queueTraversalIncrement; // accessed only from this thread
    private final AStatement1NoThrow<Throwable> exceptionHandler; // accessed only from this thread
    private final int numPrefetchLocal;        // accessed only from this thread

    final long idleThreadMask;                 //accessed from arbitrary other thread during thread wake-up

    long padding1, padding2, padding3, padding4, padding5, padding6, padding7;

    //---------------------------------------------------
    //-- statistics data, written only from this thread
    //---------------------------------------------------

    long stat_numTasksExecuted = 0;
    long stat_numSharedTasksExecuted = 0;
    long stat_numSteals = 0;
    long stat_numExceptions = 0;

    long stat_numParks = 0;
    long stat_numFalseAlarmUnparks = 0;
    long stat_numSharedQueueSwitches = 0;

    long stat_numLocalSubmits = 0;

    /**
     * This is the index of the shared queue that this thread currently feeds from.
     */
    private int currentSharedQueue = 0;

    WorkerThread (int numPrefetchLocal, LocalQueue localQueue, ASharedQueue[] sharedQueues, AThreadPoolImpl pool, int threadIdx, int queueTraversalIncrement, AStatement1NoThrow<Throwable> exceptionHandler) {
        this.numPrefetchLocal = numPrefetchLocal;
        this.exceptionHandler = exceptionHandler;

        this.localQueue = localQueue;
        this.sharedQueues = sharedQueues;
        this.pool = pool;
        this.allLocalQueues = pool.localQueues;
        idleThreadMask = 1L << threadIdx;
        this.queueTraversalIncrement = queueTraversalIncrement;

        currentSharedQueue = threadIdx % sharedQueues.length;
    }

    /**
     * This method returns an approximation of this thread's execution statistics for the entire period since the thread was started. Writes are done without memory barriers
     *  to minimize the performance impact of statistics gathering, so some or all returned data may be arbitrarily stale, and some fields may be far staler than others. For
     *  long-running pools however even approximate data may provide useful insights. Your mileage may vary however, you have been warned ;-)
     */
    AWorkerThreadStatistics getStatistics() {
        return new AWorkerThreadStatistics (getState (), getId (),
                stat_numTasksExecuted, stat_numSharedTasksExecuted, stat_numSteals, stat_numExceptions, stat_numParks, stat_numFalseAlarmUnparks, stat_numSharedQueueSwitches, stat_numLocalSubmits, localQueue.approximateSize ());
    }

    @Override public void run () {
        long tasksAtPark = -1;

//        topLevelLoop:
        while (true) {
            try {
                Runnable task;

                //TODO intermittently read from global localQueue(s) and FIFO end of local localQueue
                if ((task = tryGetWork ()) != null) {
                    if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numTasksExecuted += 1;
                    task.run ();
                }
                else {
                    // spin a little before parking - this currently does not provide measurable speedup
//                    for (int i=0; i<0; i++) {
//                        if ((task = tryGetForeignWork ()) != null) {
//                            if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numTasksExecuted += 1;
//                            task.run ();
//                            continue topLevelLoop;
//                        }
//                    }

                    pool.markWorkerAsIdle (idleThreadMask);

                    // re-check availability of work after marking the thread as idle --> avoid races
                    if ((task = tryGetForeignWork ()) != null) {
                        if (! pool.markWorkerAsBusy (idleThreadMask)) {
                            // thread was 'woken up' because of available work --> cause some other thread to be notified instead
                            pool.unmarkScanning (); //TODO merge with 'markWorkerAsBusy'
                            pool.onAvailableTask ();
                        }
                        if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numTasksExecuted += 1;
                        task.run ();
                        continue;
                    }

                    if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numParks += 1;
                    if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) {
                        if (tasksAtPark == stat_numTasksExecuted) {
                            stat_numFalseAlarmUnparks += 1;
                        }
                        else {
                            tasksAtPark = stat_numTasksExecuted;
                        }
                    }

                    UNSAFE.park (false, 0L);

                    // This flag is usually set before the call unpark(), but some races cause a thread to be unparked redundantly, causing the flag to be out of sync.
                    // Setting the flag before unpark() is piggybacked on another CAS operation and therefore basically for free, so we leave it there, but we need it
                    //  here as well.
                    pool.markWorkerAsBusy (idleThreadMask);

                    if ((task = tryGetForeignWork ()) != null) {
                        pool.unmarkScanning();
                        pool.wakeUpWorker ();
                        if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numTasksExecuted += 1;
                        task.run ();
                    }
                    else {
                        pool.unmarkScanning();
                    }
                }
            }
            catch (PoolShutdown e) {
                e.shutdownFuture.completeAsSuccess (null);
                return;
            }
            catch (Throwable th) {
                if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numExceptions += 1;
                //TODO special handling for 'important' errors and exceptions?
                try {
                    exceptionHandler.apply (th);
                }
                catch (Throwable th2) {
                    System.err.println ("exception handler terminated with a throwable");
                    th2.printStackTrace ();
                }
            }
        }
    }

    private Runnable tryGetWork() {
        Runnable task;

        //TODO intermittently read from global SharedQueue(s) and FIFO end of local localQueue
        if ((task = localQueue.popLifo ()) != null) {
            return task;
        }
        else if ((task = tryGetSharedWork ()) != null) {
            return task;
        }
        else if ((task = tryStealWork ()) != null) {
            return task;
        }
        return null;
    }

    private Runnable tryGetForeignWork () {
        Runnable task;

        if ((task = tryGetSharedWork ()) != null) {
            return task;
        }
        else if ((task = tryStealWork ()) != null) {
            return task;
        }
        return null;
    }

    private Runnable tryGetSharedWork() {
        Runnable task;

        //TODO go forward once in a while to avoid starvation

        final int prevQueue = currentSharedQueue;

        //noinspection ForLoopReplaceableByForEach
        for (int i=0; i < sharedQueues.length; i++) {
            if ((task = sharedQueues[currentSharedQueue].popFifo (localQueue)) != null) { //TODO adjust statistics to reflect prefetched tasks
                if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numSharedTasksExecuted += 1;
                //noinspection PointlessBooleanExpression,ConstantConditions
                if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS && prevQueue != currentSharedQueue) stat_numSharedQueueSwitches += 1;
                return task;
            }
            currentSharedQueue = (currentSharedQueue + queueTraversalIncrement) % sharedQueues.length; //TODO bit mask instead of division?
        }

        return null;
    }

    private Runnable tryStealWork () {
        Runnable task;
        for (LocalQueue otherQueue: allLocalQueues) {
            if (otherQueue == localQueue) {
                continue;
            }
            if ((task = otherQueue.popFifo ()) != null) {
                if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numSteals += 1;

                //TODO refine prefetching based on other queue's size
                //TODO other LocalQueue implementations
                for (int i=0; i<numPrefetchLocal; i++) {
                    final Runnable prefetched = otherQueue.popFifo ();
                    if (prefetched == null) break;
                    localQueue.push (prefetched);
                    if (AThreadPoolImpl.SHOULD_GATHER_STATISTICS) stat_numSteals += 1; //TODO count separately?
                }
                return task;
            }
        }

        return null;
    }

    //-------------------- Unsafe stuff
    private static final Unsafe UNSAFE;

    static {
        try {
            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            UNSAFE = (Unsafe) f.get (null);
        }
        catch (Exception exc) {
            AUnchecker.throwUnchecked (exc);
            throw new RuntimeException(); // for the compiler
        }
    }
}
