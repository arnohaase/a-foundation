package com.ajjpj.afoundation.collection.immutable;

/**
 * This interface encapsulates an entry of an AMap, i.e. a key-value pair.
 *
 * @author arno
 */
public interface AMapEntry<K,V> {
    K getKey();
    V getValue();
}
