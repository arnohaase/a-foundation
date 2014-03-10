package com.ajjpj.abase.collection;


import com.ajjpj.abase.function.AFunction1;

import java.util.Iterator;

/**
 * @author arno
 */
public interface AMap<K,V> extends Iterable<APair<K,V>> {
    int size();
    boolean isEmpty();
    boolean nonEmpty();

    boolean containsKey(K key);
    boolean containsValue(V value);
    AOption<V> get(K key);

    /**
     * This is the equivalent of calling get(...).get(); implementations throw
     * a NoSuchElementException if there is not entry for the key
     */
    V getRequired(K key);

    Iterable<K> keys();
    Iterable<V> values();

    AMap<K,V> updated(K key, V value);
    AMap<K,V> removed(K key);

    @Override
    Iterator<APair<K,V>> iterator();

    java.util.Map<K,V> asJavaUtilMap();

    AMap<K,V> withDefaultValue(V defaultValue);
    <E extends Exception> AMap<K,V> withDefault(AFunction1<V,K,E> function);
}
