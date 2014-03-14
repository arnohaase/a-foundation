package com.ajjpj.abase.collection;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * @author arno
 */
public class ACollectionHelperTest {
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
}
