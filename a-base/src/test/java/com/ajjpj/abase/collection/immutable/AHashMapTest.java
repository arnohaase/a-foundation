package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.collection.immutable.AHashMap;
import com.ajjpj.abase.collection.immutable.AMap;
import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.function.AFunction1NoThrow;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class AHashMapTest {

    final int size = 1000;
    final int numIter = 100*1000;

    private Map<Integer, Integer> createJu() {
        final Random rand = new Random(12345);
        final Map<Integer, Integer> result = new HashMap<>();

        for(int i=0; i<numIter; i++) {
            final int key = rand.nextInt(size);
            final boolean add = rand.nextBoolean();

            if(add)
                result.put(key, key);
            else
                result.remove(key);
        }
        return result;
    }
    private Map<Integer, Integer> createConc() {
        final Random rand = new Random(12345);
        final Map<Integer, Integer> result = new ConcurrentHashMap<>();

        for(int i=0; i<numIter; i++) {
            final int key = rand.nextInt(size);
            final boolean add = rand.nextBoolean();

            if(add)
                result.put(key, key);
            else
                result.remove(key);
        }
        return result;
    }
    private AMap<Integer, Integer> createA() {
        final Random rand = new Random(12345);
        AMap<Integer, Integer> result = AHashMap.empty();

        for(int i=0; i<numIter; i++) {
            final int key = rand.nextInt(size);
            final boolean add = rand.nextBoolean();

            if(add)
                result = result.updated(key, key);
            else
                result = result.removed(key);
        }
        return result;
    }

    private void doReadJu(Map<Integer, Integer> m) {
        for (int i=0; i<numIter; i++) {
            for(int j=0; j<size; j++) {
                m.get(j);
            }
        }
    }
    private void doReadA(AMap<Integer, Integer> m) {
        for (int i=0; i<numIter; i++) {
            for(int j=0; j<size; j++) {
                m.get(j);
            }
        }
    }

//TODO extract to a performance test suite
    @Test
    @Ignore
    public void testReadWritePerf() {
        doReadJu(createJu());
        doReadJu(createConc());
        doReadA(createA());

        final long t0 = System.currentTimeMillis();
        final Map<Integer, Integer> ju = createJu();
        final long t1 = System.currentTimeMillis();
        final Map<Integer, Integer> conc = createConc();
        final long t2 = System.currentTimeMillis();
        final AMap<Integer, Integer> a = createA();
        final long t3 = System.currentTimeMillis();
        doReadJu(ju);
        final long t4 = System.currentTimeMillis();
        doReadJu(conc);
        final long t5 = System.currentTimeMillis();
        doReadA(a);
        final long t6 = System.currentTimeMillis();

        System.out.println((t1 - t0));
        System.out.println((t2 - t1));
        System.out.println((t3 - t2));
        System.out.println((t4 - t3));
        System.out.println((t5 - t4));
        System.out.println((t6 - t5));
    }

    @Test
    public void testWithDefaultValue() {
        AMap<Integer, String> map = AHashMap.empty();
        map = map.withDefaultValue("a");

        assertEquals("a", map.getRequired(1));
        assertEquals("a", map.getRequired(2));
        assertEquals(AOption.some("a"), map.get(3));
        assertEquals(AOption.some("a"), map.get(4));

        assertTrue(map.keys().isEmpty());
        assertTrue(map.values().isEmpty());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("a"));

        assertEquals(0, map.size());

        map = map.updated(1, "x");

        assertEquals(1, map.size());
        assertEquals(1, map.keys().size());
        assertTrue(map.keys().contains(1));
        assertEquals(1, map.values().size());
        assertTrue(map.values().contains("x"));

        assertEquals("x", map.getRequired(1));
        assertEquals("a", map.getRequired(2));
        assertEquals(AOption.some("x"), map.get(1));
        assertEquals(AOption.some("a"), map.get(2));

        map = map.removed(1);

        assertEquals("a", map.getRequired(1));
        assertEquals("a", map.getRequired(2));
        assertEquals(AOption.some("a"), map.get(3));
        assertEquals(AOption.some("a"), map.get(4));

        assertTrue(map.keys().isEmpty());
        assertTrue(map.values().isEmpty());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("a"));

        assertEquals(0, map.size());
    }

    @Test
    public void testWithDefault() {
        AMap<Integer, String> map = AHashMap.empty();
        map = map.withDefault(new AFunction1NoThrow<Integer, String>() {
            @Override public String apply(Integer param) {
                return String.valueOf(param);
            }
        });

        assertEquals("1", map.getRequired(1));
        assertEquals("2", map.getRequired(2));
        assertEquals(AOption.some("3"), map.get(3));
        assertEquals(AOption.some("4"), map.get(4));

        assertTrue(map.keys().isEmpty());
        assertTrue(map.values().isEmpty());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("1"));

        assertEquals(0, map.size());

        map = map.updated(1, "x");

        assertEquals(1, map.size());
        assertEquals(1, map.keys().size());
        assertTrue(map.keys().contains(1));
        assertEquals(1, map.values().size());
        assertTrue(map.values().contains("x"));

        assertEquals("x", map.getRequired(1));
        assertEquals("2", map.getRequired(2));
        assertEquals(AOption.some("x"), map.get(1));
        assertEquals(AOption.some("2"), map.get(2));

        map = map.removed(1);

        assertEquals("1", map.getRequired(1));
        assertEquals("2", map.getRequired(2));
        assertEquals(AOption.some("3"), map.get(3));
        assertEquals(AOption.some("4"), map.get(4));

        assertTrue(map.keys().isEmpty());
        assertTrue(map.values().isEmpty());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("x"));

        assertEquals(0, map.size());
    }

    @Test
    public void testHashCollision() {
        final Long withHash1 = 0x100000000L;

        assertEquals(1, withHash1.hashCode());
        assertEquals(1, Long.valueOf(1).hashCode());

        AMap<Long, Long> map = AHashMap.empty();
        map = map.updated(1L, 1L);
        map = map.updated(withHash1, withHash1);

        assertEquals(2, map.size());

        assertEquals(AOption.some(1L), map.get(1L));
        assertEquals(AOption.some(withHash1), map.get(withHash1));
    }

    @Test
    public void testCustomEquality() {
        final AEquality equality = new AEquality() {
            @Override public boolean equals(Object o1, Object o2) {
                return ((Integer) o1)%2 == ((Integer) o2)%2;
            }

            @Override public int hashCode(Object o) {
                return 0;
            }
        };

        AMap<Integer, Integer> map = AHashMap.empty(equality);
        map = map.updated(1, 1);
        map = map.updated(2, 2);
        map = map.updated(3, 3);

        assertEquals(2, map.size());
        assertEquals(AOption.some(3), map.get(1));
        assertEquals(AOption.some(2), map.get(2));
        assertEquals(AOption.some(3), map.get(3));
    }

    @SuppressWarnings ("RedundantStringConstructorCall")
    @Test
    public void testEqualityIdentity() {
        AMap<String, String> map = AHashMap.empty (AEquality.IDENTITY);

        final String key1 = new String("key");
        final String key2 = new String("key");
        final String key3 = new String("key");

        assertEquals (key1, key2);
        assertNotSame (key1, key2);

        map = map.updated (key1, "1");
        map = map.updated (key2, "2");
        map = map.updated (key3, "3");

        assertEquals ("1", map.getRequired (key1));
        assertEquals ("2", map.getRequired (key2));
        assertEquals ("3", map.getRequired (key3));

        map = map.removed (key2);
        assertEquals ("1", map.getRequired (key1));
        assertEquals (AOption.<String>none (), map.get (key2));
        assertEquals ("3", map.getRequired (key3));

        map = map.removed (key1);
        assertEquals (AOption.<String>none (), map.get (key1));
        assertEquals (AOption.<String>none (), map.get (key2));
        assertEquals ("3", map.getRequired (key3));
    }
}
