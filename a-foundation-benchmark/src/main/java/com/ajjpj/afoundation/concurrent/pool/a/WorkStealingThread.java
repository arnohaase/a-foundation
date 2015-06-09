package com.ajjpj.afoundation.concurrent.pool.a;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.concurrent.pool.a.WorkStealingPoolImpl.ASubmittable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.locks.LockSupport;


/**
 * @author arno
 */
class WorkStealingThread extends Thread {
    final WorkStealingLocalQueue queue = new WorkStealingLocalQueue (true);
    final WorkStealingPoolImpl pool;

    private volatile ASubmittable wakeUpTask; // is written with Unsafe.putOrderedObject

    //TODO optimization: is a lazySet sufficient for in-thread access as long as other threads use a volatile read? Is there a 'lazy CAS'?
    // method 'run' starts in 'working' mode for simplicity's sake, so 'isWorking==true' is set accordingly
//    final AtomicBoolean isWorking = new AtomicBoolean (true);

    public WorkStealingThread (WorkStealingPoolImpl pool) {
        this.pool = pool;
    }

    @Override public void run () {

        while (true) { //TODO shutdown
            try {
                ASubmittable newTask;

                if ((newTask = queue.nextLocalTask ()) != null) {
                    //TODO intermittently read from global queue if available --> avoid starvation

                    newTask.run ();
                }
                else if ((newTask = pool.globalQueue.poll ()) != null) {
                    newTask.run ();
                }
                else {
                    //TODO work stealing

                    preparePark ();

                    do {
                        LockSupport.park (); //TODO exception handling
                        newTask = wakeUpTask;
                    }
                    while (newTask == null);
                    U.putOrderedObject (this, WAKE_UP_TASK, null); //TODO replace with U.compareAndSwap? --> does that have volatile read semantics? Is that even faster

                    newTask.run ();
                }
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }

    void wakeUpWith (ASubmittable task) {
//        wakeUpTask = task;

        //TODO does 'putOrderedObject' work on a volatile field?!
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
