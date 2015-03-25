package com.ajjpj.abase.collection.immutable;

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
                new Object[] {AListMap.empty ()},
                new Object[] {ABTree.empty (new BTreeSpec (4, NATURAL_ORDER))},
                new Object[] {ABTree.empty (new BTreeSpec (8, NATURAL_ORDER))},
                new Object[] {ABTree.empty (new BTreeSpec (16, NATURAL_ORDER))},
                new Object[] {ARedBlackTree.empty (NATURAL_ORDER)}
        );
    }

    private final AMap EMPTY;

    public AMapTest (AMap<Integer, Integer> emptyMap) {
        this.EMPTY = emptyMap;
    }

    @Test
    public void testSimple() {
        final AMap<Integer, Integer> m0 = EMPTY;
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
    public void testKeysValues() {
        final AMap<Integer, Integer> map = EMPTY
                .updated(11, 1)
                .updated (22, 2)
                .updated (33, 3)
                .updated (44, 4);

        final Set<Integer> keys = map.keys();
        assertEquals(4, keys.size());
        assertTrue (keys.contains (11));
        assertTrue(keys.contains(22));
        assertTrue(keys.contains(33));
        assertTrue(keys.contains(44));

//        ((ARedBlackTree) map).dump();

        final Collection<Integer> values = map.values();
        assertEquals(4, values.size());
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertTrue(values.contains(3));
        assertTrue(values.contains(4));
    }

    @Test
    public void testEquals() {
        assertEquals(EMPTY, EMPTY);
        assertEquals(EMPTY.hashCode (), EMPTY.hashCode ());

        assertEquals(EMPTY.updated ("a", "a1"),
                EMPTY.updated ("a", "a1"));
        assertEquals(EMPTY.updated ("a", "a1").updated("b", "b1"),
                EMPTY.updated ("a", "a1").updated("b", "b1"));
        assertEquals(EMPTY.updated ("a", "a1").updated("b", "b1").updated("a", "a2").removed("b"),
                EMPTY.updated ("a", "a1").updated("b", "b1").updated("a", "a2").removed("b"));
        assertEquals(EMPTY.updated ("a", "a1").hashCode(),
                EMPTY.updated ("a", "a1").hashCode());
        assertEquals(EMPTY.updated ("a", "a1").updated("b", "b1").hashCode(),
                EMPTY.updated ("a", "a1").updated("b", "b1").hashCode());
        assertEquals(EMPTY.updated ("a", "a1").updated("b", "b1").updated("a", "a2").removed("b").hashCode(),
                EMPTY.updated ("a", "a1").updated("b", "b1").updated("a", "a2").removed("b").hashCode());

        assertEquals(EMPTY.updated ("a", "a").updated("b", "b"),
                EMPTY.updated ("b", "b").updated("a", "a"));
        assertEquals(EMPTY.updated ("a", "a").updated("b", "b").hashCode(),
                EMPTY.updated ("b", "b").updated("a", "a").hashCode());

        assertNotEquals(EMPTY.updated ("a", "1"),
                EMPTY.updated ("a", "2"));
        assertNotEquals(EMPTY.updated ("a", "1"),
                EMPTY.updated ("b", "1"));
        assertNotEquals(EMPTY.updated ("a", "1").hashCode(),
                EMPTY.updated ("a", "2").hashCode());
        assertNotEquals(EMPTY.updated ("a", "1").hashCode(),
                EMPTY.updated ("b", "1").hashCode());
    }

    @Test
    public void testShotgun() {
        final Random rand = new Random(12345);

        final Map<Integer, Integer> ju = new HashMap<>();
        AMap<Integer, Integer> a = EMPTY;

        final int numIters = EMPTY == AListMap.empty () ? 10_000 : 10_000_000;

        for(int i=0; i<numIters; i++) {
            final int key = rand.nextInt(100*1000);
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
        for(int k: ju.keySet()) {
            assertEquals (AOption.some (ju.get (k)), a.get (k));
        }
    }

    @Test
    public void testShotgunSmall() {
        final Random rand = new Random(12345);

        final Map<Integer, Integer> ju = new HashMap<>();
        AMap<Integer, Integer> a = EMPTY;

        final int numIters = EMPTY == AListMap.empty () ? 10_000 : 10_000_000;

        for(int i=0; i<numIters; i++) {
            final int key = rand.nextInt(100);
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
        for(int k: ju.keySet()) {
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

