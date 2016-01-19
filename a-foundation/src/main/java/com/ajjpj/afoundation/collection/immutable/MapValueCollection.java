package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.APartialFunction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;


/**
 * @author arno
 */
class MapValueCollection<K,V> extends AbstractACollection<V, AList<V>> { //TODO other more efficient internal representation?
    private final AMap<K,V> inner;

    public MapValueCollection (AMap<K, V> inner) {
        this.inner = inner;
    }

    @Override protected AList<V> createInternal (Collection<V> elements) {
        return AList.create (elements);
    }
    @Override public ACollection<V> clear () {
        return AList.nil();
    }
    @Override public int size () {
        return inner.size ();
    }
    @Override public boolean contains (V el) {
        for (V candidate: this) {
            if (Objects.equals (candidate, el)) {
                return true;
            }
        }
        return false;
    }
    @SuppressWarnings ("unchecked")
    @Override public <X> ACollection<X> flatten () {
        return AList.create ((Iterable<X>) ACollectionHelper.flatten ((Iterable<? extends Iterable<Object>>) this));
    }
    @Override public <X, E extends Throwable> ACollection<X> map (AFunction1<? super V, ? extends X, E> f) throws E {
        return AList.create (ACollectionHelper.map (this, f));
    }
    @SuppressWarnings ("unchecked")
    @Override public <X, E extends Throwable> ACollection<X> flatMap (AFunction1<? super V, ? extends Iterable<X>, E> f) throws E {
        return AList.create (ACollectionHelper.flatMap (this, f));
    }

    @Override public <X, E extends Throwable> ACollection<X> collect (APartialFunction<? super V, ? extends X, E> pf) throws E {
        return AList.create (ACollectionHelper.collect (this, pf));
    }

    @Override public Iterator<V> iterator () {
        return new Iterator<V> () {
            private final Iterator<AMapEntry<K,V>> iter = inner.iterator ();

            @Override public boolean hasNext () {
                return iter.hasNext ();
            }
            @Override public V next () {
                return iter.next ().getValue ();
            }
            @Override public void remove () {
                throw new UnsupportedOperationException ();
            }
        };
    }
}
