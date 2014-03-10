package com.ajjpj.abase.collection;

import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.util.AUnchecker;

import java.util.Iterator;
import java.util.Map;

/**
 * @author arno
 */
class AMapWithDefault<K,V> implements AMap<K,V> {
    private final AMap<K,V> inner;
    private final AFunction1<V,K,? extends Exception> defaultFuncction;

    AMapWithDefault(AMap<K, V> inner, AFunction1<V,K,? extends Exception> defaultFunction) {
        this.inner = inner;
        this.defaultFuncction = defaultFunction;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean nonEmpty() {
        return inner.nonEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return inner.containsValue(value);
    }

    @Override
    public AOption<V> get(K key) {
        final AOption<V> innerResult = inner.get(key);
        if(innerResult == AOption.none()) {
            try {
                return AOption.some(defaultFuncction.apply(key));
            } catch (Exception e) {
                AUnchecker.throwUnchecked(e);
            }
        }
        return innerResult;
    }

    @Override
    public V getRequired(K key) {
        if(containsKey(key)) {
            return inner.getRequired(key);
        }
        try {
            return defaultFuncction.apply(key);
        } catch (Exception e) {
            AUnchecker.throwUnchecked(e);
            return null; // for the compiler
        }
    }

    @Override
    public AMap<K,V> updated(K key, V value) {
        return inner.updated(key, value);
    }

    @Override
    public AMap<K,V> removed(K key) {
        return inner.removed(key);
    }

    @Override
    public Iterator<APair<K,V>> iterator() {
        return inner.iterator();
    }

    @Override
    public Map<K,V> asJavaUtilMap() {
        return inner.asJavaUtilMap();
    }

    @Override
    public AMap<K, V> withDefaultValue(V defaultValue) {
        return inner.withDefaultValue(defaultValue);
    }

    @Override
    public <E extends Exception> AMap<K, V> withDefault(AFunction1<V, K, E> function) {
        return inner.withDefault(function);
    }

    @Override
    public Iterable<K> keys() {
        return inner.keys();
    }

    @Override
    public Iterable<V> values() {
        return inner.values();
    }

    @Override
    public String toString() {
        return inner.toString();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return inner.equals(obj);
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }
}
