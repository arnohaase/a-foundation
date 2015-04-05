package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;

import java.util.Arrays;
import java.util.Comparator;


/**
 * @author arno
 */
public class ARedBlackTreeSet<T> extends MapAsSetWrapper<T, ARedBlackTreeSet<T>> {
    @SuppressWarnings ("unchecked")
    public static <T> ARedBlackTreeSet<T> empty (Comparator comparator) {
        return new ARedBlackTreeSet<> (ARedBlackTreeMap.<T,Boolean> empty (comparator));
    }

    @SuppressWarnings ("unchecked")
    public static <T> ARedBlackTreeSet<T> create (Comparator comparator, T... elements) {
        return create (comparator, Arrays.asList (elements));
    }

    public static <T> ARedBlackTreeSet<T> create (Comparator comparator, Iterable<T> elements) {
        ARedBlackTreeSet<T> result = empty (comparator);

        for (T el: elements) {
            result = result.added (el);
        }
        return result;
    }

    public static <T> ARedBlackTreeSet<T> create (ARedBlackTreeMap<T,?> inner) {
        return new ARedBlackTreeSet<> (inner);
    }

    //TODO ASortedSet


    private ARedBlackTreeSet (AMap<T, ?> inner) {
        super (inner);
    }

    @Override protected ARedBlackTreeSet<T> wrapAsSet (AMap<T, ?> inner) {
        return new ARedBlackTreeSet<> (inner);
    }
}
