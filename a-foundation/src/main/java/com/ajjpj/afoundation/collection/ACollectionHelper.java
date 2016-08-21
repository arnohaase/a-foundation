package com.ajjpj.afoundation.collection;

import com.ajjpj.afoundation.collection.immutable.ACollection;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.immutable.ASet;
import com.ajjpj.afoundation.collection.immutable.AbstractACollection;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AFunction2;
import com.ajjpj.afoundation.function.APartialFunction;
import com.ajjpj.afoundation.function.APredicate;

import java.lang.reflect.Array;
import java.util.*;


/**
 * This class consists of a number of useful methods that operate on a variety of collection types.
 *
 * @author arno
 */
public class ACollectionHelper {
    /**
     * Create a {@link java.util.Set} from an {@link Iterator}.
     */
    public static <T> Set<T> createSet (Iterator<T> elements) {
        final Set<T> result = new HashSet<> ();
        while (elements.hasNext ()) result.add (elements.next ());
        return result;
    }

    /**
     * Create a {@link java.util.Set} from an {@link java.lang.Iterable}.
     */
    public static <T> Set<T> createSet (Iterable<T> elements) {
        return createSet (elements.iterator ());
    }

    /**
     * Create a {@link java.util.Set} from an {@link Iterator}, transforming each element.
     */
    public static <T,U,E extends Throwable> Set<U> createSet (Iterator<T> elements, AFunction1<T,U,E> f) throws E {
        final Set<U> result = new HashSet<> ();
        while (elements.hasNext ()) result.add (f.apply (elements.next ()));
        return result;
    }

    /**
     * Create a {@link java.util.Set} from an {@link Iterable}, transforming each element.
     */
    public static <T,U,E extends Throwable> Set<U> createSet (Iterable<T> elements, AFunction1<T,U,E> f) throws E {
        return createSet (elements.iterator (), f);
    }

    /**
     * Returns a string representation of a collection, separating elements with a comma.
     */
    public static String mkString(Iterable<?> iterable) {
        return mkString (iterable, ", ");
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
        return result.toString ();
    }

    /**
     * Returns an element of a collection that matches a predicate, if any, or AOption.none() if there is no match.
     */
    public static <T, E extends Throwable> AOption<T> find(Iterable<T> coll, APredicate<? super T, E> pred) throws E {
        for(T o: coll) {
            if(pred.apply(o)) {
                return AOption.some(o);
            }
        }
        return AOption.none ();
    }

    /**
     * Matches a predicate against collection elements, and returns true iff it matches them all.
     */
    public static <T, E extends Throwable> boolean forAll(Iterable<T> coll, APredicate<? super T, E> pred) throws E {
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
    public static <T, E extends Throwable> boolean exists(Iterable<T> coll, APredicate<? super T, E> pred) throws E {
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
    public static <T, X, E extends Throwable> Collection<X> map(Iterable<T> coll, AFunction1<? super T, ? extends X, E> f) throws E {
        final List<X> result = new ArrayList<>();

        for(T o: coll) {
            result.add(f.apply(o));
        }

        return result;
    }

    /**
     * Applies a transformation function to all elements of a collection, creating a new collection from the results.
     */
    public static <T, X, E extends Throwable> List<X> map(List<T> coll, AFunction1<? super T, ? extends X, E> f) throws E {
        final List<X> result = createEmptyListOfType (coll, true);

        for(T o: coll) {
            result.add(f.apply(o));
        }

        return result;
    }

    /**
     * Applies a transformation function to all elements of a collection, creating a new collection from the results.
     */
    public static <T, X, E extends Throwable> Set<X> map (Set<T> coll, AFunction1<? super T, ? extends X, E> f) throws E {
        final Set<X> result = createEmptySetOfType(coll, true);

        for(T o: coll) {
            result.add(f.apply(o));
        }

        return result;
    }

    @SuppressWarnings ("unchecked")
    private static <T> List<T> createEmptyListOfType (List<?> original, boolean sameSize) {
        if (original instanceof ArrayList) {
            return sameSize ? new ArrayList<T> (original.size ()) : new ArrayList<T> ();
        }

        try {
            return original.getClass ().newInstance ();
        }
        catch (Exception e) {
            return sameSize ? new ArrayList<T> (original.size ()) : new ArrayList<T> ();
        }
    }

    @SuppressWarnings ("unchecked")
    private static <T> Set<T> createEmptySetOfType (Set<?> original, boolean sameSize) {
        if (original instanceof HashSet) {
            return sameSize ? new HashSet<T> (original.size ()) : new HashSet<T> ();
        }

        try {
            return original.getClass ().newInstance ();
        }
        catch (Exception e) {
            return sameSize ? new HashSet<T> (original.size ()) : new HashSet<T> ();
        }
    }

    /**
     * Same as <code>map()</code>, except that the transformation function returns collections and all the results are
     *  flattened into a single collection.
     */
    public static <T, X, E extends Throwable> Collection<X> flatMap(Iterable<T> coll, AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
        final List<X> result = new ArrayList<>();

        for(T o: coll) {
            for(X el: f.apply(o)) {
                result.add(el);
            }
        }

        return result;
    }

    /**
     * Same as <code>map()</code>, except that the transformation function returns collections and all the results are
     *  flattened into a single collection.
     */
    public static <T, X, E extends Throwable> List<X> flatMapList(Iterable<T> coll, AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
        final List<X> result = new ArrayList<>();

        for(T o: coll) {
            for(X el: f.apply(o)) {
                result.add(el);
            }
        }

        return result;
    }

    /**
     * Same as <code>map()</code>, except that the transformation function returns collections and all the results are
     *  flattened into a single collection.
     */
    public static <T, X, E extends Throwable> Set<X> flatMapSet(Iterable<T> coll, AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
        final Set<X> result = new HashSet<>();

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
     * Takes a collection of collections and creates a new collection from the elements, leaving out the innermost
     *  level of collection.
     */
    public static <T> List<T> flattenList(Iterable<? extends Iterable<T>> coll) {
        final List<T> result = new ArrayList<>();
        for(Iterable<T> o: coll) {
            for(T el: o) {
                result.add(el);
            }
        }
        return result;
    }

    /**
     * Takes a collection of collections and creates a new collection from the elements, leaving out the innermost
     *  level of collection.
     */
    public static <T> Set<T> flattenSet(Iterable<? extends Iterable<T>> coll) {
        final Set<T> result = new HashSet<>();
        for(Iterable<T> o: coll) {
            for(T el: o) {
                result.add(el);
            }
        }
        return result;
    }

    /**
     * Applies a transformation function to all elements of a collection, where the partial function is defined for. Creates a new collection
     *   of the transformed elements only. So the number of result elements may be less than the number of elements in the source collection.
     */
    public static <T, X, E extends Throwable> Collection<X> collect (Iterable<T> coll, APartialFunction<? super T, ? extends X, E> pf) throws E {
        final List<X> result = new ArrayList<>();

        for (T o: coll) {
            if (pf.isDefinedAt (o)) {
                result.add (pf.apply (o));
            }
        }

        return result;
    }

    /**
     * Applies a transformation function to all elements of a collection, where the partial function is defined for. Creates a new collection
     *   of the transformed elements only. So the number of result elements may be less than the number of elements in the source collection.
     */
    public static <T, X, E extends Throwable> List<X> collect (List<T> coll, APartialFunction<? super T, ? extends X, E> pf) throws E {
        final List<X> result = createEmptyListOfType (coll, true);

        for (T o: coll) {
            if (pf.isDefinedAt (o)) {
                result.add (pf.apply (o));
            }
        }

        return result;
    }

    /**
     * Applies a transformation function to all elements of a collection, where the partial function is defined for. Creates a new collection
     *   of the transformed elements only. So the number of result elements may be less than the number of elements in the source collection.
     */
    public static <T, X, E extends Throwable> Set<X> collect (Set<T> coll, APartialFunction<? super T, ? extends X, E> pf) throws E {
        final Set<X> result = createEmptySetOfType(coll, true);

        for(T o: coll) {
            if (pf.isDefinedAt (o)) {
                result.add (pf.apply (o));
            }
        }

        return result;
    }


    /**
     * Matches all elements of a collection against a predicate, creating a new collection from those that match.
     */
    public static <T, E extends Throwable> Collection<T> filter(Iterable<T> coll, APredicate<? super T, E> pred) throws E {
        final List<T> result = new ArrayList<>();
        for(T o: coll) {
            if(pred.apply(o)) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Matches all elements of a collection against a predicate, creating a new collection from those that match.
     */
    public static <T, E extends Throwable> List<T> filter(List<T> coll, APredicate<? super T, E> pred) throws E {
        final List<T> result = createEmptyListOfType (coll, false);
        for(T o: coll) {
            if(pred.apply(o)) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Matches all elements of a collection against a predicate, creating a new collection from those that match.
     */
    public static <T, E extends Throwable> Set<T> filter(Set<T> coll, APredicate<? super T, E> pred) throws E {
        final Set<T> result = createEmptySetOfType(coll, false);
        for(T o: coll) {
            if(pred.apply(o)) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Matches all elements of a collection against a predicate, creating a new collection from those that do not match.
     */
    public static <T, E extends Throwable> Collection<T> filterNot (Iterable<T> coll, APredicate<? super T, E> pred) throws E {
        final List<T> result = new ArrayList<>();
        for(T o: coll) {
            if(! pred.apply(o)) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Matches all elements of a collection against a predicate, creating a new collection from those that do not match.
     */
    public static <T, E extends Throwable> List<T> filterNot (List<T> coll, APredicate<? super T, E> pred) throws E {
        final List<T> result = createEmptyListOfType (coll, false);
        for(T o: coll) {
            if (! pred.apply(o)) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Matches all elements of a collection against a predicate, creating a new collection from those that do not match.
     */
    public static <T, E extends Throwable> Set<T> filterNot (Set<T> coll, APredicate<? super T, E> pred) throws E {
        final Set<T> result = createEmptySetOfType(coll, false);
        for(T o: coll) {
            if (! pred.apply(o)) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Creates a Map from a collection. Each element's key is determined by applying a function to the element. All
     *  elements with the same key are stored as that key's value in the returned Map.
     */
    public static <T, X, E extends Throwable> Map<X, Collection<T>> groupBy (Iterable<T> coll, AFunction1<? super T, ? extends X, E> f) throws E {
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
     *  elements with the same key are stored as that key's value in the returned Map.
     */
    public static <T, X, E extends Throwable> Map<X, List<T>> groupBy (List<T> coll, AFunction1<? super T, ? extends X, E> f) throws E {
        final Map<X, List<T>> result = new HashMap<>();
        for(T o: coll) {
            final X key = f.apply(o);
            List<T> perKey = result.get(key);
            if(perKey == null) {
                perKey = createEmptyListOfType (coll, false);
                result.put(key, perKey);
            }
            perKey.add(o);
        }
        return result;
    }

    /**
     * Creates a Map from a collection. Each element's key is determined by applying a function to the element. All
     *  elements with the same key are stored as that key's value in the returned Map.
     */
    public static <T, X, E extends Throwable> Map<X, Set<T>> groupBy (Set<T> coll, AFunction1<? super T, ? extends X, E> f) throws E {
        final Map<X, Set<T>> result = new HashMap<>();
        for(T o: coll) {
            final X key = f.apply(o);
            Set<T> perKey = result.get(key);
            if(perKey == null) {
                perKey = createEmptySetOfType(coll, false);
                result.put(key, perKey);
            }
            perKey.add(o);
        }
        return result;
    }

    /**
     * Creates a Map from a collection. Each element's key is determined by applying a function to the element. All
     *  elements with the same key are stored as that key's value in the returned Map.<p>
     *
     * This method gives control over the equalityForEquals strategy used to determine if two keys are 'equal'. To accomodate that,
     *  the keys are wrapped in AEqualsWrapper. <p>
     *
     * This method is rather technical in nature, and it is probably more useful as a foundation for generic code than
     *  for direct use by applications.
     */
    public static <T, X, E extends Throwable> Map<AEqualsWrapper<X>, Collection<T>> groupBy(Iterable<T> coll, AFunction1<? super T, ? extends X, E> f, AEquality keyEquality) throws E {
        final Map<AEqualsWrapper<X>, Collection<T>> result = new HashMap<>();
        for(T o: coll) {
            final AEqualsWrapper<X> key = new AEqualsWrapper<>(keyEquality, f.apply(o));
            Collection<T> perKey = result.get(key);
            if(perKey == null) {
                perKey = new ArrayList<>();
                result.put (key, perKey);
            }
            perKey.add(o);
        }
        return result;
    }

    /**
     * Creates a Map from a collection. Each element's key is determined by applying a function to the element. All
     *  elements with the same key are stored as that key's value in the returned Map.<p>
     *
     * This method gives control over the equalityForEquals strategy used to determine if two keys are 'equal'. To accomodate that,
     *  the keys are wrapped in AEqualsWrapper. <p>
     *
     * This method is rather technical in nature, and it is probably more useful as a foundation for generic code than
     *  for direct use by applications.
     */
    public static <T, X, E extends Throwable> Map<AEqualsWrapper<X>, List<T>> groupBy(List<T> coll, AFunction1<? super T, ? extends X, E> f, AEquality keyEquality) throws E {
        final Map<AEqualsWrapper<X>, List<T>> result = new HashMap<>();
        for(T o: coll) {
            final AEqualsWrapper<X> key = new AEqualsWrapper<>(keyEquality, f.apply(o));
            List<T> perKey = result.get(key);
            if(perKey == null) {
                perKey = createEmptyListOfType (coll, false);
                result.put (key, perKey);
            }
            perKey.add(o);
        }
        return result;
    }

    /**
     * Creates a Map from a collection. Each element's key is determined by applying a function to the element. All
     *  elements with the same key are stored as that key's value in the returned Map.<p>
     *
     * This method gives control over the equalityForEquals strategy used to determine if two keys are 'equal'. To accomodate that,
     *  the keys are wrapped in AEqualsWrapper. <p>
     *
     * This method is rather technical in nature, and it is probably more useful as a foundation for generic code than
     *  for direct use by applications.
     */
    public static <T, X, E extends Throwable> Map<AEqualsWrapper<X>, Set<T>> groupBy (Set<T> coll, AFunction1<? super T, ? extends X, E> f, AEquality keyEquality) throws E {
        final Map<AEqualsWrapper<X>, Set<T>> result = new HashMap<>();
        for(T o: coll) {
            final AEqualsWrapper<X> key = new AEqualsWrapper<>(keyEquality, f.apply(o));
            Set<T> perKey = result.get(key);
            if(perKey == null) {
                perKey = createEmptySetOfType(coll, false);
                result.put(key, perKey);
            }
            perKey.add(o);
        }
        return result;
    }

    /**
     * Applies a binary operator to a start value and all elements of this sequence, going left to right.
     *
     * @param <T> element type of the collection
     * @param <R> result type
     */
    public static <T, R, E extends Throwable> R foldLeft (Iterable<T> coll, R startValue, AFunction2<R, ? super T, R, E> f) throws E {
        R result = startValue;

        for (T e: coll) {
            result = f.apply (result, e);
        }

        return result;
    }

    /**
     * Applies a binary operator to a start value and all elements of this list, going left to right.
     *
     * @param <T> element type of the collection
     * @param <R> result type
     *
     */
    public static <T, R, E extends Throwable> R foldRight (List<T> coll, R startValue, AFunction2<R, ? super T, R, E> f) throws E {
        R result = startValue;

        ListIterator<T> i = coll.listIterator(coll.size());
        while ( i.hasPrevious() ) {
            result = f.apply (result, i.previous());
        }

        return result;
    }


    /**
     * Copies the content of an <code>Iterable</code> into an (immutable) <code>ACollection</code> instance. Subsequent
     *  changes to the underlying collection have no effect on the returned <code>ACollection</code> instance.<p>
     *
     * The returned collection has list semantics with regard to <code>map()</code> and other modifying methods;
     *  duplicate values are allowed.
     */
    public static <T> ACollectionWrapper<T> asACollectionCopy(Collection<T> c) {
        return asACollectionView(new ArrayList<>(c));
    }

    /**
     * Wraps the content of a <code>java.util.Collection</code> in an <code>ACollection</code> instance. While the returned
     *  instance itself has no mutator methods, changes to the underlying collection are reflected in the wrapping
     *  <code>ACollection</code> instance.<p>
     *
     * The returned collection has list semantics with regard to <code>map()</code> and other modifying methods; duplicate
     *  values are allowed.
     */
    @SuppressWarnings("unchecked")
    public static <T> ACollectionWrapper<T> asACollectionView(Collection<T> c) {
        return new ACollectionWrapper(c);
    }

    /**
     * Wraps the content of a <code>java.util.Set</code> in an <code>ASet</code> instance. While the returned
     *  instance itself has no mutator methods, changes to the underlying collection are reflected in the wrapping
     *  <code>ASet</code> instance.<p>
     *
     * The returned collection has set semantics with regard to <code>map()</code> and other modifying methods; duplicate
     *  values are removed.
     */
    public static <T> ASetWrapper<T> asASetView (Collection<T> c) {
        if (c instanceof Set) {
            return new ASetWrapper<> ((Set<T>) c);
        }

        return new ASetWrapper<> (new HashSet<> (c));
    }

    /**
     * Copies the content of an array into an (immutable) <code>ACollection</code> instance. Subsequent
     *  changes to the underlying array have no effect on the returned <code>ACollection</code> instance.<p>
     *
     * The returned collection has list semantics with regard to <code>map()</code> and other modifying methods;
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
     *  <code>ACollection</code> instance.<p>
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

        @Override protected AEquality equalityForEquals() {
            return AEquality.EQUALS;
        }

        @Override public ACollection<T> clear () {
            throw new UnsupportedOperationException ();
        }

        @Override public boolean contains (T el) {
            for (T e: inner) {
                if (Objects.equals (e, el)) {
                    return true;
                }
            }
            return false;
        }

        @Override public int size() {
            return inner.size();
        }

        @Override public <X, E extends Throwable> ACollection<X> map(AFunction1<? super T, ? extends X, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.<T,X,E>map(inner, f));
        }

        @Override public <X, E extends Throwable> ACollection<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.<T,X,E>flatMap (inner, f));
        }

        @SuppressWarnings("unchecked")
        @Override public <X> ACollection<X> flatten() {
            return new ACollectionWrapper<>(ACollectionHelper.flatten ((Iterable<? extends Iterable<X>>) inner));
        }

        @Override public <X, E extends Throwable> ACollection<X> collect (APartialFunction<? super T, ? extends X, E> pf) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.<T,X,E>collect (inner, pf));
        }

        @Override public <R, E extends Throwable> R foldLeft (R startValue, AFunction2<R, ? super T, R, E> f) throws E {
            return ACollectionHelper.foldLeft (inner, startValue, f);
        }

        @SuppressWarnings("NullableProblems")
        @Override public Iterator<T> iterator() {
            return inner.iterator();
        }
    }

    public static class ASetWrapper<T> extends AbstractACollection<T, ASetWrapper<T>> implements ASet<T> {
        private final Set<T> inner;

        public ASetWrapper (Set<T> inner) {
            this.inner = inner;
        }

        @Override public Set<T> asJavaUtilSet () {
            return inner;
        }

        @Override protected ASetWrapper<T> createInternal (Collection<T> elements) {
            return new ASetWrapper<> (new HashSet <> (elements));
        }
        @Override public ACollection<T> clear () {
            return createInternal (new HashSet<T> ());
        }
        @Override public int size () {
            return inner.size ();
        }
        @Override public boolean contains (T el) {
            return inner.contains (el);
        }
        @Override public Iterator<T> iterator () {
            return inner.iterator ();
        }

        @Override public AEquality equalityForEquals () {
            return AEquality.EQUALS;
        }
        @Override public ASet<T> with (T el) {
            throw new UnsupportedOperationException ("adding and removing elements is not supported for views - create a full-blown AHashSet instead");
        }

        @Override public ASet<T> without (T el) {
            throw new UnsupportedOperationException ("adding and removing elements is not supported for views - create a full-blown AHashSet instead");
        }

        @Override public <X, E extends Throwable> ASet<X> map (AFunction1<? super T, ? extends X, E> f) throws E {
            return new ASetWrapper<> (ACollectionHelper.<T,X,E>map (inner, f));
        }

        @Override public <X, E extends Throwable> ASet<X> flatMap (AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
            return new ASetWrapper<> (new HashSet<> (ACollectionHelper.<T,X,E>flatMap (inner, f)));
        }

        @Override public <X, E extends Throwable> ASet<X> collect (APartialFunction<? super T, ? extends X, E> pf) throws E {
            return new ASetWrapper<> (ACollectionHelper.<T,X,E>collect (inner, pf));
        }

        @SuppressWarnings ("unchecked")
        @Override public <X> ASet<X> flatten () {
            return new ASetWrapper<>(new HashSet<> (ACollectionHelper.flatten ((Iterable<? extends Iterable<X>>) inner)));
        }

        @Override public <E extends Throwable> ASetWrapper<T> filter (APredicate<? super T, E> pred) throws E {
            return new ASetWrapper<> (ACollectionHelper.<T,E>filter (inner, pred));
        }

        @Override public <E extends Throwable> ASetWrapper<T> filterNot (APredicate<? super T, E> pred) throws E {
            return new ASetWrapper<> (ACollectionHelper.<T,E>filterNot (inner, pred));
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

        @Override public ACollection<T> clear () {
            throw new UnsupportedOperationException ();
        }

        @Override public boolean contains (T el) {
            for (T e: inner) {
                if (Objects.equals (e, el)) {
                    return true;
                }
            }
            return false;
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
        @Override public <X, E extends Throwable> ACollectionWrapper<X> map(AFunction1<? super T, ? extends X, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.<T,X,E>map (Arrays.asList (inner), f));
        }

        /**
         * Returns ACollectionWrapper instead of AArrayWrapper because Java can not instantiate an array for a component type that is available only as a generic parameter.
         */
        @Override public <X, E extends Throwable> ACollectionWrapper<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.<T,X,E>flatMap (Arrays.asList (inner), f));
        }

        @Override public <X, E extends Throwable> ACollection<X> collect (APartialFunction<? super T, ? extends X, E> pf) throws E {
            return new ACollectionWrapper<> (ACollectionHelper.<T,X,E>collect (Arrays.asList (inner), pf));
        }

        @Override public <R, E extends Throwable> R foldLeft (R startValue, AFunction2<R, ? super T, R, E> f) throws E {
            return ACollectionHelper.foldLeft (Arrays.asList (inner), startValue, f);
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
     * Returns a <code>Collection</code> with the exact same elements as an <code>Iterable</code>, copying only if the parameter is not a collection.
     */
    public static <T> List<T> asJavaUtilList(Iterable<T> c) {
        if(c instanceof List) {
            return (List<T>) c;
        }

        return asJavaUtilCollection(c.iterator());

    }
    /**
     * Returns a <code>Collection</code> with the exact same elements as an <code>Iterable</code>, copying only if the parameter is not a collection.
     */
    public static <T> Collection<T> asJavaUtilCollection(Iterable<T> c) {
        if(c instanceof Collection) {
            return (Collection<T>) c;
        }

        return asJavaUtilCollection(c.iterator());
    }

    /**
     * Copies the elements from an <code>Iterator</code> into a <code>Collection</code>.
     */
    public static <T> List<T> asJavaUtilCollection(Iterator<T> c) {
        final List<T> result = new ArrayList<>();

        while(c.hasNext()) {
            result.add(c.next());
        }

        return result;
    }
}
