package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.AFunction1;

import java.util.*;


/**
 * An immutable BTree implementation
 *
 * @author arno
 */
public abstract class ABTreeMap<K, V> implements AMap<K,V> { //TODO null as a key?
    public final ABTreeSpec spec;
    transient private Integer cachedHashcode = null; // intentionally not volatile: This class is immutable, so recalculating per thread works

    @SuppressWarnings ("unchecked")
    public static <K, V> ABTreeMap<K, V> empty (ABTreeSpec spec) {
        return new LeafNode (spec, new Object[0], new Object[0]);
    }

    ABTreeMap (ABTreeSpec spec) {
        this.spec = spec;
    }

    public abstract AOption<V> get (K key);
//    public Iterable<K> keys ();
//    public Iterable<V> values ();
//    public Iterable<K> keys (K keyMin, K keyMax);
//    public Iterable<V> values (K keyMin, K keyMax);

    @SuppressWarnings ("unchecked")
    public ABTreeMap<K,V> updated (K key, V value) {
        final UpdateResult result = _updated (key, value);

        if (result.optRight == null) {
            return result.left;
        }

        // This is the only place where the tree depth can grow.
        // The 'minimum number of children' constraint does not apply to root nodes.
        return new IndexNode (spec, new Object[] {result.separator}, new ABTreeMap[] {result.left, result.optRight});
    }

    @SuppressWarnings ("unchecked")
    public ABTreeMap<K,V> removed (K key) {
        final RemoveResult removeResult = _removed (key, null);
        if (removeResult.underflowed &&
                removeResult.newNode instanceof IndexNode &&
                ((IndexNode) removeResult.newNode).children.length == 1) {
            return ((IndexNode) removeResult.newNode).children[0];
        }
        return _removed (key, null).newNode;
    }

    abstract UpdateResult _updated (Object key, Object value);
    abstract RemoveResult _removed (Object key, Object leftSeparator);
    abstract UpdateResult merge (ABTreeMap rightNeighbour, Object separator);

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
    @Override public ACollection<V> values () {
        return new MapValueCollection<> (this);
    }
    @Override public ASet<K> keys () {
        return ABTreeSet.create (this);
    }

    @Override public AMap<K, V> clear () {
        return empty (spec);
    }
    @Override public AEquality keyEquality () {
        return new AEquality.ComparatorBased (spec.comparator);
    }

    @Override public Iterator<AMapEntry<K, V>> iterator () {
        return new BTreeIterator<> (this);
    }

    private static class BTreeIterator<K,V> implements Iterator<AMapEntry<K,V>>, AMapEntry<K,V> {
        private final Deque<Object> stack = new ArrayDeque<> ();

        private K curKey;
        private V curValue;

        public BTreeIterator (ABTreeMap root) {
            if (! root.isEmpty ()) {
                stack.push (root);
            }
        }

        @Override public K getKey () {
            return curKey;
        }
        @Override public V getValue () {
            return curValue;
        }

        @Override public boolean hasNext () {
            return !stack.isEmpty ();
        }

        @SuppressWarnings ("unchecked")
        @Override public AMapEntry<K, V> next () {
            Object next;

            while (true) {
                next = stack.pop ();

                if (next instanceof LeafNode) {
                    final LeafNode leafNode = (LeafNode) next;
                    for (int i=0; i<leafNode.size (); i++) {
                        stack.push (leafNode.values[i]); // push key and value separately to avoid creating temporary object
                        stack.push (leafNode.keys[i]);
                    }
                }
                else if (next instanceof IndexNode) {
                    final IndexNode indexNode = (IndexNode) next;
                    for (ABTreeMap child: indexNode.children) {
                        stack.push (child);
                    }
                }
                else {
                    curKey = (K) next;
                    curValue = (V) stack.pop ();
                    return this;
                }
            }
        }

        @Override public void remove () {
            throw new UnsupportedOperationException ();
        }
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
        if (! (obj instanceof ABTreeMap)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        final ABTreeMap<K,V> other = (ABTreeMap<K, V>) obj;

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

            for(AMapEntry<K,V> el: this) {
                result = result ^ (31*el.getKey ().hashCode () + el.getValue ().hashCode ());
            }

            cachedHashcode = result;
        }

        return cachedHashcode;
    }
}
