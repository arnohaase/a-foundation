package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * @author arno
 */
public class AOptionTest {
    @Test
    public void testSome() throws Exception {
        final AOption<String> s = AOption.some("a");
        assertEquals("a", s.get());
        assertEquals("a", s.getOrElse("b"));
        assertEquals(true, s.isDefined());
        assertEquals(false, s.isEmpty());

        assertEquals(AOption.<String>none(), s.filter(new APredicate<String, Exception>() {
            @Override public boolean apply(String o) throws Exception {
                return false;
            }
        }));
        assertEquals(s, s.filter(new APredicate<String, Exception>() {
            @Override public boolean apply(String o) throws Exception {
                return true;
            }
        }));

        assertEquals(AOption.some(1), s.map(new AFunction1<String, Integer, Exception>() {
            @Override public Integer apply(String param) throws Exception {
                return param.length();
            }
        }));
    }

    @Test
    public void testNone() throws Exception {
        final AOption<String> s = AOption.none();

        try {
            s.get();
            fail("exception expected");
        } catch (NoSuchElementException e) {
            // expected
        }

        assertEquals("b", s.getOrElse("b"));
        assertEquals(false, s.isDefined());
        assertEquals(true, s.isEmpty());

        assertEquals(AOption.<String>none(), s.filter(new APredicate<String, Exception>() {
            @Override public boolean apply(String o) throws Exception {
                return false;
            }
        }));
        assertEquals(AOption.<String>none(), s.filter(new APredicate<String, Exception>() {
            @Override public boolean apply(String o) throws Exception {
                return true;
            }
        }));

        assertEquals(AOption.<Integer>none(), s.map(new AFunction1<String, Integer, Exception>() {
            @Override
            public Integer apply(String param) throws Exception {
                return param.length();
            }
        }));
    }

    @Test
    public void testFromNullable() {
        String s = "a";
        assertEquals(AOption.some("a"), AOption.fromNullable(s));

        if("".isEmpty())
            s = null;
        assertEquals(AOption.<String>none(), AOption.fromNullable(s));
    }
}
