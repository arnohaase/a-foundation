package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AbstractCollectionTest;
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
        return AHashSet.create (elements);
    }

    @Override public AHashSet<Integer> createInts(Integer... elements) {
        return AHashSet.create (elements);
    }

    @SuppressWarnings("unchecked")
    @Override public AHashSet<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        return (AHashSet<Iterable<String>>) AHashSet.create (elements);
    }

    @Override protected <T> void assertEqualColl (ACollection<T> expected, ACollection<T> coll) {
        assertEquals (expected, coll.toSet ());
    }
    @Test
    public void testEmpty() {
        assertEquals(0, AHashSet.empty ().size());
        assertTrue(AHashSet.empty ().asJavaUtilSet().isEmpty());
    }

    @Test
    public void testAddRemoveContains() {
        assertFalse(AHashSet.empty ().contains("a"));
        assertTrue (AHashSet.empty ().with("a").contains("a"));
        assertFalse(AHashSet.empty ().with("a").contains("b"));
        assertTrue (AHashSet.empty ().with("a").with("b").contains("b"));
        assertFalse(AHashSet.empty ().with("a").with("b").contains("c"));
        assertFalse(AHashSet.empty ().with("a").with("b").without("a").contains("a"));
        assertTrue (AHashSet.empty ().with("a").with("b").without("a").contains("b"));
        assertFalse(AHashSet.empty ().with("a").with("b").without("a").contains("c"));
    }

    @Test
    public void testFromJavaUtil() {
        final AHashSet<String> l = AHashSet.<String>empty ().with("a").with("b").with("c");
        final AHashSet<String> l2 = AHashSet.create (Arrays.asList ("c", "b", "a"));

        assertEquals(l, l2);

        assertEquals(AHashSet.empty (), AHashSet.create (Collections.emptyList ()));
    }

    @Test
    public void testEquals2() {
        assertEquals(AHashSet.empty (),
                     AHashSet.empty ());
        assertEquals(AHashSet.empty ().with("a"),
                     AHashSet.empty ().with("a"));
        assertEquals(AHashSet.empty ().with("a").with("b"),
                     AHashSet.empty ().with("b").with("a"));

        assertNotEquals(AHashSet.empty (),
                        AHashSet.empty ().with("a"));
        assertNotEquals(AHashSet.empty ().with("a"),
                        AHashSet.empty ());
        assertNotEquals(AHashSet.empty ().with("a").with("b"),
                        AHashSet.empty ().with("a").with("b").with("c"));
        assertNotEquals(AHashSet.empty ().with("a").with("b").with("c"),
                        AHashSet.empty ().with("a").with("b"));
        assertNotEquals(AHashSet.empty ().with("a").with("b").with("c"),
                        AHashSet.empty ().with("b").with("c"));

        assertNotEquals(AHashSet.empty (), null);
    }

    @Test
    public void testMkString() {
        assertEquals("%&",     AHashSet.empty ()                    .mkString("%", "--", "&"));
        assertEquals("%a&",    AHashSet.empty ().with("a")          .mkString("%", "--", "&"));

        final String s1 = AHashSet.empty ().with("a").with("b").mkString("%", "--", "&");
        assertTrue("%b--a&".equals(s1) || "%a--b&".equals(s1));

        assertEquals("",    AHashSet.empty ()                    .mkString("#"));
        assertEquals("a",   AHashSet.empty ().with("a")          .mkString("#"));

        final String s2 = AHashSet.empty ().with("a").with("b").mkString("#");
        assertTrue("b#a".equals(s2) || "a#b".equals(s2));
    }

    //TODO testEquality
}
