package com.ajjpj.afoundation.concurrent;


import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


public class Bench_Main {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder ()
                .include(PoolBenchmark.class.getName() + ".testFactorialMulti8")
//                .include(PoolBenchmark.class.getName() + ".*")
//                .mode (Mode.AverageTime)
//                .timeUnit(TimeUnit.MICROSECONDS)
//                .warmupTime(TimeValue.seconds(1))
//                .warmupIterations(2)
//                .measurementTime(TimeValue.seconds(1))
//                .measurementIterations(2)
//                .threads(2)
//                .forks(1)
//                .shouldFailOnError(true)
//                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                //.addProfiler(WinPerfAsmProfiler.class)
                .build();

//        new Runner(opt).list();
        new Runner (opt).run();
    }
}
