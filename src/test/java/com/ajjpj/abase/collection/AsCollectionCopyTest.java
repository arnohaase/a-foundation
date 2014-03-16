package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.AHashSet;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author arno
 */
public class AsCollectionCopyTest extends AbstractCollectionTest<ACollectionHelper.ACollectionWrapper<String>, ACollectionHelper.ACollectionWrapper<Integer>, ACollectionHelper.ACollectionWrapper<Iterable<String>>> {
    public AsCollectionCopyTest() {
        super(false);
    }

    @Override public ACollectionHelper.ACollectionWrapper<String> create(String... elements) {
        return ACollectionHelper.asACollectionCopy(Arrays.asList(elements));
    }

    @Override public ACollectionHelper.ACollectionWrapper<Integer> createInts(Integer... elements) {
        return ACollectionHelper.asACollectionCopy(Arrays.asList(elements));
    }

    @Override public ACollectionHelper.ACollectionWrapper<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        final List<Iterable<String>> result = new ArrayList<>(); // create inner collections with the opposite set/list semantics from the outer collection
        for(Iterable<String> iter: elements) {
            result.add(new HashSet<String>(ACollectionHelper.asJavaUtilCollection(iter)));
        }

        return ACollectionHelper.asACollectionCopy(result);
    }

    @Test
    public void testToSetIdentity() { //TODO
        assertEquals(AHashSet.<String>empty(AEquality.IDENTITY), create().toSet(AEquality.IDENTITY));
        assertEquals(AHashSet.create(AEquality.IDENTITY, "a", "b", "c"), create("a", "b", "c").toSet(AEquality.IDENTITY)); //TODO there appears to be a bug in AHashMap wrt AEquality.IDENTITY
        assertEquals(AHashSet.create(AEquality.IDENTITY, "a", "b", "c"), create("a", "b", "c", "a", "b", "c").toSet(AEquality.IDENTITY));
    }
}
