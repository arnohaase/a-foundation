package com.ajjpj.afoundation.collection.immutable;

/**
 * This interface extends {@link AMap}, adding methods that only make sense if there
 *  is an ordering on the keys.<p>
 *
 * It includes only methods that can be implemented efficiently. The 'range' methods
 *  for example return {@link java.lang.Iterable}s instead of full-blown ASortedMap
 *  instances; application code can always use the returned {@link java.lang.Iterable}
 *  to create an {@link ASet} (or {@link ACollection} because that does not even
 *  require any copying) explicitly, showing the cost involved in using source code.
 *
 * @author arno
 */
public interface ASortedMap<K,V> extends AMap<K,V> { //TODO ASortedSet
    @Override ASortedMap<K, V> updated (K key, V value);
    @Override ASortedMap<K, V> removed (K key);
    /**
     * @return the first entry of the map, i.e. the entry with the 'smallest' key relative
     *          to the underlying sort order - or {@link AOption#none()} if the map is empty.
     */
    AOption<AMapEntry<K,V>> first();

    /**
     * @return the last entry of the map, i.e. the entry with the 'greatest' key relative
     *          to the underlying sort order - or {@link AOption#none()} if the map is empty.
     */
    AOption<AMapEntry<K,V>> last();

    /**
     * @return the first entry with a key 'greater than' a given key relative to the
     *          underlying sort order, or {@link AOption#none()} if there is no such entry.
     */
    AOption<AMapEntry <K,V>> firstGreaterThan (K key);

    /**
     * @return the first entry with a key 'greater than' or equal to a given key relative to the
     *          underlying sort order, or {@link AOption#none()} if there is no such entry.
     */
    AOption<AMapEntry <K,V>> firstGreaterOrEquals (K key);

    /**
     * @return the first entry with a key 'smaller than' a given key relative to the
     *          underlying sort order, or {@link AOption#none()} if there is no such entry.
     */
    AOption<AMapEntry <K,V>> lastSmallerThan (K key);

    /**
     * @return the first entry with a key 'smaller than' or equal to a given key relative to the
     *          underlying sort order, or {@link AOption#none()} if there is no such entry.
     */
    AOption<AMapEntry <K,V>> lastSmallerOrEquals (K key);

    /**
     * @return all entries with keys in the range from {@code fromKey} to {@code toKey}, both
     *          boundaries included.
     */
    Iterable <AMapEntry <K,V>> rangeII (K fromKey, K toKey); // inclusive

    /**
     * @return all entries with keys in the range from {@code fromKey} to {@code toKey}, including
     *          the lower boundary and excluding the upper boundary.
     */
    Iterable <AMapEntry <K,V>> rangeIE (K fromKey, K toKey);

    /**
     * @return all entries with keys in the range from {@code fromKey} to {@code toKey}, excluding
     *          the lower boundary and including the upper boundary.
     */
    Iterable <AMapEntry <K,V>> rangeEI (K fromKey, K toKey);

    /**
     * @return all entries with keys in the range from {@code fromKey} to {@code toKey}, both
     *          boundaries excluded.
     */
    Iterable <AMapEntry <K,V>> rangeEE (K fromKey, K toKey);

    /**
     * @return all entries with keys greater than or equal to a given value.
     */
    Iterable <AMapEntry <K,V>> fromI (K fromKey);

    /**
     * @return all entries with keys strictly greater than to a given value.
     */
    Iterable <AMapEntry <K,V>> fromE (K fromKey);

    /**
     * @return all entries with keys smaller than or equal to a given value.
     */
    Iterable <AMapEntry <K,V>> toI (K fromKey);

    /**
     * @return all entries with keys strictly smaller than or equal to a given value.
     */
    Iterable <AMapEntry <K,V>> toE (K fromKey);
}
