package com.ajjpj.afoundation.conc2;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RejectedExecutionException;


/**
 * @author arno
 */
//TODO @Contended
class WorkStealingGlobalQueue {

    /**
     * Capacity of work-stealing queue array upon initialization.
     * Must be a power of two; at least 4, but should be larger to
     * reduce or eliminate cacheline sharing among queues.
     * Currently, it is much larger, as a partial workaround for
     * the fact that JVMs often place arrays in locations that
     * share GC bookkeeping (especially cardmarks) such that
     * per-write accesses encounter serious memory contention.
     */
    static final int INITIAL_QUEUE_CAPACITY = 1 << 13;

    /**
     * Maximum size for queue arrays. Must be a power of two less
     * than or equal to 1 << (31 - width of array entry) to ensure
     * lack of wraparound of index calculations, but defined to a
     * value a bit less than this to help users trap runaway
     * programs before saturating systems.
     */
    static final int MAXIMUM_QUEUE_CAPACITY = 1 << 26; // 64M

    volatile int qlock;        // 1: locked, -1: terminate; else 0
    volatile int base;         // index of next slot for poll
    int top;                   // index of next slot for push
    Runnable[] array;          // the elements (initially unallocated)

    WorkStealingGlobalQueue () {
        // Place indices in the center of array (that is not yet allocated)
        base = top = INITIAL_QUEUE_CAPACITY >>> 1;
        array = new Runnable[INITIAL_QUEUE_CAPACITY];
    }

    private int getBase() {
        final int raw = base;
        if ((raw & WorkStealingLocalQueue.FLAG_SHUTDOWN) != 0) {
            throw new WorkStealingShutdownException ();
        }
        return raw & (~ WorkStealingLocalQueue.FLAG_SHUTDOWN);
    }

    void shutdown() {
        int before;

        do {
            before = base;
        }
        while (! U.compareAndSwapInt (this, QBASE, before, before | WorkStealingLocalQueue.FLAG_SHUTDOWN));
    }

    /**
     * Unless shutting down, adds the given task to a submission queue
     * at submitter's current queue index (modulo submission
     * range). Only the most common path is directly handled in this
     * method. All others are relayed to fullExternalPush.
     *
     * @param task the task. Caller must ensure non-null.
     */
    final void externalPush(Runnable task) {
        if (U.compareAndSwapInt (this, QLOCK, 0, 1)) { // lock
            final Runnable[] a = array;
            final int am = a.length - 1;
            final int s = top;
            final int n = s - getBase (); //TODO unlock in finally block? --> shutdown exception?
            if (am > n) {
                int j = ((am & s) << ASHIFT) + ABASE;
                U.putOrderedObject(a, j, task);
                top = s + 1;                     // push on to deque
                qlock = 0;

                return;
            }
            qlock = 0;
        }
        fullExternalPush(task);
    }

    /**
     * Full version of externalPush. This method is called, among
     * other times, upon the first submission of the first task to the
     * pool, so must perform secondary initialization.  It also
     * detects first submission by an external thread by looking up
     * its ThreadLocal, and creates a new shared queue if the one at
     * index if empty or contended. The plock lock body must be
     * exception-free (so no try/finally) so we optimistically
     * allocate new queues outside the lock and throw them away if
     * (very rarely) not needed.
     *
     * Secondary initialization occurs when plock is zero, to create
     * workQueue array and set plock to a valid value.  This lock body
     * must also be exception-free. Because the plock seq value can
     * eventually wrap around zero, this method harmlessly fails to
     * reinitialize if workQueues exists, while still advancing plock.
     */
    private void fullExternalPush (Runnable task) {
        for (;;) { //TODO refactor into CAS loop?
            if (U.compareAndSwapInt(this, QLOCK, 0, 1)) {
                Runnable[] a = array;
                int s = top;
                boolean submitted = false;
                try {
                    if ((a.length > s + 1 - getBase ()) ||
                            (a = growArray()) != null) {   // must presize
                        int j = (((a.length - 1) & s) << ASHIFT) + ABASE;
                        U.putOrderedObject(a, j, task);
                        top = s + 1;
                        submitted = true;
                    }
                }
                finally {
                    qlock = 0;  // unlock
                }
                if (submitted) {
                    return;
                }
            }
        }
    }



    /**
     * Initializes or doubles the capacity of array. Call only with lock held -- it is OK for base, but not
     * top, to move while resizings are in progress.
     */
    final Runnable[] growArray() { //TODO is the new value of 'array' even safely published? --> code is from FJP, but still...
        final Runnable[] oldA = array;
        final int size = oldA != null ? oldA.length << 1 : INITIAL_QUEUE_CAPACITY;
        if (size > MAXIMUM_QUEUE_CAPACITY)
            throw new RejectedExecutionException ("Queue capacity exceeded");
        array = new Runnable[size];
        final Runnable[] a = array;

        if (oldA != null) {
            final int oldMask = oldA.length - 1;
            final int t = top;
            int b = getBase ();
            if (oldMask >= 0 && t-b > 0) {
                final int mask = size - 1;
                do {
                    int oldj = ((b & oldMask) << ASHIFT) + ABASE;
                    int j = ((b & mask) << ASHIFT) + ABASE;
                    final Runnable x = (Runnable) U.getObjectVolatile (oldA, oldj);
                    if (x != null && U.compareAndSwapObject (oldA, oldj, x, null)) {
                        U.putObjectVolatile (a, j, x);
                    }
                    b += 1;
                }
                while (b != t);
            }
        }
        return a;
    }


    /**
     * Takes next task, if one exists, in FIFO order.
     */
    final Runnable poll() {
        Runnable[] a;
        int b;

        while ((b = getBase ()) - top < 0 && (a = array) != null) {
            final int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
            final Runnable t = (Runnable) U.getObjectVolatile(a, j);
            if (t != null) {
                if (U.compareAndSwapObject(a, j, t, null)) {
                    U.putOrderedInt(this, QBASE, b + 1);
                    return t;
                }
            }
            else if (getBase () == b) {
                if (b + 1 == top)
                    break;
                Thread.yield(); // wait for lagging update (very rare)
            }
        }
        return null;
    }

    // Unsafe mechanics
    private static final Unsafe U;
    private static final long QBASE;
    private static final long QLOCK;
    private static final int ABASE;
    private static final int ASHIFT;
    static {
        try {
            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            U = (Unsafe) f.get (null);


            Class<?> k = WorkStealingGlobalQueue.class;
            Class<?> ak = ForkJoinTask[].class;
            QBASE = U.objectFieldOffset
                    (k.getDeclaredField("base"));
            QLOCK = U.objectFieldOffset
                    (k.getDeclaredField("qlock"));
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
