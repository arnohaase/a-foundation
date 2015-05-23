package com.ajjpj.afoundation.concurrent.pool.a;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author arno
 */
public interface ASchedulingStrategy {
    ALocalSubmissionQueue createLocalQueue ();
    AGlobalSubmissionQueue createGlobalQueue ();

    /**
     * This method does the actual scheduling. Implementations must block until a new task is available. <p>
     *
     * If a scheduling algorithm involves mutable data (e.g. counters, timestamps etc.), that data should be stored in the local and
     *  global queue implementations to avoid spreading mutable data across different objects.
     */
    Runnable takeNextTask (ALocalSubmissionQueue ownQueue, ALocalSubmissionQueue[] localQueues, AGlobalSubmissionQueue globalQueue) throws InterruptedException;


    ASchedulingStrategy OWN_FIRST_NO_STEALING = new ASchedulingStrategy () {
        @Override public ALocalSubmissionQueue createLocalQueue () {
            return new LocalQueue ();
        }
        @Override public AGlobalSubmissionQueue createGlobalQueue () {
            return new GlobalQueue ();
        }
        @Override public Runnable takeNextTask (ALocalSubmissionQueue ownQueue, ALocalSubmissionQueue[] localQueues, AGlobalSubmissionQueue globalQueue) throws InterruptedException {
            final LocalQueue lq = (LocalQueue) ownQueue;
            final Runnable local = lq.queue.pollFirst ();
            if (local != null) {
                return local;
            }
            return ((GlobalQueue) globalQueue).queue.take ();
        }

        class LocalQueue implements ALocalSubmissionQueue {
            final ArrayDeque<Runnable> queue = new ArrayDeque<> ();

            @Override public void submit (Runnable task) {
                queue.push (task);
            }
            @Override public void submitShutdown () {
                queue.clear ();
                queue.push (SHUTDOWN);
            }
        }

        class GlobalQueue implements AGlobalSubmissionQueue {
            final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<> ();

            @Override public void submit (Runnable task) {
                queue.offer (task);
            }
        }
    };

    static ASchedulingStrategy SingleQueue() {
        return new ASchedulingStrategy () {
            final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<> ();
            final AtomicBoolean isClearedForShutdown = new AtomicBoolean (false);

            @Override public ALocalSubmissionQueue createLocalQueue () {
                return new ALocalSubmissionQueue () {
                    @Override public void submit (Runnable task) {
                        queue.offer (task);
                    }
                    @Override public void submitShutdown () {
                        if (isClearedForShutdown.compareAndSet (false, true)) {
                            queue.clear ();
                        }
                        queue.offer (ALocalSubmissionQueue.SHUTDOWN);
                    }
                };
            }

            @Override public AGlobalSubmissionQueue createGlobalQueue () {
                return task -> queue.offer (task);
            }

            @Override public Runnable takeNextTask (ALocalSubmissionQueue ownQueue, ALocalSubmissionQueue[]localQueues, AGlobalSubmissionQueue globalQueue)throws
            InterruptedException {
                return queue.take ();
            }
        };
    }
}
