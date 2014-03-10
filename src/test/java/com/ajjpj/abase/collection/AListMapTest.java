package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.AListMap;
import com.ajjpj.abase.collection.AMap;
import com.ajjpj.abase.collection.AOption;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * @author arno
 */
public class AListMapTest {
    @Test
    public void testSimple() {
        final AMap<Integer, Integer> m0 = AListMap.empty();
        assertEquals(0, m0.size());
        assertEquals(true, m0.isEmpty());
        assertEquals(false, m0.nonEmpty());

        assertEquals(false, m0.containsKey(1));
        assertEquals(false, m0.get(1).isDefined());
        assertEquals(false, m0.containsKey(2));

        final AMap<Integer, Integer> m1 = m0.updated(1, 1);
        assertEquals(1, m1.size());
        assertEquals(false, m1.isEmpty());
        assertEquals(true, m1.nonEmpty());

        assertEquals(true, m1.containsKey(1));
        assertEquals(Integer.valueOf(1), m1.get(1).get());
        assertEquals(false, m1.containsKey(2));

        final AMap<Integer, Integer> m2 = m1.updated(2, 2);
        assertEquals(2, m2.size());
        assertEquals(false, m2.isEmpty());
        assertEquals(true, m2.nonEmpty());

        assertEquals(true, m2.containsKey(1));
        assertEquals(Integer.valueOf(1), m2.get(1).get());
        assertEquals(true, m2.containsKey(2));
        assertEquals(Integer.valueOf(2), m2.get(2).get());

        final AMap<Integer, Integer> m3 = m2.removed(1);
        assertEquals(1, m3.size());
        assertEquals(false, m3.isEmpty());
        assertEquals(true, m3.nonEmpty());

        assertEquals(false, m3.containsKey(1));
        assertEquals(true, m3.containsKey(2));
        assertEquals(Integer.valueOf(2), m3.get(2).get());

        final AMap<Integer, Integer> m4 = m3.removed(2);
        assertEquals(0, m4.size());
        assertEquals(true, m4.isEmpty());
        assertEquals(false, m4.nonEmpty());

        assertEquals(false, m4.containsKey(1));
        assertEquals(false, m4.get(1).isDefined());
        assertEquals(false, m4.containsKey(2));
    }

    @Test
    public void testEquals() {
        assertEquals(AListMap.empty(), AListMap.empty());
        assertEquals(AListMap.empty().hashCode(), AListMap.empty().hashCode());

        assertEquals(AListMap.empty().updated("a", "a1"),
                     AListMap.empty().updated("a", "a1"));
        assertEquals(AListMap.empty().updated("a", "a1").updated("b", "b1"),
                     AListMap.empty().updated("a", "a1").updated("b", "b1"));
        assertEquals(AListMap.empty().updated("a", "a1").updated("b", "b1").updated("a", "a2").removed("b"),
                     AListMap.empty().updated("a", "a1").updated("b", "b1").updated("a", "a2").removed("b"));
        assertEquals(AListMap.empty().updated("a", "a1").hashCode(),
                     AListMap.empty().updated("a", "a1").hashCode());
        assertEquals(AListMap.empty().updated("a", "a1").updated("b", "b1").hashCode(),
                     AListMap.empty().updated("a", "a1").updated("b", "b1").hashCode());
        assertEquals(AListMap.empty().updated("a", "a1").updated("b", "b1").updated("a", "a2").removed("b").hashCode(),
                     AListMap.empty().updated("a", "a1").updated("b", "b1").updated("a", "a2").removed("b").hashCode());

        assertEquals(AListMap.empty().updated("a", "a").updated("b", "b"),
                     AListMap.empty().updated("b", "b").updated("a", "a"));
        assertEquals(AListMap.empty().updated("a", "a").updated("b", "b").hashCode(),
                     AListMap.empty().updated("b", "b").updated("a", "a").hashCode());

        assertNotEquals(AListMap.empty().updated("a", "1"),
                        AListMap.empty().updated("a", "2"));
        assertNotEquals(AListMap.empty().updated("a", "1"),
                        AListMap.empty().updated("b", "1"));
        assertNotEquals(AListMap.empty().updated("a", "1").hashCode(),
                        AListMap.empty().updated("a", "2").hashCode());
        assertNotEquals(AListMap.empty().updated("a", "1").hashCode(),
                        AListMap.empty().updated("b", "1").hashCode());
    }

    //TODO equality

    @Test
    public void testShotgun() {
        final Random rand = new Random(12345);

        final Map<Integer, Integer> ju = new HashMap<Integer, Integer>();
        AMap<Integer, Integer> a = AListMap.empty();

        for(int i=0; i<100; i++) {
            final int key = rand.nextInt(10);
            final boolean add = rand.nextBoolean();

//            System.out.println(i + ": " + key + " / " + add);

            if(add) {
                ju.put(key, key);
                a = a.updated(key, key);
            }
            else {
                ju.remove(key);
                a = a.removed(key);
            }
            assertEquals(ju.size(), a.size());
        }

        for(int key: ju.keySet()) {
            assertEquals(AOption.some(key), a.get(key));
        }
    }
}
