package com.ajjpj.afoundation.collection.immutable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;


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

        for (int start=0; start<100; start ++) {
            for (int end=start; end < 110; end ++) {
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
            for (long i=start; i<=end; i++) {
                map = map.updated (i, (int) i);
            }
        }
        else {
            for (long i=end; i>=start; i--) {
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

        for (int start=0; start<100; start++) {
            for (int end=start; end < 110; end++) {
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

    @Test
    public void testRange() {
        fail ("todo");
    }

    @Test
    public void testFromTo() {
        fail ("todo");
    }
}
