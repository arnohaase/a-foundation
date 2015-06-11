package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.function.AFunction0;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/*

  TODO 'statistics' API: wakeUps, global, local; numAvailableWorkers; backlog size
  TODO with / without timeout (combinable)
  TODO cancel
  TODO interrupt
  TODO exception handling
  TODO fixed size queues
  TODO Builder
  TODO adapter --> as ExecutorService, from ExecutorService
  TODO adapter --> as ExecutionContext
  TODO shutdown: finsish processing submitted tasks
  TODO shutdownNow (with / without interrupting)
  TODO awaitTermination

  TODO AScheduler

 */


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
public class AWorkStealingPoolImpl implements APromisingExecutor {
    final WorkStealingThread[] threads;
    final WorkStealingLocalQueue[] localQueues;
    final WorkStealingGlobalQueue globalQueue;

    final AtomicReference<AList<WorkStealingThread>> waitingWorkers = new AtomicReference<> (AList.<WorkStealingThread>nil()); // threads will add themselves in 'run' loop when they don't find work

    private final CountDownLatch shutdownLatch;

    static final Runnable SHUTDOWN = new Runnable () {
        @Override public void run () {
        }
    };

    public AWorkStealingPoolImpl (int numThreads, int globalBeforeLocalInterval, int numPollsBeforePark, int pollNanosBeforePark) {
        this.globalQueue = new WorkStealingGlobalQueue ();
        this.shutdownLatch = new CountDownLatch (numThreads);

        this.localQueues = new WorkStealingLocalQueue[numThreads];
        this.threads     = new WorkStealingThread [numThreads];
        for (int i=0; i<numThreads; i++) {
            threads[i] = new WorkStealingThread (this, i, globalBeforeLocalInterval, numPollsBeforePark, pollNanosBeforePark);
            localQueues[i] = threads[i].queue;
        }
    }

    /**
     * This method actually starts the threads. It is separate from the constructor to ensure safe publication of final state.
     */
    public AWorkStealingPoolImpl start () {
        for (Thread thread: threads) {
            thread.start ();
        }

        return this;
    }

    @Override public <T> APromise<T> submit (AFunction0<T, ? extends Exception> function) {
        return AExecutors.calcAsync (this, function);
    }

    @Override public void submit (Runnable task) {
        try {
            final WorkStealingThread availableWorker = availableWorker ();
            if (availableWorker != null) {
                if (shouldCollectStatistics) numWakeups.incrementAndGet ();
                availableWorker.wakeUpWith (task);
            }
            else {
                final Thread curThread = Thread.currentThread ();
                if (curThread instanceof WorkStealingThread && ((WorkStealingThread) curThread).pool == this) {
                    if (shouldCollectStatistics) numLocalPush.incrementAndGet ();
                    ((WorkStealingThread) curThread).queue.submit (task);
                }
                else {
                    if (shouldCollectStatistics) numGlobalPush.incrementAndGet ();
                    globalQueue.externalPush (task);
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

    @Override
    public void shutdown (boolean cancelBacklog, boolean interruptRunningTasks) throws InterruptedException {
        //TODO handle shutdown without cancelling back log
        //TODO interrupt running tasks

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
}
