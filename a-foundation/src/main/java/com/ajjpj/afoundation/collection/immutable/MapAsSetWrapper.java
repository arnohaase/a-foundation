package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AFunction2;
import com.ajjpj.afoundation.function.APredicate;
import com.ajjpj.afoundation.function.AStatement1;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * @author arno
 */
abstract class MapAsSetWrapper<T, C extends MapAsSetWrapper<T,C>> implements ASet<T>, Serializable { //TODO second generic parameter --> concrete return type!!
    private final AMap<T,?> inner;

    protected MapAsSetWrapper (AMap<T, ?> inner) {
        this.inner = inner;
    }

    @Override public ACollection<T> clear () {
        return wrapAsSet (inner.clear ());
    }

    @Override public AEquality equalityForEquals () {
        return inner.keyEquality();
    }

    @Override public int size () {
        return inner.size ();
    }

    @Override public boolean isEmpty () {
        return inner.isEmpty ();
    }

    @Override public boolean nonEmpty () {
        return inner.nonEmpty ();
    }

    @SuppressWarnings ("unchecked")
    @Override public C added (T el) {
        return wrapAsSet (((AMap<T, Boolean>) inner).updated (el, Boolean.TRUE));
    }

    @SuppressWarnings ("unchecked")
    @Override public C removed (T el) {
        return wrapAsSet (inner.removed (el));
    }

    @Override public boolean contains (T el) {
        return inner.containsKey (el);
    }

    @Override public Iterator<T> iterator () {
        return inner.keyIterator ();
    }

    @Override public String mkString () {
        return ACollectionHelper.mkString (this);
    }

    @Override public String mkString (String separator) {
        return ACollectionHelper.mkString (this, separator);
    }

    @Override public String mkString (String prefix, String separator, String suffix) {
        return ACollectionHelper.mkString (this, prefix, separator, suffix);
    }

    @Override public AList<T> toList () {
        return AList.create (this);
    }

    @Override public ASet<T> toSet () {
        return this;
    }

    @SuppressWarnings ("unchecked")
    @Override public ASet<T> toSet (AEquality equality) {
        if (equality.equals (equalityForEquals ())) {
            return this;
        }

        return AHashSet.create (equality, this);
    }

    @Override public Collection<T> asJavaUtilCollection () {
        return asJavaUtilSet ();
    }

    @Override public Set<T> asJavaUtilSet () {
        return inner.asJavaUtilMap ().keySet ();
    }

    @Override public int hashCode() {
        return inner.hashCode();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(! (o instanceof MapAsSetWrapper)) {
            return false;
        }
        return inner.equals(((MapAsSetWrapper) o).inner);
    }

    //-------------------------------------- collection transformations

    @SuppressWarnings ("unchecked")
    protected <X> ASet<X> create (Iterable<X> elements) {
        AMap result = inner.clear ();

        for (X el: elements) {
            result = result.updated (el, Boolean.TRUE);
        }
        return (ASet) wrapAsSet (result);
    }

    protected abstract C wrapAsSet (AMap<T,?> inner);

    @Override public <E extends Exception> ACollection<T> filter (APredicate<? super T, E> pred) throws E {
        return create (ACollectionHelper.filter (this, pred));
    }

    @SuppressWarnings ("unchecked")
    @Override public <X> ACollection<X> flatten () {
        return (ACollection<X>) create (ACollectionHelper.flatten ((Iterable<? extends Iterable<Object>>) this));
    }

    @SuppressWarnings ("unchecked")
    @Override public <X, E extends Exception> AMap<X, ? extends ACollection<T>> groupBy (AFunction1<? super T, ? extends X, E> f) throws E {
        return groupBy (f, inner.keyEquality ());
    }

    @SuppressWarnings ("unchecked")
    @Override public <X, E extends Exception> AMap<X, ? extends ACollection<T>> groupBy (AFunction1<? super T, ? extends X, E> f, AEquality keyEquality) throws E {
        final Map<X, Collection<T>> raw = ACollectionHelper.groupBy (this, f);

        AMap result = keyEquality.equals (inner.keyEquality ()) ? inner.clear () : AHashMap.empty (keyEquality);
        for(Map.Entry<X, Collection<T>> entry: raw.entrySet()) {
            result = result.updated (entry.getKey(), create (entry.getValue ()));
        }
        return result;
    }

    @Override public <E extends Exception> void forEach (AStatement1<? super T, E> f) throws E {
        for (T el: this) {
            f.apply (el);
        }
    }

    @Override public <X, E extends Exception> ATraversable<X> map (AFunction1<? super T, ? extends X, E> f) throws E {
        return ACollectionHelper.asACollectionView (ACollectionHelper.map (this, f));
    }

    @Override public <X, E extends Exception> ATraversable<X> flatMap (AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
        return ACollectionHelper.asACollectionView (ACollectionHelper.flatMap (this, f));
    }

    @Override public <R, E extends Exception> R foldLeft (R startValue, AFunction2<R, ? super T, R, E> f) throws E {
        return ACollectionHelper.foldLeft (this, startValue, f);
    }

    @Override public <E extends Exception> AOption<T> find (APredicate<? super T, E> pred) throws E {
        return ACollectionHelper.find (this, pred);
    }

    @Override public <E extends Exception> boolean forAll (APredicate<? super T, E> pred) throws E {
        return ACollectionHelper.forAll (this, pred);
    }

    @Override public <E extends Exception> boolean exists (APredicate<? super T, E> pred) throws E {
        return ACollectionHelper.exists (this, pred);
    }
}
