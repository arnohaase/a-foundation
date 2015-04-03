package com.ajjpj.afoundation.collection.immutable;

/**
 * This is an iterator for {@link ALongHashMap}s, allowing efficient access.
 *
 * @author arno
 */
public interface ALongMapIterator<V> extends AMapEntry<Long, V> {
    boolean hasNext();
    void next();

    long getLongKey();
}
