package com.ajjpj.afoundation.concurrent.pool;

import com.ajjpj.afoundation.concurrent.pool.a.APoolImpl;
import com.ajjpj.afoundation.concurrent.pool.a.ASchedulingStrategy;
import com.ajjpj.afoundation.concurrent.pool.a.WorkStealingPoolImpl;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;


/**
 * @author arno
 */
@Fork (0)
//@Fork (1)
@Threads (1)
@Warmup (iterations = 3, time = 1)
@Measurement (iterations = 3, time = 3)
@State (Scope.Benchmark)
public class PoolBenchmark {
    APool pool;

    @Param ({
//            "naive",
//            "a-global-queue",
            "work-stealing",
            "a-strict-own",
//            "Fixed",
            "ForkJoin"
    })
    public String strategy;

    @Setup
    public void setUp() {
        switch (strategy) {
            case "naive":          pool = new NaivePool (8); break;
            case "a-global-queue": pool = new APoolImpl (8, ASchedulingStrategy.SingleQueue ()).start (); break;
            case "a-strict-own":   pool = new APoolImpl (8, ASchedulingStrategy.OWN_FIRST_NO_STEALING).start (); break;
//            case "work-stealing":  pool = new WorkStealingPoolImpl (1).start (); break;
            case "work-stealing":  pool = new WorkStealingPoolImpl (8).start (); break;
            case "Fixed":          pool = new DelegatingPool (Executors.newFixedThreadPool (8)); break;
            case "ForkJoin":       pool = new DelegatingPool (ForkJoinPool.commonPool ()); break;
            default: throw new IllegalStateException ();
        }
    }

    @TearDown
    public void tearDown() throws InterruptedException {
        pool.shutdown ();

        if (pool instanceof WorkStealingPoolImpl) {
            final WorkStealingPoolImpl ws = (WorkStealingPoolImpl) pool;
            System.out.println ("wakeup / global / local: " + ws.getNumWakeups () + " / " + ws.getNumGlobalPushs () + " / " + ws.getNumLocalPushs ());
        }
    }

    @Benchmark
    public void testSimpleScheduling() throws InterruptedException {
        final int num = 10_000;
        final CountDownLatch latch = new CountDownLatch (num);

        for (int i=0; i<num; i++) {
            pool.submit (() -> {
                latch.countDown ();
                return null;});
        }
        latch.await ();
    }

//    @Benchmark
    public void testFactorialSingle() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

//    @Benchmark
    @Threads (7)
    public void testFactorialMulti7() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

//    @Benchmark
    @Threads (8)
    public void testFactorialMulti8() throws ExecutionException, InterruptedException {
        final SettableFutureTask<Long> fact = new SettableFutureTask<> (() -> null);
        fact (1, 12, fact);
        fact.get ();
    }

    void fact (long collect, int n, SettableFutureTask<Long> result) {
        if (n <= 1) {
            result.set (collect);
        }
        else {
            pool.submit (() -> {
                fact (collect * n, n-1, result);
                return null;
            });
        }
    }

    static class SettableFutureTask<T> extends FutureTask<T> {
        public SettableFutureTask (Callable<T> callable) {
            super (callable);
        }

        @Override public void set (T t) {
            super.set (t);
        }
    }

//    @Benchmark
    public void testRecursiveFibo() throws ExecutionException, InterruptedException {
        fibo (8);
    }

    long fibo (long n) throws ExecutionException, InterruptedException {
        if (n <= 1) return 1;

        final AFuture<Long> f = pool.submit (() -> fibo(n-1));
        return f.get() + pool.submit (() -> fibo(n-2)).get ();
    }

//    @Benchmark
    public void testPingPong1() throws InterruptedException {
        testPingPong (1);
    }

//    @Benchmark
    public void testPingPong2() throws InterruptedException {
        testPingPong (2);
    }

//    @Benchmark
    public void testPingPong7() throws InterruptedException {
        testPingPong (7);
    }

//    @Benchmark
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
                pool.submit (() -> {sender.receive (PingPongActor.this, remaining-1, onFinished); return null;});
            }
        }
    }
}
