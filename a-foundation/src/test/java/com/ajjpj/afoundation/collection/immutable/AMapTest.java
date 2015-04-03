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
public class AMapTest {
    private static final Comparator<Comparable> NATURAL_ORDER = new Comparator<Comparable> () {
        @SuppressWarnings ("unchecked")
        @Override public int compare (Comparable o1, Comparable o2) {
            return o1.compareTo (o2);
        }
    };

    @Parameterized.Parameters
    public static Collection<Object> emptyMaps () {
        return Arrays.<Object>asList (
                new Object[] {AHashMap.empty ()},
                new Object[] {ALongHashMap.empty ()},
                new Object[] {AListMap.empty ()},
                new Object[] {ABTree.empty (new BTreeSpec (4, NATURAL_ORDER))},
                new Object[] {ABTree.empty (new BTreeSpec (8, NATURAL_ORDER))},
                new Object[] {ABTree.empty (new BTreeSpec (16, NATURAL_ORDER))},
                new Object[] {ARedBlackTree.empty (NATURAL_ORDER)},
                new Object[] {ALongRedBlackTree.empty ()}
        );
    }

    private final AMap EMPTY;

    public AMapTest (AMap<Integer, Integer> emptyMap) {
        this.EMPTY = emptyMap;
    }

    @Test
    public void testSimple() {
        final AMap<Long, Integer> m0 = EMPTY;
        assertEquals(0, m0.size());
        assertEquals(true, m0.isEmpty());
        assertEquals(false, m0.nonEmpty());

        assertEquals(false, m0.containsKey(1L));
        assertEquals(false, m0.get(1L).isDefined());
        assertEquals(false, m0.containsKey(2L));

        final AMap<Long, Integer> m1 = m0.updated(1L, 1);
        assertEquals(1, m1.size());
        assertEquals(false, m1.isEmpty());
        assertEquals(true, m1.nonEmpty());

        assertEquals(true, m1.containsKey(1L));
        assertEquals(Integer.valueOf(1), m1.get(1L).get());
        assertEquals(false, m1.containsKey(2L));

        final AMap<Long, Integer> m2 = m1.updated(2L, 2);
        assertEquals(2, m2.size());
        assertEquals(false, m2.isEmpty());
        assertEquals(true, m2.nonEmpty());

        assertEquals(true, m2.containsKey(1L));
        assertEquals(Integer.valueOf(1), m2.get(1L).get());
        assertEquals(true, m2.containsKey(2L));
        assertEquals(Integer.valueOf(2), m2.get(2L).get());

        final AMap<Long, Integer> m3 = m2.removed(1L);
        assertEquals(1, m3.size());
        assertEquals(false, m3.isEmpty());
        assertEquals(true, m3.nonEmpty());

        assertEquals(false, m3.containsKey(1L));
        assertEquals(true, m3.containsKey(2L));
        assertEquals(Integer.valueOf(2), m3.get(2L).get());

        final AMap<Long, Integer> m4 = m3.removed(2L);
        assertEquals(0, m4.size());
        assertEquals(true, m4.isEmpty());
        assertEquals(false, m4.nonEmpty());

        assertEquals(false, m4.containsKey(1L));
        assertEquals(false, m4.get(1L).isDefined());
        assertEquals(false, m4.containsKey(2L));
    }

    @Test
    public void testKeysValues() {
        final AMap<Long, Integer> map = EMPTY
                .updated (11L, 1)
                .updated (22L, 2)
                .updated (33L, 3)
                .updated (44L, 4);

        final Set<Long> keys = map.keys();
        assertEquals(4, keys.size());
        assertTrue (keys.contains (11L));
        assertTrue (keys.contains(22L));
        assertTrue (keys.contains(33L));
        assertTrue (keys.contains(44L));

//        ((ARedBlackTree) map).dump();

        final Collection<Integer> values = map.values();
        assertEquals (4, values.size());
        assertTrue (values.contains(1));
        assertTrue (values.contains(2));
        assertTrue (values.contains(3));
        assertTrue (values.contains(4));
    }

    @Test
    public void testEquals() {
        assertEquals(EMPTY, EMPTY);
        assertEquals(EMPTY.hashCode (), EMPTY.hashCode ());

        assertEquals(
                EMPTY.updated (1L, "a1"),
                EMPTY.updated (1L, "a1"));
        assertEquals(
                EMPTY.updated (1L, "a1").updated(2L, "b1"),
                EMPTY.updated (1L, "a1").updated(2L, "b1"));
        assertEquals(
                EMPTY.updated (1L, "a1").updated(2L, "b1").updated(1L, "a2").removed(2L),
                EMPTY.updated (1L, "a1").updated(2L, "b1").updated(1L, "a2").removed(2L));
        assertEquals(
                EMPTY.updated (1L, "a1").hashCode(),
                EMPTY.updated (1L, "a1").hashCode());
        assertEquals(
                EMPTY.updated (1L, "a1").updated(2L, "b1").hashCode(),
                EMPTY.updated (1L, "a1").updated(2L, "b1").hashCode());
        assertEquals(
                EMPTY.updated (1L, "a1").updated(2L, "b1").updated(1L, "a2").removed(2L).hashCode(),
                EMPTY.updated (1L, "a1").updated(2L, "b1").updated(1L, "a2").removed(2L).hashCode());

        assertEquals(
                EMPTY.updated (1L, "a").updated(2L, "b"),
                EMPTY.updated (2L, "b").updated(1L, "a"));
        assertEquals(
                EMPTY.updated (1L, "a").updated(2L, "b").hashCode(),
                EMPTY.updated (2L, "b").updated(1L, "a").hashCode());

        assertNotEquals(
                EMPTY.updated (1L, "1"),
                EMPTY.updated (1L, "2"));
        assertNotEquals(
                EMPTY.updated (1L, "1"),
                EMPTY.updated (2L, "1"));
        assertNotEquals(
                EMPTY.updated (1L, "1").hashCode(),
                EMPTY.updated (1L, "2").hashCode());
        assertNotEquals(
                EMPTY.updated (1L, "1").hashCode(),
                EMPTY.updated (2L, "1").hashCode());
    }

    @Test
    public void testShotgun() {
        final Random rand = new Random(12345);

        final Map<Long, Integer> ju = new HashMap<>();
        AMap<Long, Integer> a = EMPTY;

        final int numIters =
                (EMPTY == AListMap.empty ()) ?
                        10_000 :
                        10_000_000;

        for(int i=0; i<numIters; i++) {
            final long key = rand.nextInt(100*1000);
            final boolean add = rand.nextBoolean();

            if(add) {
                final int value = rand.nextInt ();
                ju.put (key, value);

                a = a.updated(key, value);
            }
            else {
                ju.remove (key);
                a = a.removed(key);
            }
        }

        assertEquals (ju.size (), a.size ());
        for(long k: ju.keySet()) {
            assertEquals (AOption.some (ju.get (k)), a.get (k));
        }
    }

    @Test
    public void testShotgunSmall() {
        final Random rand = new Random(12345);

        final Map<Long, Integer> ju = new HashMap<>();
        AMap<Long, Integer> a = EMPTY;

        final int numIters =
                (EMPTY == AListMap.empty ()) ?
                        10_000 :
                        10_000_000;

        for(int i=0; i<numIters; i++) {
            final long key = rand.nextInt(100);
            final boolean add = rand.nextBoolean();

            if(add) {
                final int value = rand.nextInt ();
                ju.put (key, value);

                a = a.updated(key, value);
            }
            else {
                ju.remove (key);
                a = a.removed(key);
            }

//            assertEquals (ju.size (), a.size ());
//            for(long k: ju.keySet()) {
//                assertEquals (AOption.some (ju.get (k)), a.get (k));
//            }
        }

        assertEquals (ju.size (), a.size ());
        for(long k: ju.keySet()) {
            assertEquals (AOption.some (ju.get (k)), a.get (k));
        }
    }

//    void validate (RedBlackTree.Tree tree) {
//        if (tree instanceof RedBlackTree.RedTree) {
//            if (tree.left() instanceof RedBlackTree.RedTree) throw new IllegalStateException ();
//            if (tree.right () instanceof RedBlackTree.RedTree) throw new IllegalStateException ();
//        }
//
//        countBlackDepth (tree);
//    }
//
//    int countBlackDepth (RedBlackTree.Tree tree) {
//        if (tree == null) return 1;
//
//        final int own = tree instanceof RedBlackTree.BlackTree ? 1 : 0;
//
//        final int left  = countBlackDepth (tree.left());
//        final int right = countBlackDepth (tree.right ());
//
//        if (left != right) throw new IllegalStateException ();
//        return left + own;
//    }
//
//    String indent (int level) {
//        return "                                                                                                                                              ".substring (0, 4*level);
//    }
//
//    void dump (RedBlackTree.Tree tree, int indent) {
//        if (tree == null) {
//            System.out.println (indent (indent) + "<>");
//            return;
//        }
//        dump (tree.left (), indent+1);
//        System.out.println (indent (indent) + (tree instanceof RedBlackTree.BlackTree ? "+ " : "* ") + tree.key ());
//        dump (tree.right (), indent+1);
//    }
//
//    void compare (ARedBlackTree.Tree<Integer, Integer> a, RedBlackTree.Tree<Integer, Integer> b) {
//        if (a == null && b == null) {
//            return;
//        }
//
//        if (a == null || b == null) throw new IllegalStateException ();
//
//        final boolean aIsBlack = a instanceof ARedBlackTree.BlackTree;
//        final boolean bIsBlack = b instanceof RedBlackTree.BlackTree;
//
//        if (aIsBlack != bIsBlack) throw new IllegalStateException ();
//        if (! Objects.equals (a.key, b.key ())) throw new IllegalStateException ();
//
//        compare (a.left, b.left ());
//        compare (a.right, b.right ());
//    }

}

