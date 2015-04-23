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
public class AThreadPoolBenchmark {
    private final AThreadPool threadPool = new AThreadPoolBuilder().buildFixedSize (Runtime.getRuntime ().availableProcessors ());

    public static void main (String[] args) throws InterruptedException {
        ForkJoinPool.commonPool ().execute (new MyTask (5));
        Thread.sleep (1000);
    }

    static class MyTask extends ForkJoinTask {
        private final int i;

        public MyTask (int i) {
            this.i = i;
        }

        @Override public Object getRawResult () {
            return null;
        }
        @Override protected void setRawResult (Object value) {
        }
        @Override protected boolean exec () {
            if (i > 0) {
                super.getPool ().execute (new MyTask (i-1));
            }

            System.out.println ("finsished " + i + " in " + Thread.currentThread ().getName () + ": " + isDone ());

            return true;
        }
    }



    @TearDown
    public void shutdown() {
        threadPool.shutdown ();
    }

    @Benchmark
    public void testAFuture() throws ExecutionException, InterruptedException {
        final List<AFuture<Integer>> futures = new ArrayList<> ();
        for (int i=0; i<1000; i++) {
            final int n = i;
            futures.add ((AFuture) threadPool.submit (() -> n, 1, TimeUnit.SECONDS)
                    .mapSync (this::inc)
                    .mapSync (this::timesTwo)
                    .mapSync (this::inc));
//                    .mapAsync (this::inc, 1, TimeUnit.SECONDS)
//                    .mapAsync (this::timesTwo, 1, TimeUnit.SECONDS)
//                    .mapAsync (this::inc, 1, TimeUnit.SECONDS));
        }

        for (AFuture f: futures) {
            f.get ();
        }
    }

    @Benchmark
    public void testCompletableFutureApplySync() throws ExecutionException, InterruptedException, TimeoutException {
        final List<CompletableFuture<Integer>> futures = new ArrayList<> ();
        for (int i=0; i<1000; i++) {
            final int n=i;
            futures.add ((CompletableFuture) CompletableFuture.supplyAsync (() -> n)
                    .thenApply (this::inc)
                    .thenApply (this::timesTwo)
                    .thenApply (this::inc));
        }

        for (CompletableFuture f: futures) {
            f.get (1, TimeUnit.SECONDS);
        }
    }

    @Benchmark
    public void testCompletableFutureApplyAsync() throws ExecutionException, InterruptedException, TimeoutException {
        final List<CompletableFuture<Integer>> futures = new ArrayList<> ();
        for (int i=0; i<1000; i++) {
            final int n=i;
            futures.add ((CompletableFuture) CompletableFuture.supplyAsync (() -> n)
                    .thenApplyAsync (this::inc)
                    .thenApplyAsync (this::timesTwo)
                    .thenApplyAsync (this::inc));
        }

        for (CompletableFuture f: futures) {
            f.get (1, TimeUnit.SECONDS);
        }
    }

    int inc (int n) {
        return n+1;
    }

    int timesTwo (int n) {
        return n*2;
    }
}
