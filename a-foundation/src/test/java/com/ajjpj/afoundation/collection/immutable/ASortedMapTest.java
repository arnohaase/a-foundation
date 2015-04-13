package com.ajjpj.afoundation.collection.immutable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.*;


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

        for (int start=0; start<100; start+=2) {
            for (int end=start; end < 110; end+=2) {
                for (int asc=0; asc<3; asc++) {
                    final ASortedMap<Long, Integer> map = fill (start, end, asc);

                    assertEquals (Long.valueOf (start), map.first ().get ().getKey ());
                    assertEquals (Long.valueOf (end),   map.last  ().get ().getKey ());
                }
            }
        }
    }

    private ASortedMap<Long,Integer> fill(long start, long end, int asc) {
        ASortedMap<Long,Integer> map = EMPTY;

        switch (asc) {
            case 0:
                for (long i=start; i<=end; i+=2) {
                    map = map.updated (i, (int) i);
                }
                break;
            case 1:
                for (long i=end; i>=start; i-=2) {
                    map = map.updated (i, (int) i);
                }
                break;
            case 2:
                final List<Long> list = new ArrayList<> ();
                for (long i=start; i<=end; i+=2) {
                    list.add (i);
                }
                Collections.shuffle (list, new Random (12345));
                for (Long l: list) {
                    map = map.updated (l, l.intValue ());
                }
                break;
        }

        return map;
    }

    @Test
    public void testFirstLastWithParam() {
        assertTrue (EMPTY.firstGreaterOrEquals (1L).isEmpty ());
        assertTrue (EMPTY.firstGreaterThan (1L).isEmpty ());
        assertTrue (EMPTY.lastSmallerThan (1L).isEmpty ());
        assertTrue (EMPTY.lastSmallerOrEquals (1L).isEmpty ());

        for (long start=0; start<100; start+=2) {
            for (long end=start; end < 110; end+=2) {
                for (int asc=0; asc<3; asc++) {
                    final ASortedMap<Long, Integer> map = fill (start, end, asc);

                    assertFalse (map.firstGreaterOrEquals (end+1).isDefined ());
                    assertFalse (map.firstGreaterOrEquals (end+2).isDefined ());

                    assertFalse (map.firstGreaterThan (end).isDefined ());
                    assertFalse (map.firstGreaterThan (end + 1).isDefined ());
                    assertFalse (map.firstGreaterThan (end + 2).isDefined ());

                    assertFalse (map.lastSmallerOrEquals (start-1).isDefined ());
                    assertFalse (map.lastSmallerOrEquals (start-2).isDefined ());

                    assertFalse (map.lastSmallerThan (start).isDefined ());
                    assertFalse (map.lastSmallerThan (start - 1).isDefined ());
                    assertFalse (map.lastSmallerThan (start - 2).isDefined ());

//                    dump (map);
                    for (long i = start-2; i <= end+2; i++) {
//                        System.out.println ("  " + i + " -> " + map.lastSmallerOrEquals (i));

                        checkEqOption (inRange (upperEven (i),   start, end, false), map.firstGreaterOrEquals (i));
                        checkEqOption (inRange (upperEven (i+1), start, end, false), map.firstGreaterThan (i));

                        checkEqOption (inRange (lowerEven (i),   start, end, true), map.lastSmallerOrEquals (i));
                        checkEqOption (inRange (lowerEven (i-1), start, end, true), map.lastSmallerThan (i));
                    }
                }
            }
        }
    }

    long lowerEven (long l) {
        return l%2 == 0 ? l : l-1;
    }

    long upperEven (long l) {
        return l%2 == 0 ? l : l+1;
    }

    private void checkEqOption (Long l, AOption<AMapEntry<Long, Integer>> e) {
        if (l == null) {
            assertFalse (e.isDefined ());
        }
        else {
            assertEquals (l, e.get ().getKey ());
        }
    }

    private Long inRange (long value, long min, long max, boolean undefinedBelow) {
        if (value < min) return  undefinedBelow ? null : min;
        if (value > max) return !undefinedBelow ? null : max;
        return value;
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
        assertFalse (EMPTY.rangeEE (0L, 1L).iterator ().hasNext ());
        assertFalse (EMPTY.rangeEI (0L, 1L).iterator ().hasNext ());
        assertFalse (EMPTY.rangeIE (0L, 1L).iterator ().hasNext ());
        assertFalse (EMPTY.rangeII (0L, 1L).iterator ().hasNext ());

        //TODO toString implementation for ranges

        for (long start=0; start<100; start+=2) {
            for (long end=start; end < 110; end+=2) {
                for (int asc=0; asc<3; asc++) {
                    final ASortedMap<Long, Integer> map = fill (start, end, asc);
//                    System.out.print (start + " to " + end + (asc > 0 ? " asc" : " desc") + ": ");
//                    dump (map);

                    for (long rangeStart=start-1; rangeStart<=end+1; rangeStart++) {
                        for (long rangeEnd=end-5; rangeEnd <= end+1; rangeEnd++) {
//                            System.out.print ("  [" + rangeStart + ", " + rangeEnd + "] -> ");
//                            dump (map.rangeII (rangeStart, rangeEnd));

                            checkEq (Math.max (start, rangeStart),   Math.min (end, rangeEnd),   map.rangeII (rangeStart, rangeEnd));
                            checkEq (Math.max (start, rangeStart),   Math.min (end, rangeEnd-1), map.rangeIE (rangeStart, rangeEnd));
                            checkEq (Math.max (start, rangeStart+1), Math.min (end, rangeEnd),   map.rangeEI (rangeStart, rangeEnd));
                            checkEq (Math.max (start, rangeStart+1), Math.min (end, rangeEnd-1), map.rangeEE (rangeStart, rangeEnd));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testFromTo() {
        assertFalse (EMPTY.fromI (1L).iterator ().hasNext ());
        assertFalse (EMPTY.fromE (1L).iterator ().hasNext ());
        assertFalse (EMPTY.toI (1L).iterator ().hasNext ());
        assertFalse (EMPTY.toE (1L).iterator ().hasNext ());

        for (long start=0; start<100; start+=2) {
            for (long end=start; end < 110; end+=2) {
                for (int asc=0; asc<3; asc++) {
                    final ASortedMap<Long, Integer> map = fill (start, end, asc);

                    for (long rangeStart=start-1; rangeStart<=end+1; rangeStart++) {
                        checkEq (Math.max (start, rangeStart),   end, map.fromI (rangeStart));
                        checkEq (Math.max (start, rangeStart+1), end, map.fromE (rangeStart));
                        checkEq (start, Math.min (end, rangeStart),   map.toI   (rangeStart));
                        checkEq (start, Math.min (end, rangeStart-1), map.toE   (rangeStart));
                    }
                }
            }
        }
    }
}
