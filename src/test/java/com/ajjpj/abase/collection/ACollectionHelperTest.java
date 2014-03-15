package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.ACollection;
import com.ajjpj.abase.collection.immutable.AHashSet;
import com.ajjpj.abase.collection.immutable.AList;
import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.function.AFunction1NoThrow;
import com.ajjpj.abase.function.APredicateNoThrow;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class ACollectionHelperTest {
    @Test
    public void testMkStringNoArgs() {
        assertEquals("",        ACollectionHelper.mkString(Arrays.asList()));
        assertEquals("a",       ACollectionHelper.mkString(Arrays.asList("a")));
        assertEquals("a, b, c", ACollectionHelper.mkString(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testMkStringSeparator() {
        assertEquals("",      ACollectionHelper.mkString(Arrays.asList(),              "#"));
        assertEquals("a",     ACollectionHelper.mkString(Arrays.asList("a"),           "#"));
        assertEquals("a#b#c", ACollectionHelper.mkString(Arrays.asList("a", "b", "c"), "#"));

        assertEquals("",        ACollectionHelper.mkString(Arrays.asList(),              "?!"));
        assertEquals("a",       ACollectionHelper.mkString(Arrays.asList("a"),           "?!"));
        assertEquals("a?!b?!c", ACollectionHelper.mkString(Arrays.asList("a", "b", "c"), "?!"));
    }

    @Test
    public void testMkStringFull() {
        assertEquals("[]",      ACollectionHelper.mkString(Arrays.asList(),              "[", "#", "]"));
        assertEquals("[a]",     ACollectionHelper.mkString(Arrays.asList("a"),           "[", "#", "]"));
        assertEquals("[a#b#c]", ACollectionHelper.mkString(Arrays.asList("a", "b", "c"), "[", "#", "]"));

        assertEquals("<<>>",        ACollectionHelper.mkString(Arrays.asList(),              "<<", "?!", ">>"));
        assertEquals("<<a>>",       ACollectionHelper.mkString(Arrays.asList("a"),           "<<", "?!", ">>"));
        assertEquals("<<a?!b?!c>>", ACollectionHelper.mkString(Arrays.asList("a", "b", "c"), "<<", "?!", ">>"));
    }

    @Test
    public void testFind() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals (AOption.<String>none(), ACollectionHelper.find(Arrays.<String>asList(), len1));
        assertEquals (AOption.<String>none(), ACollectionHelper.find(Arrays.asList("", "ab", "cde"), len1));
        assertEquals (AOption.some("d"), ACollectionHelper.find(Arrays.asList("", "abc", "d", "ef", "g"), len1));
    }

    @Test
    public void testForAll() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(true, ACollectionHelper.forAll(Arrays.<String>asList(), len1));
        assertEquals(true, ACollectionHelper.forAll(Arrays.asList("a"), len1));
        assertEquals(true, ACollectionHelper.forAll(Arrays.asList("a", "b", "c"), len1));
        assertEquals(false, ACollectionHelper.forAll(Arrays.asList("a", "b", "cd", "e"), len1));
    }

    @Test
    public void testExists() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(false, ACollectionHelper.exists(Arrays.<String>asList(), len1));
        assertEquals(true, ACollectionHelper.exists(Arrays.asList("a"), len1));
        assertEquals(false, ACollectionHelper.exists(Arrays.asList("ab"), len1));
        assertEquals(true, ACollectionHelper.exists(Arrays.asList("ab", "c", "def"), len1));
    }

    @Test
    public void testMap() {
        final AFunction1NoThrow<Integer, String> len = new AFunction1NoThrow<Integer, String>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        assertEquals(Arrays.<Integer>asList(), ACollectionHelper.map(Arrays.<String>asList(), len));
        assertEquals(Arrays.asList(1), ACollectionHelper.map(Arrays.asList("a"), len));
        assertEquals(Arrays.asList(2, 1, 3), ACollectionHelper.map(Arrays.asList("ab", "c", "def"), len));
    }

    @Test
    public void testFlatMapTokens() {
        final AFunction1NoThrow<List<String>, String> tokens = new AFunction1NoThrow<List<String>, String>() {
            @Override public List<String> apply(String param) {
                return Arrays.asList(param.split(" "));
            }
        };

        assertEquals(Arrays.<String>asList(), ACollectionHelper.flatMap(Arrays.<String>asList(), tokens));
        assertEquals(Arrays.asList("a"), ACollectionHelper.flatMap(Arrays.asList("a"), tokens));
        assertEquals(Arrays.asList("a", "bc", "def"), ACollectionHelper.flatMap(Arrays.asList("a bc def"), tokens));
        assertEquals(Arrays.asList("a", "bc", "def", "x", "yz"), ACollectionHelper.flatMap(Arrays.asList("a bc def", "x yz"), tokens));
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

        assertEquals(Arrays.<String>asList(), ACollectionHelper.flatMap(Arrays.<String>asList(), uppercaseFirst));
        assertEquals(Arrays.asList("A"), ACollectionHelper.flatMap(Arrays.asList("Asdf"), uppercaseFirst));
        assertEquals(Arrays.asList("A", "Q"), ACollectionHelper.flatMap(Arrays.asList("xyz", "Asdf", "Qzd", "rLS"), uppercaseFirst));
    }

    @Test
    public void testFlatten() {
        final Set<Set<String>> set = new HashSet<>();

        set.add(new HashSet<>(Arrays.asList("a", "b")));
        set.add(new HashSet<>(Arrays.asList("b", "c", "d")));

        final Collection<String> flattened = ACollectionHelper.flatten(set);
        assertEquals(5, flattened.size());

        final List<String> flattenedList = new ArrayList<>(flattened);
        Collections.sort(flattenedList);
        assertEquals(Arrays.asList("a", "b", "b", "c", "d"), flattenedList);
    }

    @Test
    public void testFilter() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(Arrays.<String>asList(), ACollectionHelper.filter(Arrays.<String>asList(), len1));
        assertEquals(Arrays.<String>asList(), ACollectionHelper.filter(Arrays.asList("abc"), len1));
        assertEquals(Arrays.asList("a"), ACollectionHelper.filter(Arrays.asList("a"), len1));
        assertEquals(Arrays.asList("a", "d"), ACollectionHelper.filter(Arrays.asList("a", "bc", "d", "efg"), len1));
    }

    @Test
    public void testGroupByEquals() {
        final AFunction1NoThrow<Integer, String> len = new AFunction1NoThrow<Integer, String>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        final Map<Integer, Collection<String>> grouped = ACollectionHelper.groupBy(Arrays.asList("a", "bc", "d", "efg", "hi", "j"), len);
        assertEquals(3, grouped.size());
        assertEquals(Arrays.asList("a", "d", "j"), grouped.get(1));
        assertEquals(Arrays.asList("bc", "hi"), grouped.get(2));
        assertEquals(Arrays.asList("efg"), grouped.get(3));
    }

    @Test
    public void testGroupByCustomEquality() {
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

        final Map<AEqualsWrapper<Integer>, Collection<String>> grouped = ACollectionHelper.groupBy(Arrays.asList("a", "bc", "d", "efg", "hi", "j"), len, equality);
        assertEquals(2, grouped.size());
        assertEquals(Arrays.asList("a", "d", "efg", "j"), grouped.get(new AEqualsWrapper<>(equality, 1)));
        assertEquals(Arrays.asList("bc", "hi"),           grouped.get(new AEqualsWrapper<>(equality, 2)));
    }

    @Test
    public void testAsCollectionCopy() {
        final List<String> list = new ArrayList<>(Arrays.asList("a", "b"));

        final ACollection<String, ?> copied = ACollectionHelper.asACollectionCopy(list);
        assertEquals(2, copied.size());
        assertEquals(true, copied.nonEmpty());
        assertEquals(false, copied.isEmpty());

        assertEquals(AList.create("a", "b"), copied.toList());
        assertEquals(AHashSet.create("a", "b"), copied.toSet());

        list.clear();

        assertEquals(2, copied.size());
        assertEquals(true, copied.nonEmpty());
        assertEquals(false, copied.isEmpty());

        assertEquals(AList.create("a", "b"), copied.toList());
        assertEquals(AHashSet.create("a", "b"), copied.toSet());

        final ACollection<String, ?> copiedEmpty = ACollectionHelper.asACollectionCopy(Arrays.<String>asList());
        assertEquals(0, copiedEmpty.size());
        assertEquals(true, copiedEmpty.isEmpty());
        assertEquals(false, copiedEmpty.nonEmpty());

        assertEquals(AList.<String>nil(), copiedEmpty.toList());
        assertEquals(AHashSet.<String>empty(), copiedEmpty.toSet());
    }

    @Test
    public void testAsCollectionCopyMkStringNoArgs() {
        assertEquals("",        ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList())));
        assertEquals("a",       ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a"))));
        assertEquals("a, b, c", ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a", "b", "c"))));
    }

    @Test
    public void testAsCollectionCopyMkStringSeparator() {
        assertEquals("",      ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList()),              "#"));
        assertEquals("a",     ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a")),           "#"));
        assertEquals("a#b#c", ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a", "b", "c")), "#"));

        assertEquals("",        ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList()),              "?!"));
        assertEquals("a",       ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a")),           "?!"));
        assertEquals("a?!b?!c", ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a", "b", "c")), "?!"));
    }

    @Test
    public void testAsCollectionCopyMkStringFull() {
        assertEquals("[]",      ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList()),              "[", "#", "]"));
        assertEquals("[a]",     ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a")),           "[", "#", "]"));
        assertEquals("[a#b#c]", ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a", "b", "c")), "[", "#", "]"));

        assertEquals("<<>>",        ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList()),              "<<", "?!", ">>"));
        assertEquals("<<a>>",       ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a")),           "<<", "?!", ">>"));
        assertEquals("<<a?!b?!c>>", ACollectionHelper.mkString(ACollectionHelper.asACollectionCopy(Arrays.asList("a", "b", "c")), "<<", "?!", ">>"));
    }

    @Test
    public void testAsCollectionCopyOperations() {
        fail("todo");
    }

    @Test
    public void testAsCollectionView() {
        final List<String> list = new ArrayList<>(Arrays.asList("a", "b"));

        final ACollection<String, ?> copied = ACollectionHelper.asACollectionView(list);
        assertEquals(2, copied.size());
        assertEquals(true, copied.nonEmpty());
        assertEquals(false, copied.isEmpty());

        assertEquals(AList.create("a", "b"), copied.toList());
        assertEquals(AHashSet.create("a", "b"), copied.toSet());

        list.clear();

        assertEquals(0, copied.size());
        assertEquals(false, copied.nonEmpty());
        assertEquals(true, copied.isEmpty());

        assertEquals(AList.<String>nil(), copied.toList());
        assertEquals(AHashSet.<String>empty(), copied.toSet());

        final ACollection<String, ?> copiedEmpty = ACollectionHelper.asACollectionCopy(Arrays.<String>asList());
        assertEquals(0, copiedEmpty.size());
        assertEquals(true, copiedEmpty.isEmpty());
        assertEquals(false, copiedEmpty.nonEmpty());

        assertEquals(AList.<String>nil(), copiedEmpty.toList());
        assertEquals(AHashSet.<String>empty(), copiedEmpty.toSet());
    }
}
