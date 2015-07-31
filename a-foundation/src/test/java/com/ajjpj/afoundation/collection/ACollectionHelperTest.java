package com.ajjpj.afoundation.collection;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.function.AFunction1NoThrow;
import com.ajjpj.afoundation.function.AFunction2;
import com.ajjpj.afoundation.function.APartialFunctionNoThrow;
import com.ajjpj.afoundation.function.APredicateNoThrow;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;


/**
 * @author arno
 */
public class ACollectionHelperTest {
    @Test
    public void testMkStringNoArgs() {
        assertEquals("",        ACollectionHelper.mkString (Arrays.asList ()));
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

        assertEquals (AOption.<String>none(), ACollectionHelper.find (Arrays.<String>asList (), len1));
        assertEquals (AOption.<String>none(), ACollectionHelper.find (Arrays.asList ("", "ab", "cde"), len1));
        assertEquals (AOption.some("d"), ACollectionHelper.find (Arrays.asList ("", "abc", "d", "ef", "g"), len1));
    }

    @Test
    public void testForAll() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(true, ACollectionHelper.forAll(Arrays.<String>asList(), len1));
        assertEquals(true, ACollectionHelper.forAll (Arrays.asList ("a"), len1));
        assertEquals(true, ACollectionHelper.forAll (Arrays.asList ("a", "b", "c"), len1));
        assertEquals(false, ACollectionHelper.forAll (Arrays.asList ("a", "b", "cd", "e"), len1));
    }

    @Test
    public void testExists() {
        final APredicateNoThrow<String> len1 = new APredicateNoThrow<String>() {
            @Override  public boolean apply(String o) {
                return o.length() == 1;
            }
        };

        assertEquals(false, ACollectionHelper.exists (Arrays.<String>asList (), len1));
        assertEquals(true, ACollectionHelper.exists (Arrays.asList ("a"), len1));
        assertEquals(false, ACollectionHelper.exists (Arrays.asList ("ab"), len1));
        assertEquals(true, ACollectionHelper.exists (Arrays.asList ("ab", "c", "def"), len1));
    }

    @Test
    public void testMap() {
        final AFunction1NoThrow<String, Integer> len = new AFunction1NoThrow<String, Integer>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        assertEquals(Arrays.<Integer>asList(), ACollectionHelper.map (Arrays.<String>asList (), len));
        assertEquals(Arrays.asList(1), ACollectionHelper.map (Arrays.asList ("a"), len));
        assertEquals(Arrays.asList(2, 1, 3), ACollectionHelper.map (Arrays.asList ("ab", "c", "def"), len));
    }

    @Test
    public void testFlatMapTokens() {
        final AFunction1NoThrow<String, List<String>> tokens = new AFunction1NoThrow<String, List<String>>() {
            @Override public List<String> apply(String param) {
                return Arrays.asList (param.split (" "));
            }
        };

        assertEquals(Arrays.<String>asList(), ACollectionHelper.flatMap(Arrays.<String>asList(), tokens));
        assertEquals(Arrays.asList("a"), ACollectionHelper.flatMap (Arrays.asList ("a"), tokens));
        assertEquals(Arrays.asList("a", "bc", "def"), ACollectionHelper.flatMap (Arrays.asList ("a bc def"), tokens));
        assertEquals(Arrays.asList("a", "bc", "def", "x", "yz"), ACollectionHelper.flatMap (Arrays.asList ("a bc def", "x yz"), tokens));
    }

    @Test
    public void testFlatMapOption() {
        final AFunction1NoThrow<String, AOption<String>> uppercaseFirst = new AFunction1NoThrow<String, AOption<String>>() {
            @Override public AOption<String> apply(String param) {
                if(Character.isUpperCase(param.charAt(0)))
                    return AOption.some(param.substring(0, 1));
                return AOption.none ();
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
        assertEquals(5, flattened.size ());

        final List<String> flattenedList = new ArrayList<>(flattened);
        Collections.sort (flattenedList);
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
        assertEquals(Arrays.<String>asList(), ACollectionHelper.filter (Arrays.asList ("abc"), len1));
        assertEquals (Arrays.asList ("a"), ACollectionHelper.filter (Arrays.asList ("a"), len1));
        assertEquals(Arrays.asList("a", "d"), ACollectionHelper.filter(Arrays.asList("a", "bc", "d", "efg"), len1));
    }

    @Test
    public void testGroupByEquals() {
        final AFunction1NoThrow<String, Integer> len = new AFunction1NoThrow<String, Integer>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        final Map<Integer, List<String>> grouped = ACollectionHelper.groupBy(Arrays.asList("a", "bc", "d", "efg", "hi", "j"), len);
        assertEquals(3, grouped.size ());
        assertEquals(Arrays.asList("a", "d", "j"), grouped.get (1));
        assertEquals(Arrays.asList("bc", "hi"), grouped.get (2));
        assertEquals(Arrays.asList("efg"), grouped.get (3));
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

        final AFunction1NoThrow<String, Integer> len = new AFunction1NoThrow<String, Integer>() {
            @Override public Integer apply(String param) {
                return param.length();
            }
        };

        final Map<AEqualsWrapper<Integer>, List<String>> grouped = ACollectionHelper.groupBy(Arrays.asList("a", "bc", "d", "efg", "hi", "j"), len, equality);
        assertEquals(2, grouped.size ());
        assertEquals(Arrays.asList("a", "d", "efg", "j"), grouped.get(new AEqualsWrapper<>(equality, 1)));
        assertEquals(Arrays.asList("bc", "hi"),           grouped.get(new AEqualsWrapper<>(equality, 2)));
    }

    @Test
    public void testAsJavaUtilCollection() {
        assertEquals(true, ACollectionHelper.asJavaUtilCollection(new ArrayList<>()           ).isEmpty());
        assertEquals(true, ACollectionHelper.asJavaUtilCollection(new ArrayList<>().iterator()).isEmpty());

        assertEquals(true, ACollectionHelper.asJavaUtilCollection(new Iterable<String>() {
            @Override public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override public boolean hasNext() {
                        return false;
                    }

                    @Override public String next() {
                        return null;
                    }

                    @Override public void remove() {
                    }
                };
            }
        }).isEmpty());

        assertEquals(Arrays.asList("a"), ACollectionHelper.asJavaUtilCollection(Arrays.asList("a")           ));
        assertEquals(Arrays.asList("a"), ACollectionHelper.asJavaUtilCollection (Arrays.asList ("a").iterator ()));

        assertEquals(Arrays.asList("a", "b"), ACollectionHelper.asJavaUtilCollection (Arrays.asList ("a", "b")));
        assertEquals(Arrays.asList("a", "b"), ACollectionHelper.asJavaUtilCollection (Arrays.asList ("a", "b").iterator ()));
    }

    @Test public void testFoldLeft() throws Exception {
        assertEquals (ACollectionHelper.foldLeft (
                          Arrays.asList (1, 2, 3, 4, 5),
                        0, new AFunction2<Integer, Integer, Integer, Exception> () {
                              @Override public Integer apply (Integer param1, Integer param2) throws Exception {
                                  return param1 * 2 + param2;
                              }
                          }).intValue (),
                      ((((1*2+2)*2+3)*2+4)*2+5)
        );
    }

    @Test
    public void testFoldRight() throws Exception {
        assertEquals (ACollectionHelper.foldRight (
                        Arrays.asList (1, 2, 3, 4, 5),
                        0, new AFunction2<Integer, Integer, Integer, Exception> () {
                            @Override
                            public Integer apply (Integer param1, Integer param2) throws Exception {
                                return param1 * 2 + param2;
                            }
                        }).intValue (),
                      ((((5 * 2 + 4) * 2 + 3) * 2 + 2) * 2 + 1)
        );
    }

    @Test
    public void testCollect() throws Exception {
        APartialFunctionNoThrow<Integer, Double> squareRoot = new APartialFunctionNoThrow<Integer, Double> () {
            @Override public boolean isDefinedAt (Integer param) {
                return param != 0;
            }

            @Override public Double apply (Integer param) {
                return Math.sqrt (param);
            }
        };
        final List<Double> result = ACollectionHelper.collect (Arrays.asList (0, 1, 2, 3, 4), squareRoot);
        assertEquals (4, result.size ());
    }
}
