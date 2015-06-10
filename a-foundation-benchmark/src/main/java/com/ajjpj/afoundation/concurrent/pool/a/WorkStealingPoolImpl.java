package com.ajjpj.afoundation.concurrent.pool.a;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.concurrent.pool.AFuture;
import com.ajjpj.afoundation.concurrent.pool.APool;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


/**
 *
 *
 * optional 'no work stealing' policy
 *
 * intermittent fetching of work from global queue even if there is work in local queues --> avoid starvation
 *
 * availableWorkers: 'stack' semantics is intentional - reuse most recently parked thread first
 *
 * @author arno
 */
public class WorkStealingPoolImpl implements APool {
    final WorkStealingThread[] threads;
    final WorkStealingLocalQueue[] localQueues;
    final WorkStealingGlobalQueue globalQueue;

    final AtomicReference<AList<WorkStealingThread>> waitingWorkers = new AtomicReference<> (AList.nil()); // threads will add themselves in 'run' loop when they don't find work

    private final CountDownLatch shutdownLatch;

    static final ASubmittable SHUTDOWN = new ASubmittable (null, null);

    public WorkStealingPoolImpl (int numThreads) {
        this.globalQueue = new WorkStealingGlobalQueue ();
        this.shutdownLatch = new CountDownLatch (numThreads);

        this.localQueues = new WorkStealingLocalQueue[numThreads];
        this.threads     = new WorkStealingThread [numThreads];
        for (int i=0; i<numThreads; i++) {
            threads[i] = new WorkStealingThread (this, i);
            localQueues[i] = threads[i].queue;
        }
    }

    /**
     * This method actually starts the threads. It is separate from the constructor to ensure safe publication of final state.
     */
    public WorkStealingPoolImpl start () {
        for (Thread thread: threads) {
            thread.start ();
        }

        return this;
    }


//    @Override public <T> AFuture<T> submit (Callable<T> code) {
//
//        final ATask<T> result = new ATask<> ();
//        final ASubmittable submittable = new ASubmittable (result, code);
//
//        final Thread curThread = Thread.currentThread ();
//        if (curThread instanceof WorkStealingThread && ((WorkStealingThread) curThread).pool == this) {
//            ((WorkStealingThread) curThread).queue.submit (submittable);
//        }
//        else {
//            final WorkStealingThread availableWorker = availableWorker ();
//            if (availableWorker != null) {
//                availableWorker.wakeUpWith (submittable);
//            }
//            else {
//                globalQueue.externalPush (submittable);
//            }
//        }
//
//        return result;
//    }

    @Override public <T> AFuture<T> submit (Callable<T> code) {
        final ATask<T> result = new ATask<> ();
        final ASubmittable submittable = new ASubmittable (result, code);

        doSubmit (submittable);

        return result;
    }

    void doSubmit (ASubmittable submittable) {
        try {
            final WorkStealingThread availableWorker = availableWorker ();
            if (availableWorker != null) {
                if (shouldCollectStatistics) numWakeups.incrementAndGet ();
                availableWorker.wakeUpWith (submittable);
            }
            else {
                final Thread curThread = Thread.currentThread ();
                if (curThread instanceof WorkStealingThread && ((WorkStealingThread) curThread).pool == this) {
                    if (shouldCollectStatistics) numLocalPush.incrementAndGet ();
                    ((WorkStealingThread) curThread).queue.submit (submittable);
                }
                else {
                    if (shouldCollectStatistics) numGlobalPush.incrementAndGet ();
                    globalQueue.externalPush (submittable);
                }
            }
        }
        catch (WorkStealingShutdownException e) {
            throw new RejectedExecutionException ("pool is shut down");
        }
    }



    private WorkStealingThread availableWorker () {
        WorkStealingThread worker;
        AList<WorkStealingThread> before;

        do {
            before = waitingWorkers.get ();
            if (before.isEmpty ()) {
                return null;
            }
            worker = before.head ();

        }
        while (!waitingWorkers.compareAndSet (before, before.tail ())); //TODO use Unsafe?

        return worker;
    }

    void onThreadFinished (WorkStealingThread thread) {
        shutdownLatch.countDown ();
    }

    @Override public void shutdown () throws InterruptedException {
        globalQueue.shutdown ();
        for (WorkStealingLocalQueue q: localQueues) {
            q.shutdown ();
        }

        WorkStealingThread worker;
        while ((worker = availableWorker ()) != null) {
            worker.wakeUpWith (SHUTDOWN);
        }

        shutdownLatch.await ();
    }

    //----------------------------------- statistics

    static final boolean shouldCollectStatistics = true;

    final AtomicLong numWakeups = new AtomicLong ();
    final AtomicLong numGlobalPush = new AtomicLong ();
    final AtomicLong numLocalPush = new AtomicLong ();

    public long getNumWakeups() {
        return numWakeups.get ();
    }

    public long getNumGlobalPushs() {
        return numGlobalPush.get ();
    }

    public long getNumLocalPushs() {
        return numLocalPush.get ();
    }

    //----------------------------------- internal data structure for submitted task

    static class ASubmittable implements Runnable {
        private final ATask result;
        private final Callable code;

        public ASubmittable (ATask result, Callable code) {
            this.result = result;
            this.code = code;
        }

        @SuppressWarnings ("unchecked")
        @Override public void run () {
            try {
                result.set (code.call ());
            }
            catch (Throwable th) {
                result.setException (th);
            }
        }
    }
}
