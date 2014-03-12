package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.ACollectionHelper;
import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.collection.AOption;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This is an immutable hash set implementation. It has mutator methods that return a modified copy of the set. It
 *  does <em>not</em> implement <code>java.util.Set</code> because that interface is inherently mutable. It does however
 *  provide a method to return an immutable view implementing <code>java.util.Set</code>.<p />
 *
 * It provides uniqueness guarantees based on a configurable equality strategy which defaults to "equals-based".
 *
 * @author arno
 */
public class AHashSet<T> implements Iterable<T> {
    private final AHashMap<T, Boolean> inner;

    private static final AHashSet<Object> emptyEquals = new AHashSet<>(AHashMap.<Object, Boolean>empty(AEquality.EQUALS));
    private static final AHashSet<Object> emptyIdentity = new AHashSet<>(AHashMap.<Object, Boolean>empty(AEquality.IDENTITY));

    /**
     * Returns an empty AHashSet instance with default (i.e. equals-based) equality. Using a factory method instead of
     *  a constructor allows AHashSet to return a cached implementation.
     */
    @SuppressWarnings("unchecked")
    public static <T> AHashSet<T> empty() {
        return (AHashSet<T>) emptyEquals;
    }
    /**
     * Returns an empty AHashSet instance with a given equality strategy. Using a factory method instead of
     *  a constructor allows AHashSet to return a cached implementation.
     */
    @SuppressWarnings("unchecked")
    public static <T> AHashSet<T> empty(AEquality equality) {
        if(equality == AEquality.EQUALS) return (AHashSet<T>) emptyEquals;
        if(equality == AEquality.IDENTITY) return (AHashSet<T>) emptyIdentity;
        return new AHashSet<>(AHashMap.<T, Boolean>empty(equality));
    }

    /**
     * Creates an AHashSet instance with default (i.e. equals-based) equality that is initialized with the elements from a given collection.
     */
    public static <T> AHashSet<T> create(Iterable<T> elements) {
        return create(AEquality.EQUALS, elements);
    }
    /**
     * Creates an AHashSet instance with a given equality strategy that is initialized with the elements from a given collection.
     */
    public static <T> AHashSet<T> create(AEquality equality, Iterable<T> elements) {
        AHashSet<T> result = empty(equality);
        for(T el: elements) {
            result = result.added(el);
        }
        return result;
    }

    private AHashSet(AHashMap<T, Boolean> inner) {
        this.inner = inner;
    }

    public int size() {
        return inner.size();
    }
    public boolean isEmpty() {
        return inner.isEmpty();
    }
    public boolean nonEmpty() {
        return inner.nonEmpty();
    }

    public boolean contains(T el) {
        return inner.containsKey(el);
    }

    /**
     * This method creates and returns a new AHashSet instance that is guaranteed to contain the given new element.
     *  'Containment' is relative to the configured equality strategy - if e.g. the set uses AEquality.IDENTITY, it
     *   can contain two objects that are equal without being the same.<p />
     *
     * This is the only method to add elements to the set.
     */
    public AHashSet<T> added(T el) {
        final AHashMap<T, Boolean> newInner = inner.updated(el, true);
        if(newInner == inner) {
            return this;
        }

        return new AHashSet<> (newInner);
    }

    /**
     * This method creates and returns a new AHashSet instance that is guaranteed not to contain the given element.
     *  'Containment' is relative to the configured equality strategy - if e.g. the set uses AEquality.IDENTITY, it
     *  might not remove an element that is equal to the object that is passed to the <code>removed()</code> method
     *  if they are not the same.<p />
     *
     * This is the only method to remove elements from the set.
     */
    public AHashSet<T> removed(T el) {
        final AHashMap<T, Boolean> newInner = inner.removed(el);
        if(newInner == inner) {
            return this;
        }
        return new AHashSet<> (newInner);
    }

    /**
     * Returns a read-only <code>java.util.Set</code> view of this set. The view is read-through, and creation has
     *  constant time complexity.
     */
    public java.util.Set<T> asJavaUtilSet() {
        return inner.asJavaUtilMap().keySet();
    }

    /**
     * Returns a <code>java.util.Iterator</code> through the set's elements, allowing the set to be used with Java's
     *  <code>for(...: set)</code> syntax introduced with version 1.5.
     */
    @Override
    public Iterator<T> iterator() {
        return asJavaUtilSet().iterator();
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(! (o instanceof AHashSet)) {
            return false;
        }
        return inner.equals(((AHashSet)o).inner);
    }

    /**
     * Creates an AList with this set's elements
     */
    public AList<T> toList() {
        return AList.create(this);
    }

    /**
     * Returns a string representation of this AList, placing <code>prefix</code> before the first element and
     *  <code>suffix</code> after the last element. The <code>separator</code> is placed between elements.
     */
    public String mkString(String prefix, String separator, String suffix) {
        return ACollectionHelper.mkString(this, prefix, separator, suffix);
    }

    /**
     * Returns a string representation of this AList, without prefix or suffix.
     */
    public String mkString(String separator) {
        return ACollectionHelper.mkString(this, separator);
    }

    /**
     * Searches through this AHashSet's elements and returns the first element matching a given predicate. if any.
     */
    public <E extends Exception> AOption<T> find(APredicate<T, E> pred) throws E {
        for(T el: this) {
            if(pred.apply(el)) {
                return AOption.some(el);
            }
        }
        return AOption.none();
    }

    /**
     * Applies a transformation function to each element, creating a new AHashSet instance from the results. For an AHashSet
     *  of strings, this could e.g. be used to create an AHashSet of integer values with each string's length.
     */
    public <X, E extends Exception> AHashSet<X> map(AFunction1<X, T, E> f) throws E {
        final List<X> result = new ArrayList<>(); // list instead of set to support arbitrary equality implementations
        for(T el: this) {
            result.add(f.apply(el));
        }
        return create(inner.equality, result);
    }

    /**
     * Filters this AHashSet's elements, this method returns a new AList comprised of only those elements that match
     *  a given predicate.
     */
    public <E extends Exception> AHashSet<T> filter(APredicate<T, E> pred) throws E {
        final List<T> result = new ArrayList<>();
        for(T el: this) {
            if(pred.apply(el)) {
                result.add(el);
            }
        }
        return create(inner.equality, result);
    }
}
