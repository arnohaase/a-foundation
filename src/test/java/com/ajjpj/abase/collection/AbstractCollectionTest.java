package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.*;
import com.ajjpj.abase.function.AFunction1NoThrow;
import com.ajjpj.abase.function.APredicateNoThrow;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public abstract class AbstractCollectionTest<C extends ACollection<String, C>, CI extends ACollection<Integer, CI>, CC extends ACollection<Iterable<String>, CC>> {
    private final boolean removesDuplicates;

    protected AbstractCollectionTest(boolean removesDuplicates) {
        this.removesDuplicates = removesDuplicates;
    }

    public abstract C create(String... elements);
    public abstract CI createInts(Integer... elements);
    public abstract CC createIter(Collection<? extends Iterable<String>> elements);


    @Test
    public void testEquals() {
        assertEquals(create(), create());
        assertEquals(create("a", "b"), create("a", "b"));

        assertNotEquals(create("a"), create("b"));
        assertNotEquals(create("a", "b"), create("b"));
        assertNotEquals(create("a"), create("a", "b"));
    }

    @Test
    public void testMkStringNoArgs() {
        assertEquals("",        create().             mkString());
        assertEquals("a",       create("a").          mkString());
        assertEquals("a, b, c", create("a", "b", "c").mkString());
    }

    @Test
    public void testMkStringSeparator() {
        assertEquals("",      create().             mkString("#"));
        assertEquals("a",     create("a").          mkString("#"));
        assertEquals("a#b#c", create("a", "b", "c").mkString("#"));

        assertEquals("",        create().             mkString("?!"));
        assertEquals("a",       create("a").          mkString("?!"));
        assertEquals("a?!b?!c", create("a", "b", "c").mkString("?!"));
    }

    @Test
    public void testMkStringFull() {
        assertEquals("[]",      create().             mkString("[", "#", "]"));
        assertEquals("[a]",     create("a").          mkString("[", "#", "]"));
        assertEquals("[a#b#c]", create("a", "b", "c").mkString("[", "#", "]"));

        assertEquals("<<>>",        create().             mkString("<<", "?!", ">>"));
        assertEquals("<<a>>",       create("a").          mkString("<<", "?!", ">>"));
        assertEquals("<<a?!b?!c>>", create("a", "b", "c").mkString("<<", "?!", ">>"));
    }

    @Test
    public void testFind() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals (AOption.<String>none(), create().find(len1));
        assertEquals (AOption.<String>none(), create("", "ab", "cde").find(len1));

        final AOption<String> found = create("", "abc", "d", "ef", "g").find(len1);
        assertTrue(found.isDefined());
        assertTrue(found.get().equals("d") || found.get().equals("g"));
    }

    @Test
    public void testForAll() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(true, create().forAll(len1));
        assertEquals(true, create("a").forAll(len1));
        assertEquals(true, create("a", "b", "c").forAll(len1));
        assertEquals(false, create("a", "b", "cd", "e").forAll(len1));
    }

    @Test
    public void testExists() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(false, create().exists(len1));
        assertEquals(true,  create("a").exists(len1));
        assertEquals(false, create("ab").exists(len1));
        assertEquals(true,  create("ab", "c", "def").exists(len1));
    }

    @Test
    public void testMap() {
        final AFunction1NoThrow<Integer, String> len = new AFunction1NoThrow<Integer, String>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        assertEquals(createInts(), create().map(len));
        assertEquals(createInts(1), create("a").map(len));
        assertEquals(createInts(2, 1, 3), create("ab", "c", "def").map(len));
    }

    @Test
    public void testFlatMapTokens() {
        final AFunction1NoThrow<List<String>, String> tokens = new AFunction1NoThrow<List<String>, String>() {
            @Override public List<String> apply(String param) {
                return Arrays.asList(param.split(" "));
            }
        };

        assertEquals(create(), create().flatMap(tokens));
        assertEquals(create("a"), create("a").flatMap(tokens));
        assertEquals(create("a", "bc", "def"), create("a bc def").flatMap(tokens));
        assertEquals(create("a", "bc", "def", "x", "yz"), create("a bc def", "x yz").flatMap(tokens));
    }

    @Test
    public void testFlatMapOption() {
        final AFunction1NoThrow<AOption<String>, String> uppercaseFirst = new AFunction1NoThrow<AOption<String>, String>() {
            @Override public AOption<String> apply(String param) {
                if(Character.isUpperCase(param.charAt(0)))
                    return AOption.some(param.substring(0, 1));
                return AOption.none();
            }
        };

        assertEquals(create(), create().flatMap(uppercaseFirst));
        assertEquals(create(), create("asdf").flatMap(uppercaseFirst));
        assertEquals(create("A"), create("Asdf").flatMap(uppercaseFirst));
        assertEquals(create("A", "Q"), create("xyz", "Asdf", "Qzd", "rLS").flatMap(uppercaseFirst));
    }

    @Test
    public void testFlatten() {
        final ACollection<String, ? extends ACollection<String, ?>> flattened = createIter(Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("b", "c", "d"))).flatten();

        assertEquals(AHashSet.create("a", "b", "c", "d"), flattened.toSet());
        if(!removesDuplicates) {
            assertEquals(5, flattened.size());
            final List<String> flattenedList = new ArrayList<>(flattened.toList().asJavaUtilList()); //TODO ACollection.asJavaUtilCollection()
            Collections.sort(flattenedList);
            assertEquals(Arrays.asList("a", "b", "b", "c", "d"), flattenedList);
        }
    }

    @Test
    public void testFilter() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(create(), create().filter(len1));
        assertEquals(create(), create("abc").filter(len1));
        assertEquals(create("a"), create("a").filter(len1));
        assertEquals(create("a", "d"), create("a", "bc", "d", "efg").filter(len1));
    }

    @Test
    public void testGroupByEquals() {
        final AFunction1NoThrow<Integer, String> len = new AFunction1NoThrow<Integer, String>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        final AMap<Integer, C> grouped = create("a", "bc", "d", "efg", "hi", "j").groupBy(len);
        assertEquals(3, grouped.size());
        assertEquals(create("a", "d", "j"), grouped.getRequired(1));
        assertEquals(create("bc", "hi"), grouped.getRequired(2));
        assertEquals(create("efg"), grouped.getRequired(3));
    }

    @Test
    public void testGroupByCustomEquality() { //TODO there appears to be a bug in AHashMap if non-equal values with the same hash code are used as keys
        final AEquality equality = new AEquality() {
            @Override public boolean equals(Object o1, Object o2) {
                return ((Integer)o1)%2 == ((Integer)o2)%2;
            }

            @Override public int hashCode(Object o) {
                return 0;
            }
        };

        final AFunction1NoThrow<Integer, String> len = new AFunction1NoThrow<Integer, String>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        final AMap<Integer, C> grouped = create("a", "bc", "d", "efg", "hi", "j").groupBy(len, equality);
        assertEquals(2, grouped.size());
        assertEquals(create("a", "d", "efg", "j"), grouped.getRequired(1));
        assertEquals(create("bc", "hi"),           grouped.getRequired(2));
    }

    @Test
    public void testToList() {
        assertEquals(AList.<String>nil(), create().toList());
        assertEquals(AList.create("a", "b", "c"), create("a", "b", "c").toList());
    }

    @Test
    public void testToSetEquals() {
        assertEquals(AHashSet.<String>empty(), create().toSet());
        assertEquals(AHashSet.create("a", "b", "c"), create("a", "b", "c").toSet());
        assertEquals(AHashSet.create("a", "b", "c"), create("a", "b", "c", "a", "b", "c").toSet());
    }

    @Test
    public void testToSetCustom() {
        final AEquality equality = new AEquality() {
            @Override public boolean equals(Object o1, Object o2) {
                return ((Integer)o1)%2 == ((Integer)o2)%2;
            }

            @Override public int hashCode(Object o) {
                return 0;
            }
        };

        assertEquals(AHashSet.create(1, 2), createInts(1, 2, 3, 4, 5).toSet(equality));
    }

    @Test
    public void testContains() {
        assertEquals(false, create().contains("a"));
        assertEquals(true, create("a").contains("a"));
        assertEquals(false, create("a").contains("b"));
        assertEquals(true, create("a", "b", "c").contains("a"));
        assertEquals(true, create("a", "b", "c").contains("b"));
        assertEquals(true, create("a", "b", "c").contains("c"));
        assertEquals(false, create("a", "b", "c").contains("d"));
    }
}
