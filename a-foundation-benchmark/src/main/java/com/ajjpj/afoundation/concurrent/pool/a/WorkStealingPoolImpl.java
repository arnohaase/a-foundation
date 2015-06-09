package com.ajjpj.afoundation.concurrent.pool.a;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.concurrent.pool.AFuture;
import com.ajjpj.afoundation.concurrent.pool.APool;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;


/**
 * @author arno
 */
public class WorkStealingPoolImpl implements APool {
    final WorkStealingThread[] threads;
    final WorkStealingLocalQueue[] localQueues;
    final WorkStealingGlobalQueue globalQueue;

    final AtomicReference<AList<WorkStealingThread>> waitingWorkers = new AtomicReference<> (AList.nil()); // threads will add themselves in 'run' loop when they don't find work

    private final CountDownLatch shutdownLatch;

    public WorkStealingPoolImpl (int numThreads) {
        this.globalQueue = new WorkStealingGlobalQueue ();
        this.shutdownLatch = new CountDownLatch (numThreads);

        this.localQueues = new WorkStealingLocalQueue[numThreads];
        this.threads     = new WorkStealingThread [numThreads];
        for (int i=0; i<numThreads; i++) {
            threads[i] = new WorkStealingThread (this);
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


    @Override public <T> AFuture<T> submit (Callable<T> code) {
        //TODO deal with submissions before the pool is started - reject them with an exception? --> do this in a thread-safe, non-racy way!

        final ATask<T> result = new ATask<> ();
        final ASubmittable submittable = new ASubmittable (result, code);

        final WorkStealingThread availableWorker = availableWorker ();
        if (availableWorker != null) {
            availableWorker.wakeUpWith (submittable);
        }
        else {
            final Thread curThread = Thread.currentThread ();
            if (curThread instanceof WorkStealingThread && ((WorkStealingThread) curThread).pool == this) {
                ((WorkStealingThread) curThread).queue.submit (submittable);
            }
            else {
                globalQueue.externalPush (submittable);
            }
        }

        return result;
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
        while (!waitingWorkers.compareAndSet (before, before.tail ()));

        return worker;
    }

    void onThreadFinished (AThread thread) {
        shutdownLatch.countDown ();
    }

    @Override public void shutdown () throws InterruptedException {
        //TODO clear global queue, prevent reentrance, ...

        System.out.println ("shutdown");

        //TODO implement this
        System.exit (1);
        shutdownLatch.await ();
    }

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
