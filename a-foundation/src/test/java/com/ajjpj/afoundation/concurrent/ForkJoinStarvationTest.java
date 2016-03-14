package com.ajjpj.afoundation.concurrent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class ForkJoinStarvationTest {
    final AtomicLong shared = new AtomicLong ();
    final AtomicLong perThread = new AtomicLong ();

    volatile boolean isShutdown;

    ForkJoinPool pool;

    @Before
    public void setUp() {
        pool = new ForkJoinPool ();
        isShutdown = false;

        shared.set (0);
        perThread.set (0);
    }

    @After
    public void tearDown() throws InterruptedException {
        isShutdown = true;
        pool.shutdown ();
        assertTrue (pool.awaitTermination (1, TimeUnit.SECONDS));
    }

    @Test
    public void testSharedQueueStarvation() throws InterruptedException {
        for (int i=0; i<100; i++) {
            pool.execute (new ForkingTask (-1));
        }

        // Give the pool time to get plenty of work done - whatever remains after this interval is assumed never to happen
        Thread.sleep (2000);

        // Each worker thread has tasks in its local queue after initial processing of a task from a shared queue, so tasks in shared queues are
        //  starved subsequently
        assertEquals (pool.getParallelism (), shared.intValue ());
    }

    @Test
    public void testLocalQueueLifoStarvation() throws InterruptedException {
        for (int i=0; i<100; i++) {
            pool.execute (new ForkingTask (-2));
        }

        // Give the pool time to get plenty of work done - whatever remains after this interval is assumed never to happen
        Thread.sleep (2000);

        assertEquals (pool.getParallelism (), perThread.intValue ());
    }

    @Test
    public void testLocalQueueFifo() throws InterruptedException {
        pool = new ForkJoinPool (pool.getParallelism (), pool.getFactory (), pool.getUncaughtExceptionHandler (), true);

        for (int i=0; i<100; i++) {
            pool.execute (new ForkingTask (-2));
        }

        // Give the pool time to get plenty of work done - whatever remains after this interval is assumed never to happen
        Thread.sleep (2000);

        assertEquals (2* pool.getParallelism (), perThread.intValue ());
    }

    class ForkingTask extends ForkJoinTask {
        private final int level;
        public ForkingTask (int level) {
            this.level = level;
        }

        @Override public Object getRawResult () {
            return null;
        }
        @Override protected void setRawResult (Object value) {
        }
        @Override protected boolean exec () {
            if (isShutdown) return true;

            // A negative level is submitted to a shared queue and causes abs(level) tasks with level 1 to be submitted.
            if (level <= 0) {
                try {
                    // 'stealing before shared queue': Delay submission of a new task to allow all worker threads to fetch their first task from a shared queue
                    Thread.sleep (10);
                }
                catch (InterruptedException e) {
                    e.printStackTrace ();
                }

                for (int i=level; i<0; i++) {
                    new ForkingTask (1).fork ();
                }
                shared.incrementAndGet ();
            }
            else if (level == 1) {
                // level 1 is the first level to be submitted to a worker thread's local queue, incrementing 'perTread' counter
                new ForkingTask (2).fork ();
                perThread.incrementAndGet ();
            }
            else {
                new ForkingTask (2).fork ();
            }

            return true;
        }
    }
}
