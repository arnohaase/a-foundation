package com.ajjpj.abase.collection;

import java.util.*;

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
}
