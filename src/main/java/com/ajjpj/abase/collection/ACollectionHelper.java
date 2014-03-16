package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.collection.immutable.AbstractACollection;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

import java.lang.reflect.Array;
import java.util.*;


/**
 * This class consists of a number of useful methods that operate on a variety of collection types.
 *
 * @author arno
 */
public class ACollectionHelper {
    /**
     * Returns a string representation of a collection, separating elements with a comma.
     */
    public static String mkString(Iterable<?> iterable) {
        return mkString(iterable, ", ");
    }

    /**
     * Returns a string representation of a collection, separating elements with a <code>separator</code>.
     */
    public static String mkString(Iterable<?> iterable, String separator) {
        return mkString(iterable, "", separator, "");
    }

    /**
     * Returns a string representation of a collection, separating elements with a <code>separator</code> and putting
     *  <code>prefix</code> before the first and a <code>suffix</code> after the last element.
     */
    public static String mkString(Iterable<?> iterable, String prefix, String separator, String suffix) {
        final StringBuilder result = new StringBuilder(prefix);

        boolean first = true;
        for(Object o: iterable) {
            if(first) {
                first = false;
            }
            else {
                result.append(separator);
            }
            result.append(o);
        }

        result.append(suffix);
        return result.toString();
    }

    /**
     * Returns an element of a collection that matches a predicate, if any, or AOption.none() if there is no match.
     */
    public static <T, E extends Exception> AOption<T> find(Iterable<T> coll, APredicate<T, E> pred) throws E {
        for(T o: coll) {
            if(pred.apply(o)) {
                return AOption.some(o);
            }
        }
        return AOption.none();
    }

    /**
     * Matches a predicate against collection elements, and returns true iff it matches them all.
     */
    public static <T, E extends Exception> boolean forAll(Iterable<T> coll, APredicate<T, E> pred) throws E {
        for(T o: coll) {
            if(!pred.apply(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Matches a predicate against collection elements, and returns true iff it matches at least one of them.
     */
    public static <T, E extends Exception> boolean exists(Iterable<T> coll, APredicate<T, E> pred) throws E {
        for(T o: coll) {
            if(pred.apply(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies a transformation function to all elements of a collection, creating a new collection from the results.
     */
    public static <T, X, E extends Exception> Collection<X> map(Iterable<T> coll, AFunction1<X, T, E> f) throws E {
        final List<X> result = new ArrayList<>();

        for(T o: coll) {
            result.add(f.apply(o));
        }

        return result;
    }

    /**
     * Same as <code>map()</code>, except that the transformation function returns collections and all the results are
     *  flattened into a single collection.
     */
    public static <T, X, E extends Exception> Collection<X> flatMap(Iterable<T> coll, AFunction1<? extends Iterable<X>, T, E> f) throws E {
        final List<X> result = new ArrayList<>();

        for(T o: coll) {
            for(X el: f.apply(o)) {
                result.add(el);
            }
        }

        return result;
    }

    /**
     * Takes a collection of collections and creates a new collection from the elements, leaving out the innermost
     *  level of collection.
     */
    public static <T> Collection<T> flatten(Iterable<? extends Iterable<T>> coll) {
        final List<T> result = new ArrayList<>();
        for(Iterable<T> o: coll) {
            for(T el: o) {
                result.add(el);
            }
        }
        return result;
    }

    /**
     * Matches all elements of a collection against a predicate, creating a new collection from those that match.
     */
    public static <T, E extends Exception> Collection<T> filter(Iterable<T> coll, APredicate<T, E> pred) throws E {
        final List<T> result = new ArrayList<>();
        for(T o: coll) {
            if(pred.apply(o)) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Creates a Map from a collection. Each element's key is determined by applying a function to the element. All
     *  elements with the same key are stored as that key's value in the returned Map.
     */
    public static <T, X, E extends Exception> Map<X, Collection<T>> groupBy(Iterable<T> coll, AFunction1<X, T, E> f) throws E {
        final Map<X, Collection<T>> result = new HashMap<>();
        for(T o: coll) {
            final X key = f.apply(o);
            Collection<T> perKey = result.get(key);
            if(perKey == null) {
                perKey = new ArrayList<>();
                result.put(key, perKey);
            }
            perKey.add(o);
        }
        return result;
    }

    /**
     * Creates a Map from a collection. Each element's key is determined by applying a function to the element. All
     *  elements with the same key are stored as that key's value in the returned Map.<p />
     *
     * This method gives control over the equalityForEquals strategy used to determine if two keys are 'equal'. To accomodate that,
     *  the keys are wrapped in AEqualsWrapper. <p />
     *
     * This method is rather technical in nature, and it is probably more useful as a foundation for generic code than
     *  for direct use by applications.
     */
    public static <T, X, E extends Exception> Map<AEqualsWrapper<X>, Collection<T>> groupBy(Iterable<T> coll, AFunction1<X, T, E> f, AEquality keyEquality) throws E {
        final Map<AEqualsWrapper<X>, Collection<T>> result = new HashMap<>();
        for(T o: coll) {
            final AEqualsWrapper<X> key = new AEqualsWrapper<>(keyEquality, f.apply(o));
            Collection<T> perKey = result.get(key);
            if(perKey == null) {
                perKey = new ArrayList<>();
                result.put(key, perKey);
            }
            perKey.add(o);
        }
        return result;
    }

    /**
     * Copies the content of an <code>Iterable</code> into an (immutable) <code>ACollection</code> instance. Subsequent
     *  changes to the underlying collection have no effect on the returned <code>ACollection</code> instance.<p />
     *
     * The returned collection has list semantics with regard to <code>map()></code> and other modifying methods;
     *  duplicate values are allowed.
     */
    public static <T> ACollectionWrapper<T> asACollectionCopy(Collection<T> c) {
        return asACollectionView(new ArrayList<>(c));
    }

    /**
     * Wraps the content of a <code>java.util.Collection</code> in an <code>ACollection</code> instance. While the returned
     *  instance itself has no mutator methods, changes to the underlying collection are reflected in the wrapping
     *  <code>ACollection</code> instance.<p />
     *
     * The returned collection has list semantics with regard to <code>map()</code> and other modifying methods; duplicate
     *  values are allowed.
     */
    @SuppressWarnings("unchecked")
    public static <T> ACollectionWrapper<T> asACollectionView(Collection<T> c) {
        return new ACollectionWrapper(c);
    }

    /**
     * Copies the content of an array into an (immutable) <code>ACollection</code> instance. Subsequent
     *  changes to the underlying array have no effect on the returned <code>ACollection</code> instance.<p />
     *
     * The returned collection has list semantics with regard to <code>map()></code> and other modifying methods;
     *  duplicate values are allowed.
     */
    @SuppressWarnings("unchecked")
    public static <T> AArrayWrapper<T> asArrayCopy(T[] c) {
        final T[] newArray = (T[]) Array.newInstance(c.getClass().getComponentType(), c.length);
        System.arraycopy(c, 0, newArray, 0, c.length);
        return new AArrayWrapper<>(newArray);
    }

    /**
     * Wraps the content of an array in an <code>ACollection</code> instance. While the returned
     *  instance itself has no mutator methods, changes to the underlying array are reflected in the wrapping
     *  <code>ACollection</code> instance.<p />
     *
     * The returned collection has list semantics with regard to <code>map()</code> and other modifying methods; duplicate
     *  values are allowed.
     */
    public static <T> AArrayWrapper<T> asArrayView(T[] c) {
        return new AArrayWrapper<>(c);
    }

    public static class ACollectionWrapper<T> extends AbstractACollection<T, ACollectionWrapper<T>> {
        private final Collection<T> inner;

        private ACollectionWrapper(Collection<T> inner) {
            this.inner = inner;
        }

        @Override protected ACollectionWrapper<T> createInternal(Collection<T> elements) {
            return new ACollectionWrapper<>(elements);
        }

        @Override
        protected AEquality equalityForEquals() {
            return AEquality.EQUALS;
        }

        @Override public int size() {
            return inner.size();
        }

        @Override public <X, E extends Exception> ACollectionWrapper<X> map(AFunction1<X, T, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.map(inner, f));
        }

        @Override public <X, E extends Exception> ACollectionWrapper<X> flatMap(AFunction1<? extends Iterable<X>, T, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.flatMap(inner, f));
        }

        @SuppressWarnings("unchecked")
        @Override public <X> ACollectionWrapper<X> flatten() {
            return new ACollectionWrapper<>(ACollectionHelper.flatten((Iterable<? extends Iterable<X>>) inner));
        }

        @SuppressWarnings("NullableProblems")
        @Override public Iterator<T> iterator() {
            return inner.iterator();
        }
    }

    public static class AArrayWrapper<T> extends AbstractACollection<T, AArrayWrapper<T>> {
        private final T[] inner;

        private AArrayWrapper(T[] inner) {
            this.inner = inner;
        }

        @SuppressWarnings("unchecked")
        @Override protected AArrayWrapper<T> createInternal(Collection<T> elements) {
            final T[] result = (T[]) Array.newInstance(inner.getClass().getComponentType(), elements.size());
            int idx = 0;
            for(T o: elements) {
                result[idx++] = o;
            }
            return new AArrayWrapper<>(result);
        }

        @Override protected AEquality equalityForEquals() {
            return AEquality.EQUALS;
        }

        @Override public int size() {
            return inner.length;
        }

        /**
         * Returns ACollectionWrapper instead of AArrayWrapper because Java can not instantiate an array for a component type that is available only as a generic parameter.
         */
        @SuppressWarnings("unchecked")
        @Override public <X> ACollectionWrapper<X> flatten() {
            return new ACollectionWrapper<>(ACollectionHelper.flatten(Arrays.asList((Iterable<X>[]) inner)));
        }

        /**
         * Returns ACollectionWrapper instead of AArrayWrapper because Java can not instantiate an array for a component type that is available only as a generic parameter.
         */
        @Override
        public <X, E extends Exception> ACollectionWrapper<X> map(AFunction1<X, T, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.map(Arrays.asList(inner), f));
        }

        /**
         * Returns ACollectionWrapper instead of AArrayWrapper because Java can not instantiate an array for a component type that is available only as a generic parameter.
         */
        @Override
        public <X, E extends Exception> ACollectionWrapper<X> flatMap(AFunction1<? extends Iterable<X>, T, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.flatMap(Arrays.asList(inner), f));
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int idx = 0;

                @Override public boolean hasNext() {
                    return idx < inner.length;
                }

                @Override public T next() {
                    final T result = inner[idx];
                    idx += 1;
                    return result;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Copies an <code>Iterable</code> into a <code>Collection</code>.
     */
    public static <T> Collection<T> asJavaUtilCollection(Iterable<T> c) { //TODO JUnit
        return asJavaUtilCollection(c.iterator());
    }

    /**
     * Copies the elements from an <code>Iterator</code> into a <code>Collection</code>.
     */
    public static <T> Collection<T> asJavaUtilCollection(Iterator<T> c) {
        final List<T> result = new ArrayList<>();

        while(c.hasNext()) {
            result.add(c.next());
        }

        return result;
    }

}
