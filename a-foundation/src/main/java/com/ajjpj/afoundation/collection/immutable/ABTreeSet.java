package com.ajjpj.afoundation.collection.immutable;


/**
 * @author arno
 */
public class ABTreeSet<T> extends MapAsSetWrapper<T, ABTreeSet<T>> {

    public static <T> ABTreeSet<T> empty (ABTreeSpec spec) {
        return new ABTreeSet<> (ABTreeMap.<T,Boolean> empty (spec));
    }

    @SuppressWarnings ("unchecked")
    public static <T> ABTreeSet<T> create (ABTreeSpec spec, T... elements) {
        return create (spec, elements);
    }

    public static <T> ABTreeSet<T> create (ABTreeSpec spec, Iterable<T> elements) {
        ABTreeSet<T> result = empty (spec);

        for (T el: elements) {
            result = result.added (el);
        }
        return result;
    }

    public static <T> ABTreeSet<T> create (ABTreeMap<T,?> inner) {
        return new ABTreeSet<> (inner);
    }

    //TODO ASortedSet


    private ABTreeSet (AMap<T, ?> inner) {
        super (inner);
    }

    @Override protected ABTreeSet<T> wrapAsSet (AMap<T, ?> inner) {
        return new ABTreeSet<> (inner);
    }
}
