package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.util.AUnchecker;
import sun.misc.Unsafe;

import java.lang.reflect.Field;


/**
 * @author arno
 */
class SharedQueueBlockPushBlockPopImpl implements ASharedQueue {
    private final int prefetchBatchSize;
    private final Object PUSH_LOCK = new Object ();

    /**
     * an array holding all currently submitted tasks.
     */
    final Runnable[] tasks;

    //TODO here and elsewhere: memory layout
    /**
     * a bit mask to project an offset into the valid range of offsets for the tasks array
     */
    private final int mask;

    final AThreadPoolImpl pool;

    long base = 0;
    long top = 0;

    /**
     * @param prefetchBatchSize the number of tasks a worker thread should attempt to fetch when its local queue has run empty, to the degree they are available in this
     *                          queue. A value of 1 denotes no prefetch, i.e. a worker thread just fetches one item and is done with. A value of 2 means a worker thread
     *                          attempts to prefetch one task in addition to the task it gets for immediate consumption etc.
     */
    SharedQueueBlockPushBlockPopImpl (int prefetchBatchSize, AThreadPoolImpl pool, int size) {
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
        final long _base;
        final long _top;

        synchronized (PUSH_LOCK) {
            _base = base;
            _top = top;

            if (_top == _base + mask) {
                throw new RejectedExecutionExceptionWithoutStacktrace ("Shared queue overflow");
            }

            tasks[asArrayIndex (_top)] = task;

            // volatile put for atomicity and to ensure ordering wrt. storing the task
            UNSAFE.putLongVolatile (this, OFFS_TOP, _top+1);
        }

        pool.onAvailableTask ();
    }

    int asArrayIndex (long l) {
        return (int) (l & mask);
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

    //------------- Unsafe stuff
    static final Unsafe UNSAFE;

    static final long OFFS_BASE;
    static final long OFFS_TOP;

    static {
        try {
            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            UNSAFE = (Unsafe) f.get (null);

            OFFS_BASE = UNSAFE.objectFieldOffset (SharedQueueBlockPushBlockPopImpl.class.getDeclaredField ("base"));
            OFFS_TOP  = UNSAFE.objectFieldOffset (SharedQueueBlockPushBlockPopImpl.class.getDeclaredField ("top"));
        }
        catch (Exception e) {
            AUnchecker.throwUnchecked (e);
            throw new RuntimeException(); // for the compiler
        }
    }
}
