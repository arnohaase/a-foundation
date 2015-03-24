package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AbstractCollectionTest;
import com.ajjpj.abase.collection.immutable.AHashSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AHashSetTest extends AbstractCollectionTest<AHashSet<String>, AHashSet<Integer>, AHashSet<Iterable<String>>> {
    public AHashSetTest() {
        super(true);
    }

    @Override public AHashSet<String> create(String... elements) {
        return AHashSet.create(elements);
    }

    @Override public AHashSet<Integer> createInts(Integer... elements) {
        return AHashSet.create(elements);
    }

    @SuppressWarnings("unchecked")
    @Override public AHashSet<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        return (AHashSet<Iterable<String>>) AHashSet.create(elements);
    }

    @Test
    public void testEmpty() {
        assertEquals(0, AHashSet.empty().size());
        assertTrue(AHashSet.empty().asJavaUtilSet().isEmpty());
    }

    @Test
    public void testAddRemoveContains() {
        assertFalse(AHashSet.empty().contains("a"));
        assertTrue (AHashSet.empty().added("a").contains("a"));
        assertFalse(AHashSet.empty().added("a").contains("b"));
        assertTrue (AHashSet.empty().added("a").added("b").contains("b"));
        assertFalse(AHashSet.empty().added("a").added("b").contains("c"));
        assertFalse(AHashSet.empty().added("a").added("b").removed("a").contains("a"));
        assertTrue (AHashSet.empty().added("a").added("b").removed("a").contains("b"));
        assertFalse(AHashSet.empty().added("a").added("b").removed("a").contains("c"));
    }

    @Test
    public void testFromJavaUtil() {
        final AHashSet<String> l = AHashSet.<String>empty().added("a").added("b").added("c");
        final AHashSet<String> l2 = AHashSet.create(Arrays.asList("c", "b", "a"));

        assertEquals(l, l2);

        assertEquals(AHashSet.empty(), AHashSet.create(Collections.emptyList()));
    }

    @Test
    public void testEquals2() {
        assertEquals(AHashSet.empty(),
                     AHashSet.empty());
        assertEquals(AHashSet.empty().added("a"),
                     AHashSet.empty().added("a"));
        assertEquals(AHashSet.empty().added("a").added("b"),
                     AHashSet.empty().added("b").added("a"));

        assertNotEquals(AHashSet.empty(),
                        AHashSet.empty().added("a"));
        assertNotEquals(AHashSet.empty().added("a"),
                        AHashSet.empty());
        assertNotEquals(AHashSet.empty().added("a").added("b"),
                        AHashSet.empty().added("a").added("b").added("c"));
        assertNotEquals(AHashSet.empty().added("a").added("b").added("c"),
                        AHashSet.empty().added("a").added("b"));
        assertNotEquals(AHashSet.empty().added("a").added("b").added("c"),
                        AHashSet.empty().added("b").added("c"));

        assertNotEquals(AHashSet.empty(), null);
    }

    @Test
    public void testMkString() {
        assertEquals("%&",     AHashSet.empty()                    .mkString("%", "--", "&"));
        assertEquals("%a&",    AHashSet.empty().added("a")          .mkString("%", "--", "&"));

        final String s1 = AHashSet.empty().added("a").added("b").mkString("%", "--", "&");
        assertTrue("%b--a&".equals(s1) || "%a--b&".equals(s1));

        assertEquals("",    AHashSet.empty()                    .mkString("#"));
        assertEquals("a",   AHashSet.empty().added("a")          .mkString("#"));

        final String s2 = AHashSet.empty().added("a").added("b").mkString("#");
        assertTrue("b#a".equals(s2) || "a#b".equals(s2));
    }

    //TODO testEquality
}
