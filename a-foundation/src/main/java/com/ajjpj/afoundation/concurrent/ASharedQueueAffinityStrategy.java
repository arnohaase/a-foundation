package com.ajjpj.afoundation.concurrent;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * An AThreadPool assigns a fixed shared queue to each producer thread (without impeding producer threads' GC in any way). This
 *  interface allows configuration of that assignment.
 */
public interface ASharedQueueAffinityStrategy {
    /**
     * This method is invoked whenever a new producer threads submits work for the first time, and must be thread safe. It must
     *  return a value in the range from 0 (inclusive) to the number of shared queues (exclusive), which is passed in as a paramter
     *  for convenience.
     */
    int getSharedQueueIndex (Thread producer, int numSharedQueues);


    /**
     * This method creates an instance of the default strategy, which is to assign shared queues to (new) producer threads on a round-robin basis.
     */
    static ASharedQueueAffinityStrategy createDefault() {
        return new ASharedQueueAffinityStrategy () {
            final AtomicInteger nextSharedQueue = new AtomicInteger (0);

            @Override public int getSharedQueueIndex (Thread producer, int numSharedQueues) {
                return nextSharedQueue.getAndIncrement () % numSharedQueues;
            }
        };
    }
}
