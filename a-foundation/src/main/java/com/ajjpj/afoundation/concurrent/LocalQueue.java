package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.util.AUnchecker;
import sun.misc.Unsafe;

import java.lang.reflect.Field;


/**
 * @author arno
 */
class LocalQueue {
    /**
     * an array holding all currently submitted tasks.
     */
    final Runnable[] tasks;

    /**
     * The thread is really final and not-null. Because of circular references during initialization, it is technically not final and initialized not in the constructor but in
     *  a separate call to method {@code init()}.
     */
    WorkerThread thread;

    //TODO here and elsewhere: memory layout
    /**
     * a bit mask to project an offset into the valid range of offsets for the tasks array
     */
    private final int mask;

    private final AThreadPoolImpl pool;

    @SuppressWarnings ("unused")
    long base = 0;
    long top = 0;

    LocalQueue (AThreadPoolImpl pool, int size) {
        this.pool = pool;

        if (1 != Integer.bitCount (size)) throw new IllegalArgumentException ("size must be a power of 2");
        if (size < 8 || size > 1024*1024) throw new IllegalArgumentException ("size must be in the range from 8 to " + (1024*1024));

        this.tasks = new Runnable[size];
        this.mask = size-1;
    }

    void init(WorkerThread thread) {
        this.thread = thread;
    }

    /**
     * @return an approximation of the queue's current size, useful only for statistics purposes.
     */
    int approximateSize() {
        return (int) (
                UNSAFE.getLongVolatile (this, OFFS_TOP) -
                UNSAFE.getLongVolatile (this, OFFS_BASE)
        );
    }

    /**
     * Add a new task to the top of the localQueue, incrementing 'top'. This is only ever called from the owning thread.
     */
    void push (Runnable task) {
        final long _base = UNSAFE.getLongVolatile (this, OFFS_BASE); // read base first (and only once)
        final long _top = top;
        if (_top == _base + mask) {
            throw new RejectedExecutionExceptionWithoutStacktrace ("local queue overflow");
        }

        tasks[asArrayindex (_top)] = task;
        // 'top' is only ever modified by the owning thread, so we need no CAS here. Storing 'top' with volatile semantics publishes the task and ensures that changes to the task
        //  can never overtake changes to 'top' wrt visibility.
        UNSAFE.putLongVolatile (this, OFFS_TOP, _top+1);

        // Notify pool only for the first added item per queue.
        if (_top - _base <= 1) {
            pool.onAvailableTask();
        }
    }

    /**
     * Fetch (and remove) a task from the top of the queue, i.e. LIFO semantics. This is only ever called from the owning thread, removing (or
     *  at least reducing) contention at the top of the queue: No other thread operates there.
     */
    Runnable popLifo () {
        final long _top = top;
        final Runnable result = tasks[asArrayindex (_top-1)];
        if (result == null) {
            // The queue is empty. It is possible for the queue to be empty even if the previous unprotected read does not return null, but
            //  it will only ever return null if the queue really is empty: New entries are only added by the owning thread, and this method
            //  'popLifo()' is also only ever called by the owning thread.
            return null;
        }

        if (! UNSAFE.compareAndSwapObject (tasks, taskOffset (_top-1), result, null)) {
            // The CAS operation failing means that another thread pulled the top-most item from the queue, so the queue is now definitely
            //  empty. It also null'ed out the task in the array if it was previously available, allowing to to be GC'ed when processing is
            //  finished.
            return null;
        }

        // Since 'result' is not null, and was not previously consumed by another thread, we can safely consume it --> decrement 'top'
        UNSAFE.putOrderedLong (this, OFFS_TOP, _top-1);
        return result;
    }

    /**
     * Fetch (and remove) a task from the bottom of the queue, i.e. FIFO semantics. This method can be called by any thread.
     */
    Runnable popFifo () {
        long _base, _top;

        while (true) {
            // reading 'base' with volatile semantics emits the necessary barriers to ensure visibility of 'top'
            _base = UNSAFE.getLongVolatile (this, OFFS_BASE);
            _top = top;

            if (_base == _top) {
                // Terminate the loop: the queue is empty.
                //TODO verify that Hotspot optimizes this kind of return-from-the-middle well
                return null;
            }

            // a regular read is OK here: 'push()' emits a store barrier after storing the task, 'popLifo()' modifies it with CAS, and 'popFifo()' does
            //  a volatile read of 'base' before reading the task
            final Runnable result = tasks[asArrayindex (_base)];

            // result == null means that another thread concurrently fetched the task from under our nose.
            // checking _base against a re-read 'base' with volatile semantics avoids wrap-around race - 'base' could have incremented by a multiple of the queue's size between
            //   our first reading it and fetching the task at that offset, which would cause the increment inside the following if block to significantly decrement it and
            //   wreak havoc.
            // CAS ensures that only one thread gets the task, and allows GC when processing is finished
            if (result != null && _base == UNSAFE.getLongVolatile(this, OFFS_BASE) && UNSAFE.compareAndSwapObject (tasks, taskOffset (_base), result, null)) {
                UNSAFE.putLongVolatile (this, OFFS_BASE, _base+1); //TODO is 'putOrdered' sufficient?
                return result;
            }
        }
    }

    private long taskOffset (long l) {
        return OFFS_TASKS + SCALE_TASKS * (l & mask);
    }

    int asArrayindex (long l) {
        return (int) (l & mask);
    }

    //------------- Unsafe stuff
    private static final Unsafe UNSAFE;

    private static final long OFFS_TASKS;
    private static final long SCALE_TASKS;

    private static final long OFFS_BASE;
    static final long OFFS_TOP;

    static {
        try {
            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            UNSAFE = (Unsafe) f.get (null);

            OFFS_TASKS = UNSAFE.arrayBaseOffset (Runnable[].class);
            SCALE_TASKS = UNSAFE.arrayIndexScale (Runnable[].class);

            OFFS_BASE = UNSAFE.objectFieldOffset (LocalQueue.class.getDeclaredField ("base"));
            OFFS_TOP = UNSAFE.objectFieldOffset (LocalQueue.class.getDeclaredField ("top"));
        }
        catch (Exception e) {
            AUnchecker.throwUnchecked (e);
            throw new RuntimeException(); // for the compiler
        }
    }
}
