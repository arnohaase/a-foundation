package com.ajjpj.afoundation.collection.immutable;

import java.util.Arrays;
import java.util.Comparator;


/**
 * @author arno
 */
abstract class AbstractBTreeTest {
    protected static final Comparator naturalOrder = new Comparator<Comparable>() {
        @Override public int compare (Comparable o1, Comparable o2) {
            return o1.compareTo (o2);
        }
    };

    protected IndexNode branch (LeafNode... leaves) {
        final Object[] separators = new Object[leaves.length-1];
        for (int i=1; i<leaves.length; i++) {
            separators[i-1] = leaves[i].keys[0];
        }

        return new IndexNode (new ABTreeSpec (8, naturalOrder), separators, leaves);
    }

    protected LeafNode leaf (Integer... numbers) {
        final Integer[] values = Arrays.copyOf (numbers, numbers.length);
        for (int i=0; i<values.length; i++) {
            values[i] = values[i] * 100;
        }

        return new LeafNode (new ABTreeSpec (8, naturalOrder), numbers, values);
    }
}
