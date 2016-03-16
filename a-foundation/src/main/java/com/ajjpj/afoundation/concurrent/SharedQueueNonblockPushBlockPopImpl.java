package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.util.AUnchecker;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
class SharedQueueNonblockPushBlockPopImpl implements ASharedQueue {
    private final int prefetchBatchSize;
    /**
     * an array holding all currently submitted tasks.
     */
    private final Runnable[] tasks;

    //TODO here and elsewhere: memory layout
    /**
     * a bit mask to project an offset into the valid range of offsets for the tasks array
     */
    private final int mask;

    final AThreadPoolImpl pool;

    long base = 0;
    long top = 0;

    /**
     * This variable serves as a spin lock. All access goes through UNSAFE.
     */
    @SuppressWarnings ("UnusedDeclaration")
    private int lock = 0;

    SharedQueueNonblockPushBlockPopImpl (int prefetchBatchSize, AThreadPoolImpl pool, int size) {
        if (prefetchBatchSize < 1) throw new IllegalArgumentException ("worker threads must (attempt to) fetch a minimum of 1 task");
        this.prefetchBatchSize = prefetchBatchSize;
        this.pool = pool;

        if (1 != Integer.bitCount (size)) throw new IllegalArgumentException ("size must be a power of 2");
        if (size < 8 || size > 1024*1024) throw new IllegalArgumentException ("size must be in the range from 8 to " + (1024*1024));

        this.tasks = new Runnable[size];
        this.mask = size-1;
    }

    final Map<Long, String> byTopValue = new ConcurrentHashMap<> ();

    /**
     * @return an approximation of the queue's current size. The value may be stale and is not synchronized in any way, and it is intended for debugging and statistics
     *  purposes only.
     */
    @Override public int approximateSize () {
        return (int) (
                UNSAFE.getLongVolatile (this, OFFS_TOP) -
                UNSAFE.getLongVolatile (this, OFFS_BASE)
        );
    }

    /**
     * Add a new task to the top of the shared queue, incrementing 'top'.
     */
    @Override public void push (Runnable task) {
        lock ();

        final long _base;
        final long _top;
        try {
            _base = UNSAFE.getLongVolatile (this, OFFS_BASE);
            _top = UNSAFE.getLongVolatile (this, OFFS_TOP);

            if (_top == _base + mask) {
                throw new RejectedExecutionExceptionWithoutStacktrace ("Shared queue overflow");
            }

            // We hold a lock here, so there can be no concurrent modifications of 'top', and there is no need for CAS. We must however ensure that
            //  no concurrently reading thread sees the incremented 'top' without the task being present in the array, therefore the 'ordered' put.
            UNSAFE.putObjectVolatile (tasks, taskOffset (_top), task);
            UNSAFE.putLongVolatile (this, OFFS_TOP, _top+1);
        }
        finally {
            unlock ();
        }

        pool.onAvailableTask ();
    }

    @Override public synchronized Runnable popFifo (LocalQueue localQueue) {
        final long _base = base;
        final long _top = top;

        final long size = _top-_base;

        if (size == 0) {
            // Terminate the loop: the queue is empty.
            //TODO verify that Hotspot optimizes this kind of return-from-the-middle well
            return null;
        }

        final Runnable result = fetchTask (_base);
        if (result == null) {
            System.err.println ("null @ " + _base);
            return null; //TODO why is this necessary?
        }

        int idx;

        long newLocalTop = localQueue.top;
        for (idx=1; idx < size && idx < prefetchBatchSize; idx++) {
            final Runnable task = fetchTask (_base+idx);
            if (task == null) {
                System.err.println ("************************ fetched task[base+" + idx + "] is null although it really couldn't *******************************");
            }

            localQueue.tasks [localQueue.asArrayindex (newLocalTop++)] = task;
        }

        // volatile put for atomicity and to ensure ordering wrt. nulling the task --> read operations do not hold the same monitor
        if (idx > 1) {
            UNSAFE.putLongVolatile (localQueue, LocalQueue.OFFS_TOP, newLocalTop);
        }

        // volatile put for atomicity and to ensure ordering wrt. nulling the task --> read operations do not hold the same monitor
        UNSAFE.putLongVolatile (this, OFFS_BASE, _base + idx);

        return result;
    }

    private Runnable fetchTask (long idx) {
        final int arrIdx = asArrayIndex (idx);
        final Runnable result = tasks [arrIdx];
        if (result != null) tasks[arrIdx] = null; //TODO remove the check
        return result;
    }

    @Override public synchronized void clear () {
        final long _top = top;
        for (long _base=base; _base < _top; _base++) {
            tasks[asArrayIndex (_base)] = null;
        }

        UNSAFE.putLongVolatile (this, OFFS_BASE, _top);
    }

    private void lock() {
        //noinspection StatementWithEmptyBody
        while (!tryLock ()); //TODO add 'pause' when that becomes available
    }

    private boolean tryLock() {
        return UNSAFE.compareAndSwapInt (this, OFFS_LOCK, 0, 1);
    }

    private void unlock() {
        UNSAFE.putIntVolatile (this, OFFS_LOCK, 0);
    }

    private long taskOffset (long l) {
        return OFFS_TASKS + SCALE_TASKS * (l & mask);
    }

    private int asArrayIndex (long l) {
        return (int) (l & mask);
    }

    //------------- Unsafe stuff
    private static final Unsafe UNSAFE;

    private static final long OFFS_TASKS;
    private static final long SCALE_TASKS;

    private static final long OFFS_LOCK;
    private static final long OFFS_BASE;
    private static final long OFFS_TOP;

    static {
        try {
            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            UNSAFE = (Unsafe) f.get (null);

            OFFS_TASKS = UNSAFE.arrayBaseOffset (Runnable[].class);
            SCALE_TASKS = UNSAFE.arrayIndexScale (Runnable[].class);

            OFFS_LOCK = UNSAFE.objectFieldOffset (SharedQueueNonblockPushBlockPopImpl.class.getDeclaredField ("lock"));
            OFFS_BASE = UNSAFE.objectFieldOffset (SharedQueueNonblockPushBlockPopImpl.class.getDeclaredField ("base"));
            OFFS_TOP  = UNSAFE.objectFieldOffset (SharedQueueNonblockPushBlockPopImpl.class.getDeclaredField ("top"));
        }
        catch (Exception e) {
            AUnchecker.throwUnchecked (e);
            throw new RuntimeException(); // for the compiler
        }
    }
}
