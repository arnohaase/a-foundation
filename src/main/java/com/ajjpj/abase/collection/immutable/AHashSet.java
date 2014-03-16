package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.ACollectionHelper;
import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.function.AFunction1;

import java.util.Collection;
import java.util.Iterator;


/**
 * This is an immutable hash set implementation. It has mutator methods that return a modified copy of the set. It
 *  does <em>not</em> implement <code>java.util.Set</code> because that interface is inherently mutable. It does however
 *  provide a method to return an immutable view implementing <code>java.util.Set</code>.<p />
 *
 * It provides uniqueness guarantees based on a configurable equalityForEquals strategy which defaults to "equals-based".
 *
 * @author arno
 */
public class AHashSet<T> extends AbstractACollection<T, AHashSet<T>> {
    private final AHashMap<T, Boolean> inner;

    private static final AHashSet<Object> emptyEquals = new AHashSet<>(AHashMap.<Object, Boolean>empty(AEquality.EQUALS));
    private static final AHashSet<Object> emptyIdentity = new AHashSet<>(AHashMap.<Object, Boolean>empty(AEquality.IDENTITY));

    /**
     * Returns an empty AHashSet instance with default (i.e. equals-based) equalityForEquals. Using a factory method instead of
     *  a constructor allows AHashSet to return a cached implementation.
     */
    @SuppressWarnings("unchecked")
    public static <T> AHashSet<T> empty() {
        return (AHashSet<T>) emptyEquals;
    }
    /**
     * Returns an empty AHashSet instance with a given equalityForEquals strategy. Using a factory method instead of
     *  a constructor allows AHashSet to return a cached implementation.
     */
    @SuppressWarnings("unchecked")
    public static <T> AHashSet<T> empty(AEquality equality) {
        if(equality == AEquality.EQUALS) return (AHashSet<T>) emptyEquals;
        if(equality == AEquality.IDENTITY) return (AHashSet<T>) emptyIdentity;
        return new AHashSet<>(AHashMap.<T, Boolean>empty(equality));
    }

    /**
     * Creates an AHashSet instance with default (i.e. equals-based) equalityForEquals that is initialized with the elements from a given collection.
     */
    public static <T> AHashSet<T> create(Iterable<T> elements) {
        return create(AEquality.EQUALS, elements);
    }
    /**
     * Creates an AHashSet instance with a given equalityForEquals strategy that is initialized with the elements from a given collection.
     */
    public static <T> AHashSet<T> create(AEquality equality, Iterable<T> elements) {
        AHashSet<T> result = empty(equality);
        for(T el: elements) {
            result = result.added(el);
        }
        return result;
    }

    /**
     * Creates an AHashSet instance with default (i.e. equals-based) equalityForEquals that is initialized with the elements from a given collection.
     */
    @SuppressWarnings("unchecked")
    public static <T> AHashSet<T> create(T... elements) {
        return create(AEquality.EQUALS, elements);
    }
    /**
     * Creates an AHashSet instance with a given equalityForEquals strategy that is initialized with the elements from a given collection.
     */
    @SuppressWarnings("unchecked")
    public static <T> AHashSet<T> create(AEquality equality, T... elements) {
        AHashSet<T> result = empty(equality);
        for(T el: elements) {
            result = result.added(el);
        }
        return result;
    }

    private AHashSet(AHashMap<T, Boolean> inner) {
        this.inner = inner;
    }

    @Override protected AHashSet<T> createInternal(Collection<T> elements) {
        return create(elements);
    }

    @Override protected AEquality equalityForEquals() {
        return inner.equality;
    }

    public int size() {
        return inner.size();
    }

    @SuppressWarnings("unchecked")
    /**
     * The parameter is of type <code>Object</code> rather than <code>T</code> to conform to the signature
     *  inherited from <code>java.util.Collection</code>. Passing a reference that is not assignable to
     *  <code>T</code> causes an exception at runtime.
     */
    @Override public boolean contains(Object el) {
        return inner.containsKey((T) el);
    }

    /**
     * This method creates and returns a new AHashSet instance that is guaranteed to contain the given new element.
     *  'Containment' is relative to the configured equalityForEquals strategy - if e.g. the set uses AEquality.IDENTITY, it
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
     *  'Containment' is relative to the configured equalityForEquals strategy - if e.g. the set uses AEquality.IDENTITY, it
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

    @Override
    public Iterator<T> iterator() {
        return asJavaUtilSet().iterator();
    }

    @Override
    public int hashCode() {
        // overridden as an optimization
        return inner.hashCode();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        // overridden as an optimization
        if(o == this) {
            return true;
        }
        if(! (o instanceof AHashSet)) {
            return false;
        }
        return inner.equals(((AHashSet) o).inner);
    }

    @Override public AHashSet<T> toSet() {
        // overridden as an optimization
        return this;
    }

    @Override public AHashSet<T> toSet(AEquality equality) {
        if(equality == inner.equality) {
            return this;
        }
        return AHashSet.<T>create(equality, this);
    }

    @Override public <X, E extends Exception> AHashSet<X> map(AFunction1<X, T, E> f) throws E {
        // list instead of set to support arbitrary equalityForEquals implementations
        return create(inner.equality, ACollectionHelper.map(this, f));
    }

    @Override public <X, E extends Exception> AHashSet<X> flatMap(AFunction1<? extends Iterable<X>, T, E> f) throws E {
        return create(inner.equality, ACollectionHelper.flatMap(this, f));
    }

    @SuppressWarnings("unchecked")
    @Override public <X> AHashSet<X> flatten() {
        return (AHashSet<X>) create(inner.equality, ACollectionHelper.flatten((Iterable<? extends Iterable<Object>>) this));
    }
}
