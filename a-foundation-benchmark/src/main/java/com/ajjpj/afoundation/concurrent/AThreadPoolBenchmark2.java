package com.ajjpj.afoundation.concurrent;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * @author arno
 */
@Fork (1)
@Threads (1)
@Warmup (iterations = 3, time = 1)
@Measurement (iterations = 3, time = 5)
@State (Scope.Benchmark)
public class AThreadPoolBenchmark2 {
    private final ExecutorService es = Executors.newFixedThreadPool (10);

    @TearDown
    public void shutdown() {
        es.shutdown ();
    }

//    @Benchmark
    public void testDirect() throws Exception {
        final List<Future<Integer>> futures = new ArrayList<> ();
        for (int i=0; i<1000; i++) {
            final int n = i;
            futures.add (es.submit (() -> n));
        }

        for (Future f: futures) {
            f.get ();
        }
    }

//    @Benchmark
    public void testForkJoin() throws Exception {
        final List<Future<Integer>> futures = new ArrayList<> ();
        for (int i=0; i<1000; i++) {
            final int n = i;
            futures.add (ForkJoinPool.commonPool ().submit (() -> n));
        }

        for (Future f: futures) {
            f.get ();
        }
    }

    static final Callable<Integer> NO_CALLABLE = new Callable<Integer> () {
        @Override public Integer call () throws Exception {
            return null;
        }
    };

//    @Benchmark
    public void testIndirection() throws Exception {
        final List<Future<Integer>> futures = new ArrayList<> ();
        for (int i=0; i<1000; i++) {
            final int n = i;

            final MyFutureTask f = new MyFutureTask ();
            es.submit (() -> f.set (n));
            futures.add (f);

        }

        for (Future f: futures) {
            f.get ();
        }
    }

    static class MyFutureTask extends FutureTask<Integer> {
        public MyFutureTask () {
            super (NO_CALLABLE);
        }

        @Override public void set (Integer integer) {
            super.set (integer);
        }
    }


    int inc (int n) {
        return n+1;
    }

    int timesTwo (int n) {
        return n*2;
    }
}
