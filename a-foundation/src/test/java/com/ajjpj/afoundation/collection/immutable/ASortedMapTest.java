package com.ajjpj.afoundation.collection.immutable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;


/**
 * @author arno
 */
@RunWith (Parameterized.class)
public class ASortedMapTest {
    private static final Comparator<Comparable> NATURAL_ORDER = new Comparator<Comparable> () {
        @SuppressWarnings ("unchecked")
        @Override public int compare (Comparable o1, Comparable o2) {
            return o1.compareTo (o2);
        }
    };

    @Parameterized.Parameters
    public static Collection<Object> emptyMaps () {
        return Arrays.<Object>asList (
//                new Object[] {ABTreeMap.empty (new ABTreeSpec (4, NATURAL_ORDER))},
//                new Object[] {ABTreeMap.empty (new ABTreeSpec (8, NATURAL_ORDER))},
//                new Object[] {ABTreeMap.empty (new ABTreeSpec (16, NATURAL_ORDER))},
                (Object) new Object[] {ARedBlackTreeMap.empty (NATURAL_ORDER)}
//                new Object[] {ALongRedBlackTreeMap.empty ()}
        );
    }

    private final ASortedMap<Long,Integer> EMPTY;

    public ASortedMapTest (ASortedMap<Long, Integer> emptyMap) {
        this.EMPTY = emptyMap;
    }

    @Test
    public void testFirstLast() {
        assertTrue (EMPTY.first ().isEmpty ());
        assertTrue (EMPTY.last ().isEmpty ());

        for (int start=0; start<100; start++) {
            for (int end=start; end < 110; end++) {
                for (int asc=0; asc<2; asc++) {
                    final ASortedMap<Long, Integer> map = fill (start, end, asc > 0);

                    assertEquals (Long.valueOf (start), map.first ().get ().getKey ());
                    assertEquals (Long.valueOf (end), map.last ().get ().getKey ());
                    assertEquals (Integer.valueOf (start), map.first ().get ().getValue ());
                    assertEquals (Integer.valueOf (end), map.last ().get ().getValue ());
                }
            }
        }
    }

    private ASortedMap<Long,Integer> fill(long start, long end, boolean asc) {
        ASortedMap<Long,Integer> map = EMPTY;

        if (asc) {
            for (long i=start; i<=end; i+=2) {
                map = map.updated (i, (int) i);
            }
        }
        else {
            for (long i=end; i>=start; i-=2) {
                map = map.updated (i, (int) i);
            }
        }
        return map;
    }

    @Test
    public void testFirstLastWithParam() {
        assertTrue (EMPTY.firstGreaterOrEquals (1L).isEmpty ());
        assertTrue (EMPTY.firstGreaterThan (1L).isEmpty ());
        assertTrue (EMPTY.lastSmallerThan (1L).isEmpty ());
        assertTrue (EMPTY.lastSmallerOrEquals (1L).isEmpty ());

        for (int start=0; start<100; start+=2) {
            for (int end=start; end < 110; end+=2) {
                for (int asc=0; asc<2; asc++) {
                    final ASortedMap<Long, Integer> map = fill (start, end, asc > 0);

                    for (long i = start; i <= end; i++) {
                        assertEquals (i, map.firstGreaterOrEquals (i).get ().getKey ().longValue ());
                        assertEquals (i, map.lastSmallerOrEquals (i).get ().getKey ().longValue ());

                        if (i < end) assertEquals (i + 1, map.firstGreaterThan (i).get ().getKey ().longValue ());
                        else assertTrue (map.firstGreaterThan (i).isEmpty ());
                        if (i > start) assertEquals (i - 1, map.lastSmallerThan (i).get ().getKey ().longValue ());
                        else assertTrue (map.lastSmallerThan (i).isEmpty ());
                    }
                }
            }
        }
    }

    private void checkEq (long min, long max, Iterable<AMapEntry<Long,Integer>> data) {
        if (min%2 == 1) min += 1;
        if (max%2 == 1) max -= 1;

        final Iterator<AMapEntry<Long,Integer>> iter = data.iterator ();
        for (long i=min; i<=max; i+=2) {
//            System.out.println ("    testing for " + i);
            assertTrue (iter.hasNext ());
            assertEquals (Long.valueOf(i), iter.next ().getKey ());
        }
        assertFalse (iter.hasNext ());
    }

    private void dump (Iterable<AMapEntry<Long,Integer>> part) {
        for (AMapEntry<Long,Integer> entry: part) {
            System.out.print (entry.getKey () +" ");
        }
        System.out.println ();
    }

    @Test
    public void testRange() {
//        assertFalse (EMPTY.rangeEE (0L, 1L).iterator ().hasNext ());
//        assertFalse (EMPTY.rangeEI (0L, 1L).iterator ().hasNext ());
//        assertFalse (EMPTY.rangeIE (0L, 1L).iterator ().hasNext ());
        assertFalse (EMPTY.rangeII (0L, 1L).iterator ().hasNext ());

        //TODO toString implementation for ranges

        for (long start=0; start<100; start+=2) {
            for (long end=start; end < 110; end+=2) {
                for (int asc=0; asc<2; asc++) {
                    final ASortedMap<Long, Integer> map = fill (start, end, asc > 0);
//                    System.out.print (start + " to " + end + (asc > 0 ? " asc" : " desc") + ": ");
//                    dump (map);

                    for (long rangeStart=start-1; rangeStart<=end+1; rangeStart++) {
                        for (long rangeEnd=end-5; rangeEnd <= end+1; rangeEnd++) {

//                        checkEq (Math.min (i+1, end-1), end-2, map.rangeEE (i, end-1));
//                        checkEq (Math.min (i+1, end-1), end-1, map.rangeEE (i, end));
//                        checkEq (Math.min (i+1, end-1), end,   map.rangeEE (i, end+1));
//
//                        checkEq (Math.min (i+1, end-1), end-1, map.rangeEI (i, end-1));
//                        checkEq (Math.min (i+1, end-1), end,   map.rangeEI (i, end));
//                        checkEq (Math.min (i+1, end-1), end+1, map.rangeEI (i, end+1));
//
//                        checkEq (Math.min (i,   end-1), end-2, map.rangeIE (i, end-1));
//                        checkEq (Math.min (i,   end-1), end-1, map.rangeIE (i, end));
//                        checkEq (Math.min (i,   end-1), end,   map.rangeIE (i, end+1));


//                            System.out.print ("  [" + rangeStart + ", " + rangeEnd + "] -> ");
//                            dump (map.rangeII (rangeStart, rangeEnd));

                            checkEq (Math.max (start, rangeStart), Math.min (end, rangeEnd), map.rangeII (rangeStart, rangeEnd));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testFromTo() {
        fail ("todo");
    }
}
