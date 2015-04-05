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
public class AMapReadBenchmark {
    @Param ({"100",
//            "10000",
            "1000000"})
    private int size;

    @Param ({
//            "AHashMap",
//            "ALongHashMap64",
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
            case "AHashMap": map = AHashMap.empty (); break;
            case "ALongHashMap64": map = ALongHashMap.empty (); break;
            case "ARedBlackTree": map = ARedBlackTreeMap.empty (NATURAL_ORDER); break;
            case "ALongRedBlackTree": map = ALongRedBlackTreeMap.empty (); break;
            case "ABTree4":  map = ABTreeMap.empty (new BTreeSpec (4, NATURAL_ORDER)); break;
            case "ABTree8":  map = ABTreeMap.empty (new BTreeSpec (8, NATURAL_ORDER)); break;
            case "ABTree16": map = ABTreeMap.empty (new BTreeSpec (16, NATURAL_ORDER)); break;
            default: throw new IllegalArgumentException (mapType);
        }

        for (int i=0; i<size; i++) {
            map = map.updated ((long) i, i);
        }
    }

    private AMap<Long, Integer> map;

//    @Benchmark
    public void testRandomRead() {
        final Random rand = new Random (12345);

        final int numIters = 1_000_000;

        for (int i=0; i<numIters; i++) {
            map.get ((long) rand.nextInt (size));
        }
    }

    @Benchmark
    public void testIterate() {
        for (int i=0; i<1_000_000/size; i++) {
            for (AMapEntry el : map) {
                // just iterate
                Object key = el.getKey ();
            }
        }
    }
}
