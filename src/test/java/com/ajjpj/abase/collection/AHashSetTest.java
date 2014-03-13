package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.AHashSet;
import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AHashSetTest {
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
    public void testEquals() {
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

    @Test
    public void testFind() {
        final APredicate<String, RuntimeException> fun = new APredicate<String, RuntimeException>() {
            @Override
            public boolean apply(String o) throws RuntimeException {
                return o.length() > 1;
            }
        };

        assertEquals(AOption.<String>none(), AHashSet.<String>empty().find(fun));
        assertEquals(AOption.<String>none(), AHashSet.<String>empty().added("a").added("b").added("c").find(fun));
        assertEquals(AOption.some("bc"), AHashSet.<String>empty().added("a").added("bc").added("d").find(fun));
    }

    @Test
    public void testFilter() {
        final APredicate<String, RuntimeException> fun = new APredicate<String, RuntimeException>() {
            @Override
            public boolean apply(String o) throws RuntimeException {
                return o.length() > 1;
            }
        };

        assertEquals(AHashSet.<String>empty(), AHashSet.<String>empty().filter(fun));
        assertEquals(AHashSet.<String>empty(), AHashSet.<String>empty().added("a").added("b").added("c").filter(fun));
        assertEquals(AHashSet.<String>empty().added("bc").added("ef"), AHashSet.<String>empty().added("a").added("bc").added("d").added("ef").added("g").filter(fun));
    }

    @Test
    public void testMap() {
        final AFunction1<Integer, String, RuntimeException> lenFunc = new AFunction1<Integer, String, RuntimeException>() {
            @Override
            public Integer apply(String param) throws RuntimeException {
                return param.length();
            }
        };

        assertEquals(AHashSet.<Integer>empty(), AHashSet.<String>empty().map(lenFunc));
        assertEquals(AHashSet.<Integer>empty().added(1).added(3).added(2), AHashSet.<String>empty().added("a").added("bcd").added("ef").map(lenFunc));
    }

    //TODO testEquality
}
