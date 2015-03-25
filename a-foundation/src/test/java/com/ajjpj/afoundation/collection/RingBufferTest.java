package com.ajjpj.afoundation.collection;

import com.ajjpj.afoundation.collection.mutable.ARingBuffer;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @author arno
 */
public class RingBufferTest {
    @Test
    public void testSingleThreaded() {
        final ARingBuffer<Long> rb = new ARingBuffer<>(Long.class, 100);

        assertFalse(rb.iterator().hasNext());
        try {
            rb.iterator().next();
            fail("exception expected");
        }
        catch(Exception exc) {
        }

        for(long i=1; i<2000; i++) {
            rb.put(i);

            final Iterator<Long> iter = rb.iterator();
            for(long j=Math.max(1, i-99); j<=i; j++) {
                assertTrue(iter.hasNext());
                assertEquals(Long.valueOf(j), iter.next());
            }
            assertFalse(iter.hasNext());
        }
    }

    @Test
    public void testClear() {
        final ARingBuffer<Long> rb = new ARingBuffer<>(Long.class, 100);
        assertFalse(rb.iterator().hasNext());

        rb.clear();
        assertFalse(rb.iterator().hasNext());

        rb.put(1L);
        assertTrue(rb.iterator().hasNext());
        rb.clear();
        assertFalse(rb.iterator().hasNext());

        for(long i=0; i<12345; i++) {
            rb.put(i);
        }
        assertTrue(rb.iterator().hasNext());
        rb.clear();
        assertFalse(rb.iterator().hasNext());
    }

    @Test
    public void testOneWriterManyReaders() {
        final int BUF_SIZE = 100;

        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final ARingBuffer<Long> rb = new ARingBuffer<>(Long.class, BUF_SIZE);

        for(int i=0; i<10; i++) {
            new Thread() {
                @Override public void run() {
                    while(! finished.get()) {
                        long prev = -1;
                        for(long val: rb) {
                            try {
                                if(prev != -1) {
                                    assertEquals(prev + 1, val);
                                }
                            }
                            catch (AssertionError | RuntimeException e) {
                                failed.set(true);
                                throw e;
                            }

                            prev = val;
                        }
                    }
                }
            }.start();
        }

        for(long i=1; i<10*1000*1000; i++) {
            rb.put(i);
        }
        finished.set(true);
        assertFalse(failed.get());
    }
}
