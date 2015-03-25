package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.collection.tuples.ATuple3;
import com.ajjpj.afoundation.concurrent.AFuture;
import com.ajjpj.afoundation.concurrent.AThreadPool;
import com.ajjpj.afoundation.concurrent.AThreadPoolBuilder;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AFunction1NoThrow;
import com.ajjpj.afoundation.function.AStatement1NoThrow;
import com.ajjpj.afoundation.function.AStatement2NoThrow;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AThreadPoolTest {
    private AThreadPool threadPool;

    @After
    public void shutdown() {
        if (threadPool == null) {
            return;
        }
        threadPool.shutdown ();
        threadPool = null;
    }

    @Test
    public void testSimple() throws InterruptedException, TimeoutException, ExecutionException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final CountDownLatch latch = new CountDownLatch (1);
        final CountDownLatch latch2 = new CountDownLatch (10);

        final List<AFuture<Integer>> futures = new ArrayList<> ();
        for (int i=0; i<10; i++) {
            final int num = i;
            final AFuture<Integer> f = threadPool.submit (new Callable<Integer> () {
                @Override public Integer call () throws Exception {
                    latch.await ();
                    return num;
                }
            }, 1, TimeUnit.MINUTES);
            futures.add (f);
            f.onSuccess (new AStatement1NoThrow<Integer> () {
                @Override public void apply (Integer param) {
                    latch2.countDown ();
                }
            });
        }

        for (AFuture<Integer> f: futures) {
            assertTrue (! f.isCancelled ());
            assertTrue (! f.isFinished ());
        }

        latch.countDown ();
        latch2.await ();

        for (int i=0; i<10; i++) {
            final AFuture<Integer> f = futures.get (i);
            assertTrue (f.isFinished ());
            assertTrue (! f.isCancelled ());

            assertEquals (Integer.valueOf (i), f.get (0, TimeUnit.MILLISECONDS));
        }
    }

    @Test
    public void testFixedMaxSize() throws InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (1);

        final CountDownLatch latch = new CountDownLatch (1);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                latch.await ();
                return 1;
            }
        }, 1, TimeUnit.SECONDS);

        // this second task is blocked because all threads in the pool are in use
        final AFuture<Integer> f2 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                return 2;
            }
        }, 1, TimeUnit.SECONDS);

        Thread.sleep (100);

        assertFalse (f1.isFinished ()); // blocked by latch
        assertFalse (f2.isFinished ()); // not yet scheduled

        latch.countDown ();

        Thread.sleep (100);

        assertTrue (f1.isFinished ());
        assertTrue (f2.isFinished ());
    }

    @Test
    public void testDynamicMaxSize() throws InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildDynamicSize (1, 2);

        final CountDownLatch latch = new CountDownLatch (1);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                latch.await ();
                return 1;
            }
        }, 1, TimeUnit.SECONDS);
        final AFuture<Integer> f2 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                latch.await ();
                return 1;
            }
        }, 1, TimeUnit.SECONDS);

        final AFuture<Integer> f3;
        try {
            f3 = threadPool.submit (new Callable<Integer> () {
                @Override public Integer call () throws Exception {
                    return 2;
                }
            }, 1, TimeUnit.SECONDS);
            fail ("exception expected");
        }
        catch (Exception e) {
        }
        latch.countDown ();
    }

    @Test
    public void testTimeout() throws InterruptedException, ExecutionException {
        threadPool = new AThreadPoolBuilder ()
                .setInterruptOnTimeout (true)
                .buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                Thread.sleep (1000);
                return 1;
            }
        }, 1, TimeUnit.MILLISECONDS);

        Thread.sleep (10);

        assertEquals (true, f1.isFinished ());

        try {
            f1.get ();
            fail ("exception expected");
        }
        catch (ExecutionException exc) {
            assertEquals (TimeoutException.class, exc.getCause ().getClass ());
        }
    }

    @Test
    public void testCancel() throws InterruptedException, ExecutionException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final CountDownLatch latch = new CountDownLatch (1);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                latch.await ();
                return 1;
            }
        }, 100, TimeUnit.MILLISECONDS);

        final AtomicInteger count = new AtomicInteger (0);

        final AStatement2NoThrow<Integer, Throwable> listener = new AStatement2NoThrow<Integer, Throwable> () {
            @Override public void apply (Integer param1, Throwable param2) {
                assertNull (param1);
                assertTrue (param2 instanceof CancellationException);
                count.incrementAndGet ();
            }
        };

        f1.onFinished (listener);

        f1.cancel (false);

        f1.onFinished (listener);

        assertEquals (true, f1.isFinished ());
        assertEquals (true, f1.isCancelled ());

        latch.countDown ();

        assertEquals (true, f1.isFinished ());
        assertEquals (true, f1.isCancelled ());

        f1.onFinished (listener);

        try {
            f1.get ();
            fail ("exception expected");
        }
        catch (CancellationException exc) { // expected
        }

        f1.onFinished (listener);

        assertEquals (4, count.get ());
    }

    @Test
    public void testMap() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                return 1;
            }
        }, 1, TimeUnit.SECONDS);

        final int result = f1
                .mapSync (new AFunction1NoThrow<Integer, Integer> () {
                    @Override public Integer apply (Integer param) {
                        return param + 2;
                    }
                })
                .mapAsync (new AFunction1NoThrow<Integer, Integer> () {
                    @Override public Integer apply (Integer param) {
                        return param * 3;
                    }
                }, 1, TimeUnit.SECONDS)
                .get ();

        assertEquals (9, result);
    }

    @Test
    public void testDefaultOnFailure() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                throw new RuntimeException ();
            }
        }, 1, TimeUnit.SECONDS);
        final AFuture<Integer> f2 = f1.withDefaultValue (99);

        assertEquals (Integer.valueOf (99), f2.get ());
    }

    @Test
    public void testDefaultOnTimeout() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                Thread.sleep (1000);
                return 1;
            }
        }, 1, TimeUnit.MILLISECONDS);
        final AFuture<Integer> f2 = f1.withDefaultValue (123);

        assertEquals (Integer.valueOf (123), f2.get ());
    }

    @Test
    public void testCallbacksOnResult() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final CountDownLatch latch = new CountDownLatch (1);
        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                latch.await ();
                return 1;
            }
        }, 100, TimeUnit.MILLISECONDS);

        final AtomicInteger successCounter = new AtomicInteger (0);
        final AtomicInteger failureCounter = new AtomicInteger (0);
        final AtomicInteger finishedCounter = new AtomicInteger (0);

        f1.onSuccess (new AStatement1NoThrow<Integer> () {
            @Override public void apply (Integer param) {
                successCounter.incrementAndGet ();
            }
        });
        f1.onFailure (new AStatement1NoThrow<Throwable> () {
            @Override public void apply (Throwable param) {
                failureCounter.incrementAndGet ();
            }
        });
        f1.onFinished (new AStatement2NoThrow<Integer, Throwable> () {
            @Override public void apply (Integer param1, Throwable param2) {
                assertNotNull (param1);
                assertNull (param2);
                finishedCounter.incrementAndGet ();
            }
        });

        latch.countDown ();
        f1.get ();

        // register a callback after the future is completed
        f1.onSuccess (new AStatement1NoThrow<Integer> () {
            @Override public void apply (Integer param) {
                successCounter.incrementAndGet ();
            }
        });
        f1.onFailure (new AStatement1NoThrow<Throwable> () {
            @Override public void apply (Throwable param) {
                failureCounter.incrementAndGet ();
            }
        });
        f1.onFinished (new AStatement2NoThrow<Integer, Throwable> () {
            @Override public void apply (Integer param1, Throwable param2) {
                assertNotNull (param1);
                assertNull (param2);
                finishedCounter.incrementAndGet ();
            }
        });

        assertEquals (2, successCounter.get ());
        assertEquals (0, failureCounter.get ());
        assertEquals (2, finishedCounter.get ());
    }

    @Test
    public void testCallbacksOnFailure() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final CountDownLatch latch = new CountDownLatch (1);
        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                latch.await ();
                throw new RuntimeException ();
            }
        }, 100, TimeUnit.MILLISECONDS);

        final AtomicInteger successCounter = new AtomicInteger (0);
        final AtomicInteger failureCounter = new AtomicInteger (0);
        final AtomicInteger finishedCounter = new AtomicInteger (0);

        f1.onSuccess (new AStatement1NoThrow<Integer> () {
            @Override public void apply (Integer param) {
                successCounter.incrementAndGet ();
            }
        });
        f1.onFailure (new AStatement1NoThrow<Throwable> () {
            @Override public void apply (Throwable param) {
                failureCounter.incrementAndGet ();
            }
        });
        f1.onFinished (new AStatement2NoThrow<Integer, Throwable> () {
            @Override public void apply (Integer param1, Throwable param2) {
                assertNull (param1);
                assertNotNull (param2);
                finishedCounter.incrementAndGet ();
            }
        });

        latch.countDown ();
        try {
            f1.get ();
        }
        catch (Exception e) {
        }

        // register a callback after the future is completed
        f1.onSuccess (new AStatement1NoThrow<Integer> () {
            @Override public void apply (Integer param) {
                successCounter.incrementAndGet ();
            }
        });
        f1.onFailure (new AStatement1NoThrow<Throwable> () {
            @Override public void apply (Throwable param) {
                failureCounter.incrementAndGet ();
            }
        });
        f1.onFinished (new AStatement2NoThrow<Integer, Throwable> () {
            @Override public void apply (Integer param1, Throwable param2) {
                assertNull (param1);
                assertNotNull (param2);
                finishedCounter.incrementAndGet ();
            }
        });

        assertEquals (0, successCounter.get ());
        assertEquals (2, failureCounter.get ());
        assertEquals (2, finishedCounter.get ());
    }

    @Test
    public void testCallbacksOnTimeout() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                Thread.sleep (10);
                return 0;
            }
        }, 1, TimeUnit.MILLISECONDS);

        final AtomicInteger successCounter = new AtomicInteger (0);
        final AtomicInteger failureCounter = new AtomicInteger (0);
        final AtomicInteger finishedCounter = new AtomicInteger (0);

        f1.onSuccess (new AStatement1NoThrow<Integer> () {
            @Override public void apply (Integer param) {
                successCounter.incrementAndGet ();
            }
        });
        f1.onFailure (new AStatement1NoThrow<Throwable> () {
            @Override public void apply (Throwable param) {
                failureCounter.incrementAndGet ();
            }
        });
        f1.onFinished (new AStatement2NoThrow<Integer, Throwable> () {
            @Override public void apply (Integer param1, Throwable param2) {
                assertNull (param1);
                assertNotNull (param2);
                finishedCounter.incrementAndGet ();
            }
        });

        try {
            f1.get ();
        }
        catch (Exception e) {
        }

        // register a callback after the future is completed
        f1.onSuccess (new AStatement1NoThrow<Integer> () {
            @Override public void apply (Integer param) {
                successCounter.incrementAndGet ();
            }
        });
        f1.onFailure (new AStatement1NoThrow<Throwable> () {
            @Override public void apply (Throwable param) {
                failureCounter.incrementAndGet ();
            }
        });
        f1.onFinished (new AStatement2NoThrow<Integer, Throwable> () {
            @Override public void apply (Integer param1, Throwable param2) {
                assertNull (param1);
                assertNotNull (param2);
                finishedCounter.incrementAndGet ();
            }
        });

        assertEquals (0, successCounter.get ());
        assertEquals (2, failureCounter.get ());
        assertEquals (2, finishedCounter.get ());
    }

    @Test
    public void testSubmitAll() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final List<AFuture<Integer>> futures = threadPool.submitAll (Arrays.asList (1, 2, 3, 4, 5), new AFunction1NoThrow<Integer, Integer> () {
            @Override public Integer apply (Integer param) {
                return param + 2;
            }
        }, 1, TimeUnit.SECONDS);

        for (int i=0; i<5; i++) {
            assertEquals (Integer.valueOf (i+3), futures.get (i).get ());
        }
    }

    @Test
    public void testSubmitAllWithDefault() throws Exception {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final List<AFuture<Integer>> futures = threadPool.submitAllWithDefaultValue (Arrays.asList (1, 2, 3, 4, 5), new AFunction1<Integer, Integer, Exception> () {
            @Override public Integer apply (Integer param) throws Exception {
                Thread.sleep (10);
                return param + 2;
            }
        }, 1, TimeUnit.MILLISECONDS, 99);

        for (int i=0; i<5; i++) {
            assertEquals (Integer.valueOf (99), futures.get (i).get ());
        }
    }

    @Test
    public void testZip2() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                return 1;
            }
        }, 1, TimeUnit.SECONDS);
        final AFuture<Integer> f2 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                return 2;
            }
        }, 1, TimeUnit.SECONDS);

        final AFuture<ATuple2<Integer, Integer>> zipped = f1.zip (f2);

        assertEquals (new ATuple2<> (1, 2), zipped.get ());
    }

    @Test
    public void testZip3() throws ExecutionException, InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                return 1;
            }
        }, 1, TimeUnit.SECONDS);
        final AFuture<Integer> f2 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                return 2;
            }
        }, 1, TimeUnit.SECONDS);
        final AFuture<Integer> f3 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                return 3;
            }
        }, 1, TimeUnit.SECONDS);

        final AFuture<ATuple3<Integer, Integer, Integer>> zipped = f1.zip (f2, f3);

        assertEquals (new ATuple3<> (1, 2, 3), zipped.get ());
    }

    @Test
    public void testFailurePropagation() throws InterruptedException {
        threadPool = new AThreadPoolBuilder ().buildFixedSize (10);

        final AFuture<Integer> f1 = threadPool.submit (new Callable<Integer> () {
            @Override public Integer call () throws Exception {
                throw new RuntimeException ();
            }
        }, 1, TimeUnit.SECONDS);

        final AFuture<Integer> f2 = f1.mapSync (new AFunction1NoThrow<Integer, Integer> () {
            @Override public Integer apply (Integer param) {
                return param+1;
            }
        });
        final AFuture<Integer> f3 = f2.mapAsync (new AFunction1NoThrow<Integer, Integer> () {
            @Override public Integer apply (Integer param) {
                return param*3;
            }
        }, 1, TimeUnit.SECONDS);

        try {
            f3.get ();
            fail ("exception expected");
        }
        catch (ExecutionException exc) {
            assertEquals (RuntimeException.class, exc.getCause ().getClass ());
        }
    }

    //TODO test dynamic limitations
}


