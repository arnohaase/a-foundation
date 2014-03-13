package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.ACollection;
import com.ajjpj.abase.collection.immutable.AHashSet;
import com.ajjpj.abase.collection.immutable.AList;
import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This class holds a number of useful methods that operate on all kinds of collections (or Iterables, more specifically).
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

    public static <T, E extends Exception> AOption<T> find(Iterable<T> coll, APredicate<T, E> pred) throws E { //TODO junit, use in alist etc.
        for(T o: coll) {
            if(pred.apply(o)) {
                return AOption.some(o);
            }
        }
        return AOption.none();
    }

    public static <T, E extends Exception> boolean forAll(Iterable<T> coll, APredicate<T, E> pred) throws E { //TODO junit
        for(T o: coll) {
            if(!pred.apply(o)) {
                return false;
            }
        }
        return true;
    }

    public static <T, E extends Exception> boolean exists(Iterable<T> coll, APredicate<T, E> pred) throws E { //TODO junit
        for(T o: coll) {
            if(pred.apply(o)) {
                return true;
            }
        }
        return false;
    }

    public static <T, X, E extends Exception> Collection<X> map(Collection<T> coll, AFunction1<X, T, E> f) throws E { //TODO junit, javadoc
        final List<X> result = new ArrayList<>();

        for(T o: coll) {
            result.add(f.apply(o));
        }

        return result;
    }

    public static <T, E extends Exception> Collection<T> filter(Collection<T> coll, APredicate<T, E> pred) throws E { //TODO junit
        final List<T> result = new ArrayList<>();
        for(T o: coll) {
            if(pred.apply(o)) {
                result.add(o);
            }
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
    public static <T> ACollection<T, ? extends ACollection<T, ?>> asACollectionCopy(Collection<T> c) { //TODO Junit
        return asACollectionView(asJavaUtilCollection(c));
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
    public static <T> ACollection<T, ? extends ACollection<T, ?>> asACollectionView(Collection<T> c) { //TODO junit
        return new ACollectionWrapper(c);
    }

    private static class ACollectionWrapper<T, X extends ACollection<T, X>> implements ACollection<T, X> {
        private final Collection<T> inner;

        ACollectionWrapper(Collection<T> inner) {
            this.inner = inner;
        }

        @Override public int size() {
            return inner.size();
        }

        @Override public boolean isEmpty() {
            return inner.isEmpty();
        }

        @Override public boolean nonEmpty() {
            return !inner.isEmpty();
        }

        @Override public <X1, E extends Exception> ACollection<X1, ? extends ACollection<X1, ?>> map(AFunction1<X1, T, E> f) throws E {
            return new ACollectionWrapper<>(ACollectionHelper.map(inner, f));
        }

        @SuppressWarnings("unchecked")
        @Override public <E extends Exception> X filter(APredicate<T, E> pred) throws E {
            return (X) new ACollectionWrapper(ACollectionHelper.filter(inner, pred));
        }

        @Override public <E extends Exception> AOption<T> find(APredicate<T, E> pred) throws E {
            return ACollectionHelper.find(inner, pred);
        }

        @Override public <E extends Exception> boolean forAll(APredicate<T, E> pred) throws E {
            return ACollectionHelper.forAll(inner, pred);
        }

        @Override public <E extends Exception> boolean exists(APredicate<T, E> pred) throws E {
            return ACollectionHelper.exists(inner, pred);
        }

        @Override public AList<T> toList() {
            return AList.create(inner);
        }

        @Override public AHashSet<T> toSet() {
            return AHashSet.create(inner);
        }

        @Override public AHashSet<T> toSet(AEquality equality) {
            return AHashSet.create(equality, inner);
        }

        @Override public String mkString() {
            return ACollectionHelper.mkString(this);
        }

        @Override public String mkString(String separator) {
            return ACollectionHelper.mkString(this, separator);
        }

        @Override public String mkString(String prefix, String separator, String suffix) {
            return ACollectionHelper.mkString(this, prefix, separator, suffix);
        }

        @Override public Iterator<T> iterator() {
            return inner.iterator();
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
