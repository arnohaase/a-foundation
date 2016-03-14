package com.ajjpj.afoundation.concurrent;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;


/**
 * @author arno
 */
public class ForkJoinOverflowTest {
    public static final int N = 4;

    final CountDownLatch latch = new CountDownLatch (N);
    final AtomicLong counter = new AtomicLong ();

    @Test
    public void testOverflow() throws InterruptedException {
        final ForkJoinPool pool = new ForkJoinPool (1);
        final long NUM_ITER = Integer.MAX_VALUE;


        pool.submit (() -> {
            sleep10 ();

            for (int i=0; i<N; i++) {
                new IncTask (NUM_ITER).fork ();
            }
        });

        latch.await ();
        System.out.println (counter.get ());
        assertEquals (NUM_ITER * N + N, counter.get ());
    }

    class IncTask extends ForkJoinTask {
        private final long l;

        public IncTask (long l) {
            this.l = l;
        }

        @Override protected boolean exec () {
            counter.incrementAndGet ();
            if (l > 0) {
                new IncTask (l-1).fork ();
            }
            else {
                latch.countDown ();
            }

            return true;
        }

        @Override public Object getRawResult () {
            return null;
        }
        @Override protected void setRawResult (Object value) {
        }
    }


    private void sleep10 () {
        try {
            Thread.sleep (10);
        }
        catch (InterruptedException e) {
            e.printStackTrace ();
        }
    }
}
