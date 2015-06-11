package com.ajjpj.afoundation.conc2;

//import sun.misc.Contended;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.RejectedExecutionException;


/**
 * @author arno
 */
//@Contended
class WorkStealingLocalQueue {
    public void submit (WorkStealingPoolImpl.ASubmittable task) {
        if (task == null) {
            throw new NullPointerException ();
        }
        this.push (task);
    }

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

    /**
     * Mask for the flag to signify shutdown of the entire pool. This flag
     *  is placed in the 'base' field because that field is read with 'volatile'
     *  semantics on every access, so checking for shutdown incurs minimal
     *  overhead.
     */
    static final int FLAG_SHUTDOWN = 1 << 31;

    final boolean lifo; // mode;          // 0: lifo, > 0: fifo, < 0: shared
    volatile int base;         // index of next slot for poll
    int top;                   // index of next slot for push
    WorkStealingPoolImpl.ASubmittable[] array;          // the elements

    WorkStealingLocalQueue (boolean lifo) {
        this.lifo = lifo;
        // Place indices in the center of array
        base = top = INITIAL_QUEUE_CAPACITY >>> 1;
        array = new WorkStealingPoolImpl.ASubmittable[INITIAL_QUEUE_CAPACITY];
    }

    private int getBase() {
        final int raw = base;
        if ((raw & FLAG_SHUTDOWN) != 0) {
            throw new WorkStealingShutdownException ();
        }
        return raw & (~ FLAG_SHUTDOWN);
    }

    void checkShutdown () {
        getBase ();
    }

    void shutdown() {
        int before;

        do {
            before = base;
        }
        while (! U.compareAndSwapInt (this, QBASE, before, before | FLAG_SHUTDOWN));
    }

    /**
     * Pushes a task. Call only by owner. (The shared-queue version is embedded in method externalPush.)
     *
     * @param task the task. Caller must ensure non-null.
     * @throws java.util.concurrent.RejectedExecutionException if array cannot be resized
     */
    final void push (WorkStealingPoolImpl.ASubmittable task) {
        final int s = top;
        final WorkStealingPoolImpl.ASubmittable[] a = array;
        int m = a.length - 1;

        // get the value of 'base' early to detect shutdown
        final int base = getBase ();

        U.putOrderedObject(a, ((m & s) << ASHIFT) + ABASE, task);
        top = s+1;
        final int n = top - base;
        if (n >= m) {
            growArray ();
        }
    }


    /**
     * Initializes or doubles the capacity of array. Call either
     * by owner or with lock held -- it is OK for base, but not
     * top, to move while resizings are in progress.
     */
    private WorkStealingPoolImpl.ASubmittable[] growArray() {
        final WorkStealingPoolImpl.ASubmittable[] oldA = array;
        final int size = oldA != null ? oldA.length << 1 : INITIAL_QUEUE_CAPACITY;
        if (size > MAXIMUM_QUEUE_CAPACITY)
            throw new RejectedExecutionException ("Queue capacity exceeded");
        array = new WorkStealingPoolImpl.ASubmittable[size];
        final WorkStealingPoolImpl.ASubmittable[] a = array;

        final int oldMask = oldA.length - 1;
        final int t = top;
        int b = getBase ();
        if (oldMask >= 0 && t-b > 0) {
            final int mask = size - 1;
            do {
                int oldj = ((b & oldMask) << ASHIFT) + ABASE;
                int j = ((b & mask) << ASHIFT) + ABASE;
                final WorkStealingPoolImpl.ASubmittable x = (WorkStealingPoolImpl.ASubmittable) U.getObjectVolatile (oldA, oldj);
                if (x != null && U.compareAndSwapObject (oldA, oldj, x, null)) {
                    U.putObjectVolatile (a, j, x);
                }
                b += 1;
            }
            while (b != t);
        }
        return a;
    }

    /**
     * Takes next task, if one exists, in LIFO order.  Call only
     * by owner in unshared queues.
     */
    final WorkStealingPoolImpl.ASubmittable pop() {
        final WorkStealingPoolImpl.ASubmittable[] a = array;

        final int m = a.length-1;
        if (m >= 0) {
//            while (true) {
//                final int s = top-1;
//                if (s - getBase () < 0) {
//                    break;
//                }
//
//                final long j = ((m & s) << ASHIFT) + ABASE;
//                final ASubmittable t = (ASubmittable) U.getObject(a, j);
//                if (t == null) {
//                    break;
//                }
//                if (U.compareAndSwapObject(a, j, t, null)) {
//                    top = s;
//                    return t;
//                }
//            }

            int s;
            while ((s = top - 1) - getBase () >= 0) { //TODO how to simplify this?
                long j = ((m & s) << ASHIFT) + ABASE;
                final WorkStealingPoolImpl.ASubmittable t = (WorkStealingPoolImpl.ASubmittable) U.getObject(a, j);
                if (t == null) {
                    break;
                }
                if (U.compareAndSwapObject(a, j, t, null)) {
                    top = s;
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Takes next task, if one exists, in FIFO order.
     */
    final WorkStealingPoolImpl.ASubmittable poll() {
        WorkStealingPoolImpl.ASubmittable[] a;
        int b;

        while ((b = getBase ()) - top < 0 && (a = array) != null) {
            final int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
            final WorkStealingPoolImpl.ASubmittable t = (WorkStealingPoolImpl.ASubmittable) U.getObjectVolatile(a, j);
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

    /**
     * Takes next task, if one exists, in order specified by mode.
     */
    final WorkStealingPoolImpl.ASubmittable nextLocalTask() {
        return lifo ? pop() : poll();
    }


    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long QBASE;
    private static final int ABASE;
    private static final int ASHIFT;
    static {
        try {
            final Field f = sun.misc.Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            U = (Unsafe) f.get (null);

            Class<?> k = WorkStealingLocalQueue.class;
            Class<?> ak = WorkStealingPoolImpl.ASubmittable[].class;
            QBASE = U.objectFieldOffset (k.getDeclaredField("base"));
            ABASE = U.arrayBaseOffset (ak);
            int scale = U.arrayIndexScale (ak);
            if ((scale & (scale - 1)) != 0) {
                throw new Error ("data type scale not a power of two");
            }
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
