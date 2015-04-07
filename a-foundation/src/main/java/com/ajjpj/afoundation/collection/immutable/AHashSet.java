package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;

import java.util.Arrays;


/**
 * @author arno
 */
public class AHashSet<T> extends MapAsSetWrapper<T, AHashSet<T>> {
    @SuppressWarnings ("unchecked")
    private static final AHashSet EMPTY_IDENTITY = new AHashSet (AHashMap.empty (AEquality.IDENTITY));
    @SuppressWarnings ("unchecked")
    private static final AHashSet EMPTY_EQUALS   = new AHashSet (AHashMap.empty (AEquality.EQUALS));

    public static <T> AHashSet<T> empty () {
        return empty (AEquality.EQUALS);
    }

    @SuppressWarnings ("unchecked")
    public static <T> AHashSet<T> empty (AEquality equality) {
        if (equality == AEquality.EQUALS) {
            return EMPTY_EQUALS;
        }
        if (equality == AEquality.IDENTITY) {
            return EMPTY_IDENTITY;
        }

        return new AHashSet<> (AHashMap.<T,Boolean> empty (equality));
    }

    @SuppressWarnings ("unchecked")
    public static <T> AHashSet<T> create (T... elements) {
        return create (AEquality.EQUALS, elements);
    }

    public static <T> AHashSet<T> create (Iterable<T> elements) {
        return create (AEquality.EQUALS, elements);
    }

    @SuppressWarnings ("unchecked")
    public static <T> AHashSet<T> create (AEquality equality, T... elements) {
        return create (equality, Arrays.asList (elements));
    }

    public static <T> AHashSet<T> create (AEquality equality, Iterable<T> elements) {
        AHashSet<T> result = empty (equality);

        for (T el: elements) {
            result = result.added (el);
        }
        return result;
    }

    public static <T> AHashSet<T> create (AHashMap<T,?> inner) {
        return new AHashSet<> (inner);
    }

    //TODO ASortedSet

    private Object readResolve() {
        if (isEmpty ()) {
            if (equalityForEquals () == AEquality.EQUALS) return EMPTY_EQUALS;
            if (equalityForEquals () == AEquality.IDENTITY) return EMPTY_IDENTITY;
        }
        return this;
    }

    private AHashSet (AMap<T, ?> inner) {
        super (inner);
    }

    @Override protected AHashSet<T> wrapAsSet (AMap<T, ?> inner) {
        return new AHashSet<> (inner);
    }
}
