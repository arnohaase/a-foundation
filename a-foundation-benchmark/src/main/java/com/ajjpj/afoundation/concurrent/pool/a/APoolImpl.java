package com.ajjpj.afoundation.concurrent.pool.a;

import com.ajjpj.afoundation.conc2.AFuture;
import com.ajjpj.afoundation.concurrent.pool.APool;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


/**
 * @author arno
 */
public class APoolImpl implements APool {
    private final ASchedulingStrategy schedulingStrategy;

    final AThread[] threads;
    final ALocalSubmissionQueue[] localQueues;
    final AGlobalSubmissionQueue globalQueue;

    private final CountDownLatch shutdownLatch;

    public APoolImpl (int numThreads, ASchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;

        this.globalQueue = schedulingStrategy.createGlobalQueue();
        this.shutdownLatch = new CountDownLatch (numThreads);

        this.localQueues = new ALocalSubmissionQueue[numThreads];
        this.threads = new AThread[numThreads];
        for (int i=0; i<numThreads; i++) {
            threads[i] = new AThread (this, schedulingStrategy);
            localQueues[i] = threads[i].queue;
        }
    }

    /**
     * This method actually starts the threads. It is separate from the constructor to ensure safe publication of final state.
     */
    public APoolImpl start () {
        for (AThread thread: threads) {
            thread.start ();
        }
        return this;
    }

    @Override public <T> AFuture<T> submit (Callable<T> code) {
        final Thread curThread = Thread.currentThread ();
        if (curThread instanceof AThread && ((AThread) curThread).pool == this) {
            final ATask<T> result = new ATask<> ();
            ((AThread) curThread).queue.submit (new ASubmittable (result, code));
            return result;
        }
        else {
            final ATask<T> result = new ATask<> ();
            globalQueue.submit (new ASubmittable (result, code));
            return result;
        }
    }

    void onThreadFinished (AThread thread) {
        shutdownLatch.countDown ();
    }

    @Override public void shutdown () throws InterruptedException {
        //TODO clear global queue, prevent reentrance, ...

        for (AThread thread: threads) {
            globalQueue.submit (ALocalSubmissionQueue.SHUTDOWN);
            thread.queue.submitShutdown ();
        }
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
