package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.util.AUnchecker;
import sun.misc.Contended;
import sun.misc.Unsafe;

import java.lang.reflect.Field;


/**
 * @author arno
 */
@Contended
class SharedQueueNonBlockingImpl implements ASharedQueue {
    long p1, p2, p3, p4, p5, p6, p7;

    private final int prefetchBatchSize;
    /**
     * an array holding all currently submitted tasks.
     */
    private final Runnable[] tasks;

    /**
     * a bit mask to project an offset into the valid range of offsets for the tasks array
     */
    private final int mask;

    private final AThreadPoolImpl pool;

    private long base = 0;
    private long top = 0;

    long q1, q2, q3, q4, q5, q6, q7;

    SharedQueueNonBlockingImpl (int prefetchBatchSize, AThreadPoolImpl pool, int size) {
        if (prefetchBatchSize < 1) throw new IllegalArgumentException ("worker threads must (attempt to) fetch a minimum of 1 task");
        this.prefetchBatchSize = prefetchBatchSize;
        this.pool = pool;

        if (1 != Integer.bitCount (size)) throw new IllegalArgumentException ("size must be a power of 2");
        if (size < 8 || size > 1024*1024) throw new IllegalArgumentException ("size must be in the range from 8 to " + (1024*1024));

        this.tasks = new Runnable[size];
        this.mask = size-1;
    }

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
        while (true) {
            final long _base = UNSAFE.getLongVolatile (this, OFFS_BASE);
            final long _top = top;

            if (_top == _base + mask) {
                throw new RejectedExecutionExceptionWithoutStacktrace ("Queue overflow");
            }

            final long taskOffset = taskOffset (_top);
            if (UNSAFE.compareAndSwapObject (tasks, taskOffset, null, task)) {
                // if the publishing thread is interrupted here, other publishers will effectively do a spin wait
                if (!UNSAFE.compareAndSwapLong (this, OFFS_TOP, _top, _top+1)) {
                    // there was a buffer wrap-around in the meantime --> undo the CAS 'put' operation and try again
                    UNSAFE.putObjectVolatile (tasks, taskOffset, null);
                    continue;
                }

                if (_top - _base <= 1) {
                    pool.onAvailableTask ();
                }
                break;
            }
        }
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

    private static final long OFFS_BASE;
    private static final long OFFS_TOP;

    static {
        try {
            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            UNSAFE = (Unsafe) f.get (null);

            OFFS_TASKS = UNSAFE.arrayBaseOffset (Runnable[].class);
            SCALE_TASKS = UNSAFE.arrayIndexScale (Runnable[].class);

            OFFS_BASE = UNSAFE.objectFieldOffset (SharedQueueNonBlockingImpl.class.getDeclaredField ("base"));
            OFFS_TOP  = UNSAFE.objectFieldOffset (SharedQueueNonBlockingImpl.class.getDeclaredField ("top"));
        }
        catch (Exception e) {
            AUnchecker.throwUnchecked (e);
            throw new RuntimeException(); // for the compiler
        }
    }
}
