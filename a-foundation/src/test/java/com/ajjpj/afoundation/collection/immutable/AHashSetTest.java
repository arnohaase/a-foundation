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
public class AHashSetTest extends AbstractCollectionTest<AHashSet1<String>, AHashSet1<Integer>, AHashSet1<Iterable<String>>> {
    public AHashSetTest() {
        super(true);
    }

    @Override public AHashSet1<String> create(String... elements) {
        return AHashSet1.create (elements);
    }

    @Override public AHashSet1<Integer> createInts(Integer... elements) {
        return AHashSet1.create (elements);
    }

    @SuppressWarnings("unchecked")
    @Override public AHashSet1<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        return (AHashSet1<Iterable<String>>) AHashSet1.create (elements);
    }

    @Test
    public void testEmpty() {
        assertEquals(0, AHashSet1.empty ().size());
        assertTrue(AHashSet1.empty ().asJavaUtilSet().isEmpty());
    }

    @Test
    public void testAddRemoveContains() {
        assertFalse(AHashSet1.empty ().contains("a"));
        assertTrue (AHashSet1.empty ().added("a").contains("a"));
        assertFalse(AHashSet1.empty ().added("a").contains("b"));
        assertTrue (AHashSet1.empty ().added("a").added("b").contains("b"));
        assertFalse(AHashSet1.empty ().added("a").added("b").contains("c"));
        assertFalse(AHashSet1.empty ().added("a").added("b").removed("a").contains("a"));
        assertTrue (AHashSet1.empty ().added("a").added("b").removed("a").contains("b"));
        assertFalse(AHashSet1.empty ().added("a").added("b").removed("a").contains("c"));
    }

    @Test
    public void testFromJavaUtil() {
        final AHashSet1<String> l = AHashSet1.<String>empty ().added("a").added("b").added("c");
        final AHashSet1<String> l2 = AHashSet1.create (Arrays.asList ("c", "b", "a"));

        assertEquals(l, l2);

        assertEquals(AHashSet1.empty (), AHashSet1.create (Collections.emptyList ()));
    }

    @Test
    public void testEquals2() {
        assertEquals(AHashSet1.empty (),
                     AHashSet1.empty ());
        assertEquals(AHashSet1.empty ().added("a"),
                     AHashSet1.empty ().added("a"));
        assertEquals(AHashSet1.empty ().added("a").added("b"),
                     AHashSet1.empty ().added("b").added("a"));

        assertNotEquals(AHashSet1.empty (),
                        AHashSet1.empty ().added("a"));
        assertNotEquals(AHashSet1.empty ().added("a"),
                        AHashSet1.empty ());
        assertNotEquals(AHashSet1.empty ().added("a").added("b"),
                        AHashSet1.empty ().added("a").added("b").added("c"));
        assertNotEquals(AHashSet1.empty ().added("a").added("b").added("c"),
                        AHashSet1.empty ().added("a").added("b"));
        assertNotEquals(AHashSet1.empty ().added("a").added("b").added("c"),
                        AHashSet1.empty ().added("b").added("c"));

        assertNotEquals(AHashSet1.empty (), null);
    }

    @Test
    public void testMkString() {
        assertEquals("%&",     AHashSet1.empty ()                    .mkString("%", "--", "&"));
        assertEquals("%a&",    AHashSet1.empty ().added("a")          .mkString("%", "--", "&"));

        final String s1 = AHashSet1.empty ().added("a").added("b").mkString("%", "--", "&");
        assertTrue("%b--a&".equals(s1) || "%a--b&".equals(s1));

        assertEquals("",    AHashSet1.empty ()                    .mkString("#"));
        assertEquals("a",   AHashSet1.empty ().added("a")          .mkString("#"));

        final String s2 = AHashSet1.empty ().added("a").added("b").mkString("#");
        assertTrue("b#a".equals(s2) || "a#b".equals(s2));
    }

    //TODO testEquality
}
