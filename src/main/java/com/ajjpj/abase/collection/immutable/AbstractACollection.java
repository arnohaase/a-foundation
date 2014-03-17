package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.ACollectionHelper;
import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.collection.AEqualsWrapper;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;
import com.ajjpj.abase.function.APredicateNoThrow;
import com.ajjpj.abase.function.AStatement1;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * This is an abstract convenience superclass for implementing ACollection implementations.<p />
 *
 * It has two generic parameters. The first of these parameters is the element types, and the second parameter is the type
 *  of the concrete collection; it is used as a return type for some collection methods.
 *
 * This class provides default implementations for most of ACollection's methods, many of them based on ACollectionHelper
 *  calls. While these implementations work, it is up to the implementer to decide where a more specific implementation
 *  is called for to leverage internals of the actual collection class. <p />
 *
 * There are three methods that form the foundation for these generic implementations:
 * <ul>
 *     <li>size() returns the number of elements in the collection</li>
 *     <li>iterator() returns an iterator over the collection's elements</li>
 *     <li>createInternal() creates a new instance of the collection, containing a given list of elements</li>
 * </ul>
 *
 * The methods <code>flatten()</code>, <code>flatMap()</code> and <code>map()</code> are not implemented generically in this class
 *  because there is no way to provide the concrete collection class as their return type.<p />
 *
 * The <code>equals()</code> and <code>hashCode()</code> implementations are based on the <code>AEquality</code> instance provided
 *  by <code>equalityForEquals()</code>.
 *
 * @author arno
 */
public abstract class AbstractACollection<T, C extends AbstractACollection<T, C>> implements ACollection<T> {
    protected abstract C createInternal(Collection<T> elements);
    protected abstract AEquality equalityForEquals();

    @Override public boolean isEmpty() {
        return size() == 0;
    }

    @Override public boolean nonEmpty() {
        return size() > 0;
    }

    @Override public <E extends Exception> void forEach(AStatement1<? super T, E> f) throws E { //TODO junit
        for(T o: this) {
            f.apply(o);
        }
    }

    @Override public <E extends Exception> C filter(APredicate<? super T, E> pred) throws E {
        return createInternal(ACollectionHelper.filter(this, pred));
    }

    @Override public <X, E extends Exception> AMap<X, C> groupBy(AFunction1<? super T, ? extends X, E> f) throws E {
        final Map<X, Collection<T>> raw = ACollectionHelper.groupBy(this, f);

        AMap<X, C> result = AHashMap.empty();
        for(Map.Entry<X, Collection<T>> entry: raw.entrySet()) {
            result = result.updated(entry.getKey(), createInternal(entry.getValue()));
        }
        return result;
    }

    @Override public <X, E extends Exception> AMap<X, C> groupBy(AFunction1<? super T, ? extends X, E> f, AEquality keyEquality) throws E {
        final Map<AEqualsWrapper<X>, Collection<T>> raw = ACollectionHelper.groupBy(this, f, keyEquality);

        AMap<X, C> result = AHashMap.empty(keyEquality);
        for(Map.Entry<AEqualsWrapper<X>, Collection<T>> entry: raw.entrySet()) {
            result = result.updated(entry.getKey().value, createInternal(entry.getValue()));
        }
        return result;
    }

    @Override public <E extends Exception> AOption<T> find(APredicate<? super T, E> pred) throws E {
        return ACollectionHelper.find(this, pred);
    }

    @Override public <E extends Exception> boolean forAll(APredicate<? super T, E> pred) throws E {
        return ACollectionHelper.forAll(this, pred);
    }

    @Override public <E extends Exception> boolean exists(APredicate<? super T, E> pred) throws E {
        return ACollectionHelper.exists(this, pred);
    }

    @Override public AList<T> toList() {
        return AList.create(this);
    }

    @Override public AHashSet<T> toSet() {
        return AHashSet.create(this);
    }

    @Override public AHashSet<T> toSet(AEquality equality) {
        return AHashSet.create(equality, this);
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

    @Override public String toString() {
        return mkString("[", ", ", "]");
    }

    @Override public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if(size() != ((ACollection<?>)o).size()) return false;

        final Iterator<?> _this = iterator();
        final Iterator<?> _that = ((ACollection<?>)o).iterator();

        while(_this.hasNext()) {
            if(! AEquality.EQUALS.equals(_this.next(), _that.next())) {
                return false;
            }
        }
        return true;
    }

    @Override public int hashCode() {
        int result = 0;

        for(T o: this) {
            result = 31*result + (o != null ? equalityForEquals().hashCode(o) : 0);
        }

        return result;
    }

    //--------------------- java.util.Collection methods

    @Override public boolean contains(final Object o) {
        return find(new APredicateNoThrow<T>() {
            @Override public boolean apply(T candidate) {
                return equalityForEquals().equals(o, candidate);
            }
        }).isDefined();
    }

    @SuppressWarnings("NullableProblems")
    @Override public Object[] toArray() {
        final Object[] result = new Object[size()];
        int i = 0;
        for(T o: this) {
            result[i++] = o;
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override public <T1> T1[] toArray(T1[] a) {
        if(a.length < size()) {
            a = (T1[]) Array.newInstance(a.getClass().getComponentType());
        }
        int i = 0;
        for(T o: this) {
            a[i++] = (T1) o;
        }
        if(a.length > size()) {
            a[size()] = null;
        }

        return a;
    }

    @Override public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o: c) {
            if(! contains(o)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}

