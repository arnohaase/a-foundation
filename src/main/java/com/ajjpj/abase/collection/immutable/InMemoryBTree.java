package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.tuples.ATuple2;
import com.ajjpj.abase.function.AFunction1;

import java.util.*;


/**
 * An immutable BTree implementation
 *
 * @author arno
 */
public abstract class InMemoryBTree<K, V> implements AMap<K,V> { //TODO null as a key?
    public final BTreeSpec spec;
    transient private Integer cachedHashcode = null; // intentionally not volatile: This class is immutable, so recalculating per thread works

    @SuppressWarnings ("unchecked")
    public static <K, V> InMemoryBTree<K, V> empty (BTreeSpec spec) {
        return new LeafNode (spec, new Object[0], new Object[0]);
    }

    InMemoryBTree (BTreeSpec spec) {
        this.spec = spec;
    }

    public abstract AOption<V> get (K key);
//    public Iterable<K> keys ();
//    public Iterable<V> values ();
//    public Iterable<K> keys (K keyMin, K keyMax);
//    public Iterable<V> values (K keyMin, K keyMax);

    @SuppressWarnings ("unchecked")
    public InMemoryBTree<K,V> updated (K key, V value) {
        final UpdateResult result = _updated (key, value);

        if (result.optRight == null) {
            return result.left;
        }

        // This is the only place where the tree depth can grow.
        // The 'minimum number of children' constraint does not apply to root nodes.
        return new IndexNode (spec, new Object[] {result.separator}, new InMemoryBTree[] {result.left, result.optRight});
    }

    @SuppressWarnings ("unchecked")
    public InMemoryBTree<K,V> removed (K key) {
        return _removed (key, null).newNode;
    }

    abstract UpdateResult _updated (Object key, Object value);
    abstract RemoveResult _removed (Object key, Object leftSeparator);
    abstract UpdateResult merge (InMemoryBTree rightNeighbour, Object separator);

    @Override public boolean nonEmpty () {
        return ! isEmpty ();
    }
    @Override public boolean containsKey (K key) {
        return get (key).isDefined ();
    }
    @Override public boolean containsValue (V value) {
        for (V candidate: values ()) {
            if (Objects.equals (candidate, value)) {
                return true;
            }
        }
        return false;
    }
    @Override public V getRequired (K key) {
        return get (key).get ();
    }
    @Override public Collection<V> values () {
        return new AbstractCollection<V> () {
            @Override public Iterator<V> iterator () {
                return new Iterator<V> () {
                    final Iterator<K> keyIter = keys ().iterator ();

                    @Override public boolean hasNext () {
                        return keyIter.hasNext ();
                    }
                    @Override public V next () {
                        return getRequired (keyIter.next ());
                    }
                    @Override public void remove () {
                        throw new UnsupportedOperationException ();
                    }
                };
            }
            @Override public int size () {
                return InMemoryBTree.this.size ();
            }
        };
    }
    @Override public Iterator<ATuple2<K, V>> iterator () {
        return new Iterator<ATuple2<K, V>> () {
            final Iterator<K> keyIter = keys ().iterator ();

            @Override public boolean hasNext () {
                return keyIter.hasNext ();
            }
            @Override public ATuple2<K, V> next () {
                final K key = keyIter.next ();
                return new ATuple2<> (key, getRequired (key));
            }
            @Override public void remove () {
                throw new UnsupportedOperationException ();
            }
        };
    }

    @Override public Map<K, V> asJavaUtilMap () {
        return new JavaUtilMapWrapper<> (this);
    }

    @Override public AMap<K, V> withDefaultValue (V defaultValue) {
        return new AMapWithDefaultValue<> (this, defaultValue);
    }

    @Override public AMap<K, V> withDefault (AFunction1<? super K, ? extends V, ? extends RuntimeException> function) {
        return new AMapWithDefault<> (this, function);
    }

    @SuppressWarnings ("unchecked")
    @Override public boolean equals (Object obj) {
        if (! (obj instanceof InMemoryBTree)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        final InMemoryBTree<K,V> other = (InMemoryBTree<K, V>) obj;

        final Iterator<K> keyIter = keys ().iterator ();
        final Iterator<K> keyIter2 = other.keys ().iterator ();

        while (keyIter.hasNext ()) {
            if (!keyIter2.hasNext ()) {
                return false;
            }

            final K key = keyIter.next ();
            final K key2 = keyIter2.next ();

            if (spec.comparator.compare (key, key2) != 0) {
                return false;
            }

            if (! Objects.equals (getRequired (key), other.getRequired (key2))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if(cachedHashcode == null) {
            int result = 0;

            for(ATuple2<K,V> el: this) {
                result = result ^ (31*el._1.hashCode() + el._2.hashCode ());
            }

            cachedHashcode = result;
        }

        return cachedHashcode;
    }
}
