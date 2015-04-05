package com.ajjpj.afoundation.collection.immutable;

import org.openjdk.jmh.annotations.*;

import java.util.Comparator;
import java.util.Random;


/**
 * @author arno
 */
@Fork(1)
@Threads (1)
@Warmup (iterations = 3, time = 1)
@Measurement (iterations = 3, time = 5)
@State (Scope.Benchmark)
public class AMapShotgunUpdateBenchmark {

    @Param ({"100",
//            "10000",
//            "1000000",
            "100000000"})
    private int size;

    @Param ({
            "AHashMap",
            "ALongHashMap64",
            "ARedBlackTree",
            "ALongRedBlackTree",
//            "ABTree4",
//            "ABTree8",
//            "ABTree16"
    })
    private String mapType;

    @Setup
    public void setUp() {
        final Comparator<Long> NATURAL_ORDER = new Comparator<Long> () {
            @SuppressWarnings ("unchecked")
            @Override public int compare (Long o1, Long o2) {
                return o1.compareTo (o2);
            }
        };


        switch (mapType) {
            case "AHashMap":      EMPTY = AHashMap.empty (); break;
            case "ALongHashMap64":EMPTY = ALongHashMap.empty (); break;
            case "ARedBlackTree": EMPTY = ARedBlackTreeMap.empty (NATURAL_ORDER); break;
            case "ALongRedBlackTree": EMPTY = ALongRedBlackTreeMap.empty (); break;
            case "ABTree4":       EMPTY = ABTreeMap.empty (new BTreeSpec (4, NATURAL_ORDER)); break;
            case "ABTree8":       EMPTY = ABTreeMap.empty (new BTreeSpec (8, NATURAL_ORDER)); break;
            case "ABTree16":      EMPTY = ABTreeMap.empty (new BTreeSpec (16, NATURAL_ORDER)); break;
            default: throw new IllegalArgumentException (mapType);
        }
    }

    private AMap<Long, Integer> EMPTY;

//    @Benchmark
    public void testShotgunUpdate() {
        final Random rand = new Random(12345);
        AMap<Long, Integer> a = EMPTY;

        final int numIters = 100_000;

        for(int i=0; i<numIters; i++) {
            final long key = rand.nextInt (size);
            final boolean add = rand.nextBoolean ();

            if (add) {
                final int value = rand.nextInt ();
                a = a.updated (key, value);
            }
            else {
                a = a.removed (key);
            }
        }
    }
}
