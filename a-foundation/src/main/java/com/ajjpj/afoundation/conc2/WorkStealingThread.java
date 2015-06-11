package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.function.APredicateNoThrow;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.locks.LockSupport;


/**
 * @author arno
 */
class WorkStealingThread extends Thread {
    final WorkStealingLocalQueue queue = new WorkStealingLocalQueue (true);
    final AWorkStealingPoolImpl pool;

    private final int ownThreadIndex;

    private final int globalBeforeLocalInterval;
    private final int numPollsBeforePark;
    private final int pollNanosBeforePark;

    //TODO configuration parameter for 'no work stealing'

    @SuppressWarnings ("unused") // is written with Unsafe.putOrderedObject
    private volatile Runnable wakeUpTask;

    //TODO optimization: is a lazySet sufficient for in-thread access as long as other threads use a volatile read? Is there a 'lazy CAS'?

    public WorkStealingThread (AWorkStealingPoolImpl pool, int ownThreadIndex, int globalBeforeLocalInterval, int numPollsBeforePark, int pollNanosBeforePark) {
        this.pool = pool;
        this.ownThreadIndex = ownThreadIndex;

        this.globalBeforeLocalInterval = globalBeforeLocalInterval;
        this.numPollsBeforePark = numPollsBeforePark;
        this.pollNanosBeforePark = pollNanosBeforePark;
    }


    //TODO scheduling with code affinity (rather than data affinity)


    @Override public void run () {
        int msgCount = 0;

        while (true) {
            msgCount += 1;

            try {
                final boolean pollGlobalQueueFirst = msgCount % globalBeforeLocalInterval == 0;
                if (pollGlobalQueueFirst) {
                    // This is the exceptional case: Polling the global queue first once in a while avoids starvation of
                    //  work from the global queue. This is important in systems where locally produced work can saturate
                    //  the pool, e.g. in actor-based systems.
                    if (exec (tryGlobalFetch()) || exec (tryLocalFetch ())) continue;
                }
                else {
                    // This is the normal case: check for local work first, and only if there is no local work look in
                    //  the global queue
                    if (exec (tryLocalFetch()) || exec (tryGlobalFetch ())) continue;
                }

                if (exec (tryActiveWorkStealing ())) continue;

                waitForWork ();
            }
            catch (WorkStealingShutdownException exc) {
                // this exception signals that the thread pool was shut down

                //noinspection finally
                try {
                    pool.onThreadFinished (this);
                }
                catch (Throwable exc2) {
                    exc2.printStackTrace ();
                }
                finally {
                    //noinspection ReturnInsideFinallyBlock
                    return;
                }
            }
            catch (Exception exc) {
                exc.printStackTrace (); //TODO exception handling, InterruptedException in particular
            }
        }
    }

    private Runnable tryGlobalFetch () {
        return pool.globalQueue.poll ();
    }

    private Runnable tryLocalFetch () {
        return queue.nextLocalTask ();
    }

    private boolean exec (Runnable optTask) {
        if (optTask != null) {
            optTask.run ();
            return true;
        }
        return false;
    }

    private Runnable tryActiveWorkStealing () {
        for (int i=1; i<pool.localQueues.length; i++) { //TODO store this length in an attribute of this class?
            final int victimThreadIndex = (ownThreadIndex + i) % pool.localQueues.length;

            final Runnable stolenTask = pool.localQueues[victimThreadIndex].poll ();
            if (stolenTask != null) {
                return stolenTask;
            }
        }
        return null;
    }

    private void waitForWork () {
        Runnable newTask;

        // There is currently no work available for this thread. That means that there is currently not enough work for all
        //  worker threads, i.e. the pool is in a 'low load' situation.
        //
        // Now we want to park this thread until work becomes available. There are basically two ways of doing that, and they
        //  have different trade-offs: Pushing work from the producing thread (i.e. calling 'unpark' from the producing thread
        //  when work becomes available), or polling from this thread (i.e. waiting some time and checking for work, without
        //  involving any producing threads in this thread's scheduling).
        //
        // Unparking a thread incurs a significant overhead *for the caller of 'unpark'*. In the 'push' approach, a producer
        //  thread is burdened with this overhead, which can severely limit throughput for high-frequency producers. Polling
        //  on the other hand causes each idling thread to place an ongoing load on the system.
        //
        // The following code compromises, starting out by polling and then parking itself, waiting to be awakened by a
        //  'push' operation when work becomes available.

        for (int i=0; i<numPollsBeforePark; i++) {
            // wait a little while and look again before really going to sleep
            LockSupport.parkNanos (pollNanosBeforePark);
            if (exec (tryGlobalFetch ()) || exec (tryActiveWorkStealing ())) return;
        }

        preparePark ();

        // re-check for available work in queues to avoid a race condition.

        //TODO this or some other strategy (e.g. intermittently waking up from 'park')?
        newTask = tryGlobalFetch ();
//        if (newTask == null) newTask = tryActiveWorkStealing ();   //TODO include this or not? Benchmarks indicate it slows things down, but why exactly?
        if (newTask != null) {
            // This is a pretty rare code path, therefore the implementation burdens it with checks and overhead wherever possible, so
            //  other, more frequent paths - such as pool.submit() - can be fast.
            AList<WorkStealingThread> before, after;

            do {
                before = pool.waitingWorkers.get ();
                after = before.filter (new APredicateNoThrow<WorkStealingThread> () {
                    @Override public boolean apply (WorkStealingThread o) {
                        return o != WorkStealingThread.this;
                    }
                });
            }
            while (! pool.waitingWorkers.compareAndSet (before, after));

            if (after.size () == before.size ()) {
                // The pool is "waking up" this thread concurrently. We actively spin until the 'wake-up task' is set. That is a
                //  conscious trade-off to keep pool.submit() fast - this race condition is pretty rare, so the trade-off pays in
                //  practice.

                Runnable wakeUp;
                //noinspection StatementWithEmptyBody
                while ((wakeUp = wakeUpTask) == null) {
                    // wait actively
                }

                // re-inject the wake-up task into the pool
                pool.submit (wakeUp);
            }

            newTask.run();
            return;
        }

        do {
            queue.checkShutdown ();
            LockSupport.park (); //TODO exception handling
            newTask = wakeUpTask;

            // 'stealing' locally submitted work this way is effectively delayed by the 'parkNanos' call above

            if (newTask == null || newTask == AWorkStealingPoolImpl.SHUTDOWN) {
                // for other cases, shutdown is checked after the task is run anyway
                queue.checkShutdown ();
            }
        }
        while (newTask == null);
        U.putOrderedObject (this, WAKE_UP_TASK, null); //TODO replace with U.compareAndSwap? --> does that have volatile read semantics? Is that even faster?

        newTask.run ();
    }

    void wakeUpWith (Runnable task) {
        U.putOrderedObject (this, WAKE_UP_TASK, task); // is read with volatile semantics after wake-up
        LockSupport.unpark (this);
    }

    private void preparePark() {
        // removal from the stack of 'waiting workers' happens in the pool
        AList<WorkStealingThread> before;
        do {
            before = pool.waitingWorkers.get ();
        }
        while (! pool.waitingWorkers.compareAndSet (before, before.cons (this)));
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long WAKE_UP_TASK;
    static {
        try {
            final Field f = sun.misc.Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            U = (Unsafe) f.get (null);

            Class<?> k = WorkStealingThread.class;
            WAKE_UP_TASK = U.objectFieldOffset (k.getDeclaredField("wakeUpTask"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}