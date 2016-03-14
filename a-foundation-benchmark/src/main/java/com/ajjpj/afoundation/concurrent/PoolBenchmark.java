package com.ajjpj.afoundation.concurrent;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.*;
import java.util.concurrent.ForkJoinPool;


/**
 * @author arno
 */
//@Fork (2)
//@Fork (0)
@Fork(1)
@Threads(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
//@Timeout (time=20, timeUnit=TimeUnit.SECONDS)
public class PoolBenchmark {
    public static final int TIMEOUT_SECONDS = 200;
    public static final int POOL_SIZE = Runtime.getRuntime ().availableProcessors ();

    static {
        System.out.println ("Running with " + POOL_SIZE + " threads on " + Runtime.getRuntime ().availableProcessors () + " processors");
    }

    ABenchmarkPool pool;

    @Param({
            "a-sync-block",
            "a-sync-nocheck",
            "a-lock-block",
            "a-nonblocking",

//            "a-strict-own",
            "no-conc",

//            "Executors.newFixedThreadPool",

            "ForkJoinSharedQueues",
            "ForkJoinLifo",
            "ForkJoinFifo",

            "J9FjSharedQueues",
            "J9FjLifo",
            "J9FjFifo"
    })
    public String strategy;

    public volatile Thread timeoutThread;

    @Setup
    public void setUp() {
        switch (strategy) {
            case "a-sync-block":   pool = new AThreadPoolAdapter (new AThreadPoolBuilder ().withNumThreads (POOL_SIZE).withSharedQueueStrategy (SharedQueueStrategy.SyncPush).withCheckShutdownOnSubmission (true). build ()); break;
            case "a-sync-nocheck": pool = new AThreadPoolAdapter (new AThreadPoolBuilder ().withNumThreads (POOL_SIZE).withSharedQueueStrategy (SharedQueueStrategy.SyncPush).withCheckShutdownOnSubmission (false).build ()); break;
            case "a-lock-block":   pool = new AThreadPoolAdapter (new AThreadPoolBuilder ().withNumThreads (POOL_SIZE).withSharedQueueStrategy (SharedQueueStrategy.LockPush).build ()); break;
            case "a-nonblocking":  pool = new AThreadPoolAdapter (new AThreadPoolBuilder ().withNumThreads (POOL_SIZE).withSharedQueueStrategy (SharedQueueStrategy.NonBlockingPush).build ()); break;

            //TODO no work stealing
            case "no-conc":        pool = new AThreadPoolAdapter (AThreadPoolWithAdmin.withDummyAdminApi (AThreadPool.SYNC_THREADPOOL)); break;

            case "Executors.newFixedThreadPool": pool = new DelegatingPool (Executors.newFixedThreadPool (POOL_SIZE)); break;

            case "ForkJoinSharedQueues": pool = new DelegatingPool (ForkJoinPool.commonPool ()); break;
            case "ForkJoinLifo":         pool = new ForkJoinForkingPool (createForkJoin (false)); break;
            case "ForkJoinFifo":         pool = new ForkJoinForkingPool (createForkJoin (true)); break;

            case "J9FjSharedQueues": pool = new DelegatingPool (createJ9ForkJoin (POOL_SIZE, false)); break;
            case "J9FjLifo":         pool = new J9NewForkingPool (createJ9ForkJoin (POOL_SIZE, false)); break;
            case "J9FjFifo":         pool = new J9NewForkingPool (createJ9ForkJoin (POOL_SIZE, true)); break;

            default: throw new IllegalStateException ();
        }

        timeoutThread = new Thread() {
            @Override public void run () {
                try {
                    Thread.sleep (1000 * TIMEOUT_SECONDS);
                }
                catch (InterruptedException e) {
                    e.printStackTrace ();
                }
                System.out.println ("*** timeout ***");
                System.exit (1);
            }
        };
        timeoutThread.start ();
    }

    private static ForkJoinPool createForkJoin (boolean fifo) {
        final ForkJoinPool p = ForkJoinPool.commonPool ();
        return new ForkJoinPool (p.getParallelism (), p.getFactory (), p.getUncaughtExceptionHandler (), fifo); //TODO use NUM_THREADS to actually limit the number of threads
    }

    private static com.ajjpj.afoundation.concurrent.jdk.j9new.ForkJoinPool createJ9ForkJoin (int numThreads, boolean fifo) {
        return new com.ajjpj.afoundation.concurrent.jdk.j9new.ForkJoinPool (numThreads, new J9LimitingForkJoinThreadFactory(numThreads), null, fifo);
    }

    @TearDown
    public void tearDown() throws InterruptedException {
        pool.shutdown ();
        Thread.sleep (500);

        System.out.println ();
        System.out.println ("---- Thread Pool Statistics ----");

        final AThreadPoolStatistics stats = pool.getStatistics ();
        for (ASharedQueueStatistics s: stats.sharedQueueStatisticses) {
            System.out.println (s);
        }
        for (AWorkerThreadStatistics s: stats.workerThreadStatistics) {
            System.out.println (s);
        }
        System.out.println ("--------------------------------");

        timeoutThread.stop ();
    }

    @Benchmark
    public void testSimpleScheduling01() throws InterruptedException {
        doSimpleScheduling (false);
    }

    @Benchmark
    public void ___testSimpleScheduling01WithWork() throws InterruptedException {
        doSimpleScheduling (true);
    }

    private void doSimpleScheduling(boolean withWork) throws InterruptedException {
        final int num = 1_000;
        final CountDownLatch latch = new CountDownLatch (num);

        for (int i=0; i<num; i++) {
            pool.submit (() -> {
                if (withWork) {
                    Blackhole.consumeCPU (100);
                }
                latch.countDown();
            });
        }
        latch.await ();
    }

    @Benchmark
    @Threads(7)
    public void testSimpleScheduling07() throws InterruptedException {
        doSimpleScheduling (false);
    }

    @Benchmark
    @Threads(7)
    public void testSimpleScheduling07WithWork() throws InterruptedException {
        doSimpleScheduling (true);
    }

    @Benchmark
    @Threads(8)
    public void testSimpleScheduling08() throws InterruptedException {
        doSimpleScheduling (false);
    }

    @Benchmark
    @Threads(8)
    public void testSimpleScheduling08WithWork() throws InterruptedException {
        doSimpleScheduling (true);
    }

    @Benchmark
    @Threads(15)
    public void testSimpleScheduling15() throws InterruptedException {
        doSimpleScheduling (false);
    }

    @Benchmark
    @Threads(15)
    public void testSimpleScheduling15WithWork() throws InterruptedException {
        doSimpleScheduling (true);
    }

    @Benchmark
    @Threads(16)
    public void testSimpleScheduling16() throws InterruptedException {
        doSimpleScheduling (false);
    }

    @Benchmark
    @Threads(16)
    public void testSimpleScheduling16WithWork() throws InterruptedException {
        doSimpleScheduling (true);
    }

    @Benchmark
    @Threads(7)
    public void recPar_00010a() throws InterruptedException {
        // This simulates a saturating load, which is one of the typical modes in which a thread pool is operating in server side software
        final CountDownLatch latch = new CountDownLatch (1000);
        for (int i=0; i<1000; i++) {
            doRec (10, latch);
        }
        latch.await ();
    }

    @Benchmark
    @Threads(7)
    public void recPar_00010b() throws InterruptedException {
        // This simulates a 'worst case' load - infinitesimal below saturation, i.e. when one task is finished, no new work is available, but immediately thereafter work is added
        final CountDownLatch latch = new CountDownLatch (1);
        doRec (10, latch);
        latch.await ();
    }

    @Benchmark
    @Threads(7)
    public void recPar_01000() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch (1);
        doRec (1_000, latch);
        latch.await ();
    }

    @Benchmark
    @Threads(7)
    public void recPar_10000() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch (1);
        doRec (10_000, latch);
        latch.await ();
    }

    void doRec (int level, CountDownLatch latch) {
        if (level == 0) {
            latch.countDown ();
        }
        else {
            pool.submit (() -> doRec (level-1, latch));
        }
    }

    @Benchmark
    public void testFactorial01() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

    @Benchmark
    @Threads(7)
    public void testFactorial07() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

    @Benchmark
    @Threads(8)
    public void testFactorial08() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

    @Benchmark
    @Threads(15)
    public void testFactorial15() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

    @Benchmark
    @Threads(16)
    public void testFactorial16() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

    void fact (long collect, int n, SettableFutureTask<Long> result) {
        if (n <= 1) {
            result.set (collect);
        }
        else {
            pool.submit (() -> fact (collect * n, n-1, result));
        }
    }

    @Benchmark
    public void testRecursiveFibo() throws ExecutionException, InterruptedException {
        try {
            fibo (8);
        }
        catch (Throwable e) {
            e.printStackTrace ();
            System.exit (0);
        }
    }

    long fibo (long n) throws ExecutionException, InterruptedException {
        if (n <= 1) return 1;

//        System.err.println (Thread.currentThread ().getName () + ":  calculating fibo " + n);

        return pool.submit (() -> fibo(n-1)).get() + pool.submit (() -> fibo(n-2)).get ();
    }

    @Benchmark
    public void testStealVeryCheap() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch (10_000);

        pool.submit (() -> {
           for (int i=0; i<10_000; i++) {
               pool.submit (latch::countDown);
           }
        });
        latch.await ();
    }

    @Benchmark
    public void testStealExpensive() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch (10_000);

        pool.submit (() -> {
           for (int i=0; i<10_000; i++) {
               pool.submit (() -> {
                   Blackhole.consumeCPU (100);
                   latch.countDown();
               });
           }
        });
        latch.await ();
    }

    @Benchmark
    public void testPingPong01() throws InterruptedException {
        testPingPong (1);
    }

    @Benchmark
    public void testPingPong02() throws InterruptedException {
        testPingPong (2);
    }

    @Benchmark
    public void testPingPong07() throws InterruptedException {
        testPingPong (7);
    }

    @Benchmark
    public void testPingPong08() throws InterruptedException {
        testPingPong (8);
    }

    @Benchmark
    public void testPingPong15() throws InterruptedException {
        testPingPong (15);
    }

    @Benchmark
    public void testPingPong16() throws InterruptedException {
        testPingPong (16);
    }

    @Benchmark
    public void testPingPong32() throws InterruptedException {
        testPingPong (32);
    }

    private void testPingPong(int numThreads) throws InterruptedException {
        final PingPongActor a1 = new PingPongActor ();
        final PingPongActor a2 = new PingPongActor ();

        final CountDownLatch latch = new CountDownLatch (numThreads);

        for (int i=0; i<numThreads; i++) {
            a1.receive (a2, 10_000, latch::countDown);
        }

        latch.await ();
    }

    class PingPongActor {
        void receive (PingPongActor sender, int remaining, Runnable onFinished) {
            if (remaining == 0) {
                onFinished.run ();
            }
            else {
                pool.submit (() -> sender.receive (PingPongActor.this, remaining-1, onFinished));
            }
        }
    }
}
