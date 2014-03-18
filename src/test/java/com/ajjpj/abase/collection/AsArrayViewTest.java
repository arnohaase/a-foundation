package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.ACollection;
import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.function.AFunction1NoThrow;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AsArrayViewTest extends AbstractCollectionTest<ACollection<String>, ACollection<Integer>, ACollection<Iterable<String>>> {
    public AsArrayViewTest() {
        super(false);
    }

    @Override public ACollection<String> create(String... elements) {
        return ACollectionHelper.asArrayView(elements);
    }

    @Override public ACollection<Integer> createInts(Integer... elements) {
        return ACollectionHelper.asACollectionCopy(Arrays.asList(elements));
    }

    @SuppressWarnings("unchecked")
    @Override public ACollection<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        final Iterable<String>[] result = new Iterable[elements.size()];

        int idx=0;
        for(Iterable<String> iter: elements) {
            result[idx++] = new HashSet<>(ACollectionHelper.asJavaUtilCollection(iter));
        }

        return ACollectionHelper.asArrayView(result);
    }

    @Test
    public void testIsolation() {
        final String[] raw = new String[]{"a", "b"};
        final ACollection<String> wrapped = ACollectionHelper.asArrayView(raw);

        raw[0] = "x";
        raw[1] = "y";

        assertEquals("x, y", wrapped.mkString());
    }

    private ACollection<String> createColl(String... elements) {
        return ACollectionHelper.asACollectionCopy(Arrays.asList(elements));
    }

    @Test
    @Override
    public void testFlatMapTokens() {
        final AFunction1NoThrow<String, List<String>> tokens = new AFunction1NoThrow<String, List<String>>() {
            @Override public List<String> apply(String param) {
                return Arrays.asList(param.split(" "));
            }
        };

        assertEquals(createColl(), create().flatMap(tokens));
        assertEquals(createColl("a"), create("a").flatMap(tokens));
        assertEquals(createColl("a", "bc", "def"), create("a bc def").flatMap(tokens));
        assertEquals(createColl("a", "bc", "def", "x", "yz"), create("a bc def", "x yz").flatMap(tokens));
    }

    @Test
    @Override
    public void testFlatMapOption() {
        final AFunction1NoThrow<String, AOption<String>> uppercaseFirst = new AFunction1NoThrow<String, AOption<String>>() {
            @Override public AOption<String> apply(String param) {
                if(Character.isUpperCase(param.charAt(0)))
                    return AOption.some(param.substring(0, 1));
                return AOption.none();
            }
        };

        assertEquals(createColl(), create().flatMap(uppercaseFirst));
        assertEquals(createColl(), create("asdf").flatMap(uppercaseFirst));
        assertEquals(createColl("A"), create("Asdf").flatMap(uppercaseFirst));
        assertEquals(createColl("A", "Q"), create("xyz", "Asdf", "Qzd", "rLS").flatMap(uppercaseFirst));
    }

}
