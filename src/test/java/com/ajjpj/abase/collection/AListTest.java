package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.AList;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AListTest {
    @Test
    public void testNil() {
        assertEquals(0, AList.nil().size());
        assertTrue(AList.nil().asJavaUtilList().isEmpty());
    }

    @Test
    public void testCons() {
        AList<String> l0 = AList.nil();
        final AList<String> l1 = l0.cons("a");
        final AList<String> l2 = l0.cons("x").cons("y");

        assertEquals(1, l1.size());
        assertEquals(2, l2.size());

        assertEquals(Arrays.<String>asList(), l0.asJavaUtilList());
        assertEquals(Arrays.asList("a"),      l1.asJavaUtilList());
        assertEquals(Arrays.asList("y", "x"),  l2.asJavaUtilList());
    }

    @Test
    public void testFromJavaUtil() {
        final AList<String> l = AList.<String>nil().cons("a").cons("b").cons("c");
        final AList<String> l2 = AList.create(Arrays.asList("c", "b", "a"));
        final AList<String> l3 = AList.create((Iterable<String>) Arrays.asList("c", "b", "a"));

        assertEquals(l, l2);
        assertEquals(l, l3);

        assertEquals(AList.nil(), AList.create(Collections.emptyList()));
        assertEquals(AList.nil(), AList.create(Collections.emptySet()));
    }

    @Test
    public void testEquals() {
        assertEquals(AList.nil(), AList.nil());
        assertEquals(AList.nil().cons("a"), AList.nil().cons("a"));

        assertNotEquals(AList.nil(), AList.nil().cons("a"));
        assertNotEquals(AList.nil().cons("a"), AList.nil());
        assertNotEquals(AList.nil().cons("a").cons("b"), AList.nil().cons("a").cons("b").cons("c"));
        assertNotEquals(AList.nil().cons("a").cons("b"), AList.nil().cons("b").cons("a"));
        assertNotEquals(AList.nil().cons("a").cons("b").cons("c"), AList.nil().cons("a").cons("b"));
        assertNotEquals(AList.nil().cons("a").cons("b").cons("c"), AList.nil().cons("b").cons("c"));

        assertNotEquals(AList.nil(), null);
    }

    @Test
    public void testMkString() {
        assertEquals("%&",     AList.nil()                    .mkString("%", "--", "&"));
        assertEquals("%a&",    AList.nil().cons("a")          .mkString("%", "--", "&"));
        assertEquals("%b--a&", AList.nil().cons("a").cons("b").mkString("%", "--", "&"));

        assertEquals("",    AList.nil()                    .mkString("#"));
        assertEquals("a",   AList.nil().cons("a")          .mkString("#"));
        assertEquals("b#a", AList.nil().cons("a").cons("b").mkString("#"));
    }

    @Test
    public void testFind() {
        final APredicate<String, RuntimeException> fun = new APredicate<String, RuntimeException>() {
            @Override
            public boolean apply(String o) throws RuntimeException {
                return o.length() > 1;
            }
        };

        assertEquals(AOption.<String>none(), AList.<String>nil().find(fun));
        assertEquals(AOption.<String>none(), AList.<String>nil().cons("a").cons("b").cons("c").find(fun));
        assertEquals(AOption.some("ef"), AList.<String>nil().cons("a").cons("bc").cons("d").cons("ef").cons("g").find(fun));
    }

    @Test
    public void testFilter() {
        final APredicate<String, RuntimeException> fun = new APredicate<String, RuntimeException>() {
            @Override
            public boolean apply(String o) throws RuntimeException {
                return o.length() > 1;
            }
        };

        assertEquals(AList.<String>nil(), AList.<String>nil().filter(fun));
        assertEquals(AList.<String>nil(), AList.<String>nil().cons("a").cons("b").cons("c").filter(fun));
        assertEquals(AList.<String>nil().cons("bc").cons("ef"), AList.<String>nil().cons("a").cons("bc").cons("d").cons("ef").cons("g").filter(fun));
    }

    @Test
    public void testMap() {
        final AFunction1<Integer, String, RuntimeException> lenFunc = new AFunction1<Integer, String, RuntimeException>() {
            @Override
            public Integer apply(String param) throws RuntimeException {
                return param.length();
            }
        };

        assertEquals(AList.<Integer>nil(), AList.<String>nil().map(lenFunc));
        assertEquals(AList.<Integer>nil().cons(1).cons(3).cons(2), AList.<String>nil().cons("a").cons("bcd").cons("ef").map(lenFunc));
    }
}
