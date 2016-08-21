package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;

import java.util.Arrays;


/**
 * @author arno
 */
public class AListSet<K> extends MapAsSetWrapper<K, AListSet<K>> {
    @SuppressWarnings ("unchecked")
    private static final AListSet EMPTY_EQUALS   = new AListSet (AListMap.empty (AEquality.EQUALS));

    @SuppressWarnings ("unchecked")
    private static final AListSet EMPTY_IDENTITY = new AListSet (AListMap.empty (AEquality.IDENTITY));

    public static <T> AListSet<T> empty () {
        return empty (AEquality.EQUALS);
    }

    @SuppressWarnings ("unchecked")
    public static <T> AListSet<T> empty (AEquality equality) {
        if (equality == AEquality.EQUALS) {
            return EMPTY_EQUALS;
        }
        if (equality == AEquality.IDENTITY) {
            return EMPTY_IDENTITY;
        }

        return new AListSet<> (AHashMap.<T,Boolean> empty (equality));
    }

    @SuppressWarnings ("unchecked")
    public static <T> AListSet<T> create (T... elements) {
        return create (AEquality.EQUALS, elements);
    }

    public static <T> AListSet<T> create (Iterable<T> elements) {
        return create (AEquality.EQUALS, elements);
    }

    @SuppressWarnings ("unchecked")
    public static <T> AListSet<T> create (AEquality equality, T... elements) {
        return create (equality, Arrays.asList (elements));
    }

    public static <T> AListSet<T> create (AEquality equality, Iterable<T> elements) {
        AListSet<T> result = empty (equality);

        for (T el: elements) {
            result = result.with (el);
        }
        return result;
    }

    public static <T> AListSet<T> create (AHashMap<T,?> inner) {
        return new AListSet<> (inner);
    }


    public AListSet (AMap<K, ?> inner) {
        super (inner);
    }

    @Override protected AListSet<K> wrapAsSet (AMap<K, ?> inner) {
        return new AListSet<> (inner);
    }


}
