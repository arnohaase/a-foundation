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
public class AListTest extends AbstractCollectionTest<AList<String>, AList<Integer>, AList<Iterable<String>>> {
    public AListTest() {
        super(false);
    }

    @Override public AList<String> create(String... elements) {
        return AList.create(elements);
    }

    @Override public AList<Integer> createInts(Integer... elements) {
        return AList.create(elements);
    }

    @SuppressWarnings("unchecked")
    @Override public AList<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        return AList.create((Collection<Iterable<String>>) elements);
    }

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
    public void testEquals2() {
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
}
