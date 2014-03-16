package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.ACollection;
import com.ajjpj.abase.collection.immutable.AHashSet;
import com.ajjpj.abase.collection.immutable.AList;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author arno
 */
public class AsCollectionViewTest extends AbstractCollectionTest<ACollectionHelper.ACollectionWrapper<String>, ACollectionHelper.ACollectionWrapper<Integer>, ACollectionHelper.ACollectionWrapper<Iterable<String>>> {
    public AsCollectionViewTest() {
        super(false);
    }

    @Override public ACollectionHelper.ACollectionWrapper<String> create(String... elements) {
        return ACollectionHelper.asACollectionView(Arrays.asList(elements));
    }

    @Override public ACollectionHelper.ACollectionWrapper<Integer> createInts(Integer... elements) {
        return ACollectionHelper.asACollectionView(Arrays.asList(elements));
    }

    @Override public ACollectionHelper.ACollectionWrapper<Iterable<String>> createIter(Collection<? extends Iterable<String>> elements) {
        final List<Iterable<String>> result = new ArrayList<>(); // create inner collections with the opposite set/list semantics from the outer collection
        for(Iterable<String> iter: elements) {
            result.add(new HashSet<>(ACollectionHelper.asJavaUtilCollection(iter)));
        }

        return ACollectionHelper.asACollectionView(result);
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
