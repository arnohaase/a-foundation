package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.tuples.ATuple2;
import org.openjdk.jmh.annotations.*;

import java.util.Comparator;
import java.util.Random;


/**
 * @author arno
 */
@Fork(1)
@Threads (1)
@Warmup (iterations = 3, time = 1)
@Measurement (iterations = 5, time = 10)
@State (Scope.Benchmark)
public class AMapReadBenchmark {
    @Param ({"100", "10000", "1000000"})
    private int size;

    @Param ({"AHashMap", "ALongHashMap", "ARedBlackTree", "ABTree4", "ABTree8", "ABTree16"})
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
            case "ALongHashMap": map = ALongHashMap.empty (); break;
            case "ARedBlackTree": map = ARedBlackTree.empty (NATURAL_ORDER); break;
            case "ABTree4":  map = ABTree.empty (new BTreeSpec (4, NATURAL_ORDER)); break;
            case "ABTree8":  map = ABTree.empty (new BTreeSpec ( 8, NATURAL_ORDER)); break;
            case "ABTree16": map = ABTree.empty (new BTreeSpec (16, NATURAL_ORDER)); break;
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

//    @Benchmark
    public void testIterate() {
        for (int i=0; i<1_000_000/size; i++) {
            for (ATuple2 el : map) {
                // just iterate
                Object key = el._1;
            }
        }
    }
}
