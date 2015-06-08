package com.ajjpj.afoundation.concurrent.pool.a;

import com.ajjpj.afoundation.collection.immutable.AList;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;


/**
 * @author arno
 */
class WorkStealingThread extends Thread {
    final WorkStealingLocalQueue queue = new WorkStealingLocalQueue (true);
    final WorkStealingPoolImpl pool;

    //TODO optimization: is a lazySet sufficient for in-thread access as long as other threads use a volatile read? Is there a 'lazy CAS'?
    // method 'run' starts in 'working' mode for simplicity's sake, so 'isWorking==true' is set accordingly
    final AtomicBoolean isWorking = new AtomicBoolean (true);

    public WorkStealingThread (WorkStealingPoolImpl pool) {
        this.pool = pool;
    }

    @Override public void run () {
        try {
            int i = 0;
            while (true) { //TODO shutdown
                WorkStealingPoolImpl.ASubmittable task;

                i += 1;

    //            System.out.println ("* " + Thread.currentThread () + ": looking for work");

                if ((task = queue.nextLocalTask ()) != null) {
//                    if (i%100_000_000 == 0)
//                        System.out.println ("a: " + task);

                    //TODO sporadically take items from the global queue even if there is local work to avoid starvation
    //                System.out.println ("  " + Thread.currentThread () + ": found local work");
                    task.run ();
                }
                else if ((task = pool.globalQueue.poll ()) != null) {
//                    System.out.println ("b: " + task);
    //                System.out.println ("  " + Thread.currentThread () + ": found global work");
                    task.run ();
                }
                else {
    //                System.out.println ("  " + Thread.currentThread () + ": parking");
                    preparePark ();
                    do {
                        try {
                            LockSupport.park (); //TODO exception handling
                        }
                        catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }
                    while (! isWorking.compareAndSet (false, true)); // CAS avoids races
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
    }

    private void preparePark() {
        isWorking.compareAndSet (true, false); //TODO handle 'false' result --> internal error

        // removal from the stack of 'waiting workers' happens in the pool
        AList<WorkStealingThread> before;
        do {
            before = pool.waitingWorkers.get ();
        }
        while (! pool.waitingWorkers.compareAndSet (before, before.cons (this)));
    }
}
