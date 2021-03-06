package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.AFunction1;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;


/**
 * @author arno
 */
abstract class AWrappedMap<K,V> implements AMap<K,V>, Serializable {
    private final AMap<K,V> inner;

    AWrappedMap(AMap<K, V> inner) {
        this.inner = inner;
    }

    abstract AMap<K,V> wrap(AMap<K,V> inner);
    abstract V defaultValue(K key);

    @Override public AEquality keyEquality () {
        return inner.keyEquality ();
    }

    @Override public AMap<K, V> clear () {
        return wrap (inner.clear ());
    }

    @Override public int size() {
        return inner.size();
    }

    @Override public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override public boolean nonEmpty() {
        return inner.nonEmpty();
    }

    @Override public boolean containsKey(K key) {
        return inner.containsKey(key);
    }

    @Override public boolean containsValue(V value) {
        return inner.containsValue(value);
    }

    @Override public AOption<V> get(K key) {
        final AOption<V> innerResult = inner.get(key);
        if(innerResult == AOption.none()) {
            return AOption.some(defaultValue(key));
        }
        return innerResult;
    }

    @Override public V getRequired(K key) {
        return get(key).get();
    }

    @Override public AMap<K,V> updated(K key, V value) {
        return wrap(inner.updated(key, value));
    }

    @Override public AMap<K,V> removed(K key) {
        return wrap(inner.removed(key));
    }

    @Override public Iterator<AMapEntry<K,V>> iterator() {
        return inner.iterator();
    }

    @Override public Map<K,V> asJavaUtilMap() {
        return inner.asJavaUtilMap();
    }

    @Override public AMap<K, V> withDefaultValue(V defaultValue) {
        return inner.withDefaultValue(defaultValue);
    }

    @Override public AMap<K, V> withDefault(AFunction1<? super K, ? extends V, ? extends RuntimeException> function) {
        return inner.withDefault(function);
    }

    @Override public ASet<K> keys() {
        return inner.keys();
    }

    @Override public ACollection<V> values() {
        return inner.values();
    }

    @Override public String toString() {
        return inner.toString();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override public boolean equals(Object obj) {
        return inner.equals(obj);
    }

    @Override public int hashCode() {
        return inner.hashCode();
    }
}
