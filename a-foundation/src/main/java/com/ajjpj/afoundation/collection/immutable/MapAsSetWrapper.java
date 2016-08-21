package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;


/**
 * @author arno
 */
abstract class MapAsSetWrapper<K, C extends MapAsSetWrapper<K, C>> implements ASet<K>, Serializable {
    private final AMap<K, Boolean> inner;

    @SuppressWarnings ("unchecked")
    protected MapAsSetWrapper (AMap<K, ?> inner) {
        this.inner = (AMap<K, Boolean>) inner;
    }

    @Override public C clear () {
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

    @Override public C added (K el) {
        return with (el);
    }

    @SuppressWarnings ("unchecked")
    @Override public C with (K el) {
        return wrapAsSet (inner.updated (el, Boolean.TRUE));
    }

    @Override public C removed (K el) {
        return without (el);
    }

    @SuppressWarnings ("unchecked")
    @Override public C without (K el) {
        return wrapAsSet (inner.removed (el));
    }

    @Override public boolean contains (K el) {
        return inner.containsKey (el);
    }

    @Override public Iterator<K> iterator () {
        return new Iterator<K> () {
            final Iterator<AMapEntry<K, Boolean>> iter = inner.iterator ();

            @Override public boolean hasNext () {
                return iter.hasNext ();
            }
            @Override public K next () {
                return iter.next ().getKey ();
            }
            @Override public void remove () {
                throw new UnsupportedOperationException ();
            }
        };
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

    @Override public AList<K> toList () {
        return AList.create (this);
    }

    @Override public ASet<K> toSet () {
        return this;
    }

    @SuppressWarnings ("unchecked")
    @Override public ASet<K> toSet (AEquality equality) {
        if (equality.equals (equalityForEquals ())) {
            return this;
        }

        return AHashSet.create (equality, this);
    }

    @Override public Collection<K> asJavaUtilCollection () {
        return asJavaUtilSet ();
    }

    @Override public Set<K> asJavaUtilSet () {
        return inner.asJavaUtilMap ().keySet ();
    }

    @Override public String toString () {
        return mkString ("[", ", ", "]");
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
    protected <X> ASet<X> createInternal (Iterable<X> elements) {
        AMap result = inner.clear ();

        for (X el: elements) {
            result = result.updated (el, Boolean.TRUE);
        }
        return (ASet) wrapAsSet (result);
    }

    protected abstract C wrapAsSet (AMap<K,?> inner);

    @Override public <E extends Throwable> ASet<K> filter (APredicate<? super K, E> pred) throws E {
        return createInternal (ACollectionHelper.<K,E>filter (this, pred));
    }

    @Override public <E extends Throwable> ASet<K> filterNot (APredicate<? super K, E> pred) throws E {
        return createInternal (ACollectionHelper.<K,E>filterNot (this, pred));
    }

    @SuppressWarnings ("unchecked")
    @Override public <X> ASet<X> flatten () {
        return (ASet<X>) createInternal (ACollectionHelper.flatten ((Iterable<? extends Iterable<Object>>) this));
    }

    @SuppressWarnings ("unchecked")
    @Override public <X, E extends Throwable> AMap<X, ? extends ACollection<K>> groupBy (AFunction1<? super K, ? extends X, E> f) throws E {
        return groupBy (f, inner.keyEquality ());
    }

    @SuppressWarnings ("unchecked")
    @Override public <X, E extends Throwable> AMap<X, ? extends ACollection<K>> groupBy (AFunction1<? super K, ? extends X, E> f, AEquality keyEquality) throws E {
        AMap<X, ASet<K>> result = (AMap<X, ASet<K>>) (keyEquality.equals (inner.keyEquality ()) ? inner.clear () : AHashMap.empty (keyEquality));

        final ASet<K> emptySet = createInternal (Collections.<K>emptyList ());

        for(K o: this) {
            final X key = f.apply(o);
            final ASet<K> perKey = result
                    .get(key)
                    .getOrElse (emptySet)
                    .with (o);
            result = result.updated (key, perKey);
        }
        return result;
    }

    @Override public <E extends Throwable> void foreach(AStatement1<? super K, E> f) throws E {
        for (K el: this) {
            f.apply (el);
        }
    }

    @Override public <X, E extends Throwable> ASet<X> map (AFunction1<? super K, ? extends X, E> f) throws E {
        return ACollectionHelper.asASetView (ACollectionHelper.<K,X,E>map (this, f));
    }

    @Override public <X, E extends Throwable> ASet<X> flatMap (AFunction1<? super K, ? extends Iterable<X>, E> f) throws E {
        return ACollectionHelper.asASetView (ACollectionHelper.<K,X,E>flatMap (this, f));
    }

    @Override public <X, E extends Throwable> ASet<X> collect (APartialFunction<? super K, ? extends X, E> pf) throws E {
        return ACollectionHelper.asASetView (ACollectionHelper.<K,X,E>collect (this, pf));
    }

    @Override public <R, E extends Throwable> R foldLeft (R startValue, AFunction2<R, ? super K, R, E> f) throws E {
        return ACollectionHelper.foldLeft (this, startValue, f);
    }

    @Override public <E extends Throwable> AOption<K> find (APredicate<? super K, E> pred) throws E {
        return ACollectionHelper.find (this, pred);
    }

    @Override public <E extends Throwable> boolean forAll (APredicate<? super K, E> pred) throws E {
        return ACollectionHelper.forAll (this, pred);
    }

    @Override public <E extends Throwable> boolean exists (APredicate<? super K, E> pred) throws E {
        return ACollectionHelper.exists (this, pred);
    }
}
