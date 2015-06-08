package com.ajjpj.afoundation.concurrent.pool.a;

/**
 * @author arno
 */
class AThread extends Thread {
    final ASchedulingStrategy schedulingStrategy;

    final APoolImpl pool;
    final ALocalSubmissionQueue queue;

    AThread (APoolImpl pool, ASchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
        this.pool = pool;
        this.queue = schedulingStrategy.createLocalQueue();
    }

    @Override public void run () {
        while (true) {
            try {
                final Runnable task = schedulingStrategy.takeNextTask (queue, pool.localQueues, pool.globalQueue);
                if (task == ALocalSubmissionQueue.SHUTDOWN) {
//                    System.out.println ("shutdown");
                    break;
                }
                task.run ();
            }
            catch (InterruptedException exc) { //TODO check 'shutdown' flag here?
                // ignore
            }
            catch (Throwable e) {
                e.printStackTrace (); //TODO
            }
        }
        pool.onThreadFinished (this);
    }
}
