package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.function.AFunction1NoThrow;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * @author arno
 */
public class AsArrayCopyTest extends AbstractCollectionTest<ACollectionHelper.AArrayWrapper<String>, ACollectionHelper.ACollectionWrapper<Integer>, ACollectionHelper.AArrayWrapper<Iterable<String>>> {
    public AsArrayCopyTest() {
        super(false);
    }

    @Override public ACollectionHelper.AArrayWrapper<String> create(String... elements) {
        return ACollectionHelper.asArrayCopy(elements);
    }

    @Override public ACollectionHelper.ACollectionWrapper<Integer> createInts(Integer... elements) {
        return ACollectionHelper.asACollectionCopy(Arrays.asList(elements));
    }

    @SuppressWarnings("unchecked")
    @Override public ACollectionHelper.AArrayWrapper<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        final Iterable<String>[] result = new Iterable[elements.size()];

        int idx=0;
        for(Iterable<String> iter: elements) {
            result[idx++] = new HashSet<>(ACollectionHelper.asJavaUtilCollection(iter));
        }

        return ACollectionHelper.asArrayCopy(result);
    }

    @Test
    public void testIsolation() {
        final String[] raw = new String[]{"a", "b"};
        final ACollectionHelper.AArrayWrapper<String> wrapped = ACollectionHelper.asArrayCopy(raw);

        raw[0] = "x";
        raw[1] = "y";

        assertEquals("a, b", wrapped.mkString());
    }

    private ACollectionHelper.ACollectionWrapper<String> createColl(String... elements) {
        return ACollectionHelper.asACollectionCopy(Arrays.asList(elements));
    }

    @Test
    @Override
    public void testFlatMapTokens() {
        final AFunction1NoThrow<List<String>, String> tokens = new AFunction1NoThrow<List<String>, String>() {
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
        final AFunction1NoThrow<AOption<String>, String> uppercaseFirst = new AFunction1NoThrow<AOption<String>, String>() {
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
