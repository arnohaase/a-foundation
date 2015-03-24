package com.ajjpj.abase.collection.immutable;

import org.openjdk.jmh.annotations.*;

import java.util.Comparator;
import java.util.Random;


/**
 * @author arno
 */
@Fork(0)
@Threads (1)
@Warmup (iterations = 3, time = 2)
@Measurement (iterations = 5, time = 4)
@State (Scope.Benchmark)
public class AMapBenchmark {

    @Param ({"AHashMap", "ARedBlackTree", "ABTree4", "ABTree8", "ABTree16"})
    private String mapType;

    @Setup
    public void setUp() {
        final Comparator<Integer> NATURAL_ORDER = new Comparator<Integer> () {
            @SuppressWarnings ("unchecked")
            @Override public int compare (Integer o1, Integer o2) {
                return o1.compareTo (o2);
            }
        };


        switch (mapType) {
            case "AHashMap": EMPTY = AHashMap.empty (); break;
            case "ARedBlackTree": EMPTY = ARedBlackTree.empty (NATURAL_ORDER); break;
            case "ABTree4":  EMPTY = ABTree.empty (new BTreeSpec ( 4, NATURAL_ORDER)); break;
            case "ABTree8":  EMPTY = ABTree.empty (new BTreeSpec ( 8, NATURAL_ORDER)); break;
            case "ABTree16": EMPTY = ABTree.empty (new BTreeSpec (16, NATURAL_ORDER)); break;
            default: throw new IllegalArgumentException (mapType);
        }
    }

    private AMap<Integer, Integer> EMPTY;

    final Random rand = new Random(12345);

    @Benchmark
    public void testShotgunUpdate() {
        AMap<Integer, Integer> a = EMPTY;

        final int numIters = 100_000;

        for(int i=0; i<numIters; i++) {
            final int key = rand.nextInt (100 * 1000);
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
