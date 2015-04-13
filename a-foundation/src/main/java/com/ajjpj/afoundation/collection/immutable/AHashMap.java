package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.AFunction1;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;


/**
 * This is an immutable hash map based on 32-way hash tries. Its implementation is optimized to minimize copying when
 *  the map is modified.<p>
 *
 * The code in this class is essentially a port of the HashMap class from the Scala standard library. Thank you for
 *  the excellent code, Scala team!
 *
 * @author arno
 */
public class AHashMap<K, V> extends AbstractAMap<K,V> {
    private static final int LEVEL_INCREMENT = 5;
    private static final AEquality DEFAULT_EQUALITY = AEquality.EQUALS;

    private static final AHashMap<Object, Object> emptyEquals = new AHashMap<>(AEquality.EQUALS);
    private static final AHashMap<Object, Object> emptyIdentity = new AHashMap<>(AEquality.IDENTITY);

    final AEquality equality;

    /**
     * Returns an empty AHashMap instance with default (i.e. equals-based) equalityForEquals. Calling this factory method instead
     *  of the constructor allows internal reuse of empty map instances since they are immutable.
     */
    public static <K,V> AHashMap<K,V> empty() {
        return empty(DEFAULT_EQUALITY);
    }
    /**
     * Returns an empty AHashMap instance with the given equalityForEquals strategy. Calling this factory method instead of
     *  the constructor allows internal reuse of empty map instances since they are immutable.
     */
    @SuppressWarnings("unchecked")
    public static <K,V> AHashMap<K,V> empty(AEquality equality) {
        // for typical equalityForEquals implementations, return pre-instantiated objects
        if(equality == AEquality.EQUALS) return (AHashMap<K, V>) emptyEquals;
        if(equality == AEquality.IDENTITY) return (AHashMap<K, V>) emptyIdentity;
        return new AHashMap<>(equality);
    }

    /**
     * Returns an AHashMap instance with default (i.e. equals-based) equalityForEquals, initializing it from the contents of
     *  a given <code>java.util.Map</code>.
     */
    @SuppressWarnings("unused")
    public static <K,V> AHashMap<K,V> fromJavaUtilMap(Map<K,V> map) {
        return fromJavaUtilMap(DEFAULT_EQUALITY, map);
    }
    /**
     * Returns an AHashMap instance for a given equalityForEquals, initializing it from the contents of a given
     *  <code>java.util.Map</code>.
     */
    public static <K,V> AHashMap<K,V> fromJavaUtilMap(AEquality equality, Map<K,V> map) {
        AHashMap<K,V> result = empty(equality);

        for(Map.Entry<K,V> entry: map.entrySet()) {
            result = result.updated(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Returns an AHashMap instance with default (i.e. equals-based) equalityForEquals, initializing it from separate 'keys'
     *  and 'values' collections. Both collections are iterated exactly once, and are expected to have the same size.
     */
    public static <K,V> AHashMap<K,V> fromKeysAndValues(Iterable<K> keys, Iterable<V> values) {
        return fromKeysAndValues(DEFAULT_EQUALITY, keys, values);
    }
    /**
     * Returns an AHashMap instance with a given equalityForEquals, initializing it from separate 'keys'
     *  and 'values' collections. Both collections are iterated exactly once, and are expected to have the same size.
     */
    public static <K,V> AHashMap<K,V> fromKeysAndValues(AEquality equality, Iterable<K> keys, Iterable<V> values) {
        final Iterator<K> ki = keys.iterator();
        final Iterator<V> vi = values.iterator();

        AHashMap<K,V> result = empty(equality);

        while(ki.hasNext()) {
            final K key = ki.next();
            final V value = vi.next();

            result = result.updated(key, value);
        }
        return result;
    }

    /**
     * Returns an AHashMap instance with default (i.e. equals-based) equalityForEquals, initializing it from a collection of
     *  keys and a function. For each element of the <code>keys</code> collection, the function is called once to
     *  determine the corresponding value, and the pair is then stored in the map.
     */
    @SuppressWarnings("unused")
    public static <K,V, E extends Exception> AHashMap<K,V> fromKeysAndFunction(Iterable<K> keys, AFunction1<? super K, ? extends V, E> f) throws E {
        return fromKeysAndFunction(DEFAULT_EQUALITY, keys, f);
    }
    /**
     * Returns an AHashMap instance with a given equalityForEquals, initializing it from a collection of
     *  keys and a function. For each element of the <code>keys</code> collection, the function is called once to
     *  determine the corresponding value, and the pair is then stored in the map.
     */
    public static <K,V, E extends Exception> AHashMap<K,V> fromKeysAndFunction(AEquality equality, Iterable<K> keys, AFunction1<? super K, ? extends V, E> f) throws E {
        final Iterator<K> ki = keys.iterator();

        AHashMap<K,V> result = AHashMap.empty(equality);

        while(ki.hasNext()) {
            final K key = ki.next();
            final V value = f.apply(key);

            result = result.updated(key, value);
        }
        return result;
    }

    private AHashMap(AEquality equality) {
        this.equality = equality;
    }

    @Override public AEquality keyEquality () {
        return equality;
    }

    @Override public AMap<K, V> clear () {
        return empty (equality);
    }

    @Override public int size() {
        return 0;
    }

    @Override public AOption<V> get(K key) {
        return doGet (key, computeHash (key, equality), 0);
    }

    @Override public AHashMap<K,V> updated(K key, V value) {
        return doUpdated(key, computeHash(key, equality), 0, value);
    }

    @Override public AHashMap<K,V> removed(K key) {
        return doRemoved(key, computeHash(key, equality), 0);
    }

    @Override public Iterator<AMapEntry<K, V>> iterator() {
        return new HashMapIterator<> (this);
    }

    static class HashMapIterator<K,V> implements Iterator<AMapEntry<K,V>> {
        private final Deque<Object> stack = new ArrayDeque<> ();

        public HashMapIterator (AHashMap<K,V> root) {
            if (! root.isEmpty ()) {
                if (root instanceof HashMapCollision1) {
                    pushCollision ((HashMapCollision1<K, V>) root);
                }
                else {
                    stack.push (root);
                }
            }
        }

        private void pushCollision (HashMapCollision1<K,V> collision) {
            for (AMapEntry entry: collision.kvs) {
                stack.push (entry);
            }
        }

        @Override public boolean hasNext () {
            return ! stack.isEmpty ();
        }

        @SuppressWarnings ("unchecked")
        @Override public AMapEntry<K, V> next () {
            Object next;

            while (true) {
                next = stack.pop ();

                if (next.getClass () == HashMap1.class || next.getClass () == AListMap.Node.class) {
                    return (AMapEntry<K, V>) next;
                }
                if (next.getClass () == HashTrieMap.class) {
                    final HashTrieMap trie = (HashTrieMap) next;

                    // push elements in reverse order to provide iteration in 'ascending hash order', simplifying testing
                    for (int i=trie.elems.length-1; i>=0; i--) {
                        stack.push (trie.elems [i]);
                    }
                }
                else if (next.getClass () == HashMapCollision1.class) {
                    pushCollision ((HashMapCollision1<K, V>) next);
                }
            }
        }

        @Override public void remove () {
            throw new UnsupportedOperationException ();
        }
    }

    @Override public ASet<K> keys() {
        return AHashSet.fromMap (this);
    }

    /**
     * @param level number of least significant bits of the hash to discard for local hash lookup. This mechanism
     *              is used to create a 32-way hash trie - level increases by 5 at each level
     */
    AOption<V> doGet(K key, int hash, int level) {
        return AOption.none();
    }

    AHashMap<K,V> doUpdated(K key, int hash, int level, V value) {
        return new HashMap1<> (key, hash, value, equality);
    }

    AHashMap<K,V> doRemoved(K key, int hash, int level) {
        return this;
    }

    private static int computeHash(Object key, AEquality equality) {
        int h = equality.hashCode(key);
        h = h + ~(h << 9);
        h = h ^ (h >>> 14);
        h = h + (h << 4);
        return h ^ (h >>> 10);
    }

    @SuppressWarnings("unchecked")
    private static <K,V> AHashMap<K,V>[] createArray(int size) {
        return new AHashMap[size];
    }

    /**
     * very internal method. It assumes hash0 != hash1.
     */
    private static<K,V> HashTrieMap<K,V> mergeLeafMaps(int hash0, AHashMap<K,V> elem0, int hash1, AHashMap<K,V> elem1, int level, int size, AEquality equality) {
        final int index0 = (hash0 >>> level) & 0x1f;
        final int index1 = (hash1 >>> level) & 0x1f;
        if(index0 != index1) {
            final int bitmap = (1 << index0) | (1 << index1);
            final AHashMap<K,V>[] elems = createArray(2);
            if(index0 < index1) {
                elems[0] = elem0;
                elems[1] = elem1;
            }
            else {
                elems[0] = elem1;
                elems[1] = elem0;
            }
            return new HashTrieMap<>(bitmap, elems, size, equality);
        }
        else {
            final AHashMap<K,V>[] elems = createArray(1);
            final int bitmap = (1 << index0);
            // try again, based on the
            elems[0] = mergeLeafMaps(hash0, elem0, hash1, elem1, level + LEVEL_INCREMENT, size, equality);
            return new HashTrieMap<>(bitmap, elems, size, equality);
        }
    }


    static class HashMap1<K,V> extends AHashMap<K,V> implements AMapEntry<K,V> {
        private final K key;
        private final int hash;
        private final V value;

        HashMap1(K key, int hash, V value, AEquality equality) {
            super(equality);

            this.key = key;
            this.hash = hash;
            this.value = value;
        }

        @Override public K getKey () {
            return key;
        }
        @Override public V getValue () {
            return value;
        }
        @Override public int size() {
            return 1;
        }

        @Override AOption<V> doGet(K key, int hash, int level) {
            if(equality.equals(this.key, key)) {
                return AOption.some(value);
            }
            return AOption.none();
        }

        @Override AHashMap<K,V> doUpdated(K key, int hash, int level, V value) {
            if (hash == this.hash && equality.equals(key, this.key)) {
                if(this.value == value) {
                    return this;
                }
                else {
                    return new HashMap1<>(key, hash, value, equality);
                }
            }
            else {
                if (hash != this.hash) {
                    // they have different hashes, but may collide at this level - find a level at which they don't
                    final AHashMap<K,V> that = new HashMap1<>(key, hash, value, equality);
                    return mergeLeafMaps(this.hash, this, hash, that, level, 2, equality);
                }
                else {
                    // hash collision --> store all elements in the same bin
                    return new HashMapCollision1<> (hash, AListMap.<K,V>empty(equality).updated(this.key,this.value).updated(key,value));
                }
            }
        }

        @Override AHashMap<K,V> doRemoved(K key, int hash, int level) {
            if (hash == this.hash && equality.equals(key, this.key)) {
                return empty(equality);
            }
            else {
                return this;
            }
        }
    }

    static class HashMapCollision1<K,V> extends AHashMap<K,V> {
        private final int hash;
        private final AListMap<K,V> kvs;

        HashMapCollision1(int hash, AListMap<K, V> kvs) {
            super(kvs.equality);

            this.hash = hash;
            this.kvs = kvs;
        }

        @Override
        public int size() {
            return kvs.size();
        }

        @Override
        AOption<V> doGet(K key, int hash, int level) {
            if (hash == this.hash) {
                return kvs.get(key);
            }
            else {
                return AOption.none ();
            }
        }

        @Override
        AHashMap<K,V> doUpdated(K key, int hash, int level, V value) {
            if (hash == this.hash) {
                return new HashMapCollision1<>(hash, kvs.updated(key, value));
            }
            else {
                final HashMap1<K,V> that = new HashMap1<>(key, hash, value, equality);
                return mergeLeafMaps(this.hash, this, hash, that, level, size() + 1, equality);
            }
        }

        @Override
        AHashMap<K,V> doRemoved(K key, int hash, int level) {
            if (hash == this.hash) {
                final AListMap<K,V> kvs1 = kvs.removed(key);
                if (kvs1.isEmpty()) {
                    return AHashMap.empty(equality);
                }
                else if(kvs1.tail().isEmpty()) {
                    return new HashMap1<>(kvs1.getKey (), computeHash (kvs1.getKey (), equality), kvs1.getValue (), equality);
                }
                else {
                    return new HashMapCollision1<>(hash, kvs1);
                }
            }
            else {
                return this;
            }
        }
    }


    static class HashTrieMap<K,V> extends AHashMap<K,V> {
        final int bitmap;
        final AHashMap<K,V>[] elems;
        final int size;

        HashTrieMap(int bitmap, AHashMap<K, V>[] elems, int size, AEquality equality) {
            super(equality);

            this.bitmap = bitmap;
            this.elems = elems;
            this.size = size;
        }

        @Override public int size() {
            return size;
        }

        @Override
        AOption<V> doGet(K key, int hash, int level) {
            final int index = (hash >>> level) & 0x1f;
            final int mask = 1 << index;

            if (bitmap == - 1) {
                return elems[index & 0x1f].doGet(key, hash, level + LEVEL_INCREMENT);
            }
            else if ((bitmap & mask) != 0) {
                final int offset = Integer.bitCount(bitmap & (mask - 1));
                return elems[offset].doGet(key, hash, level + LEVEL_INCREMENT);
            }
            else {
                return AOption.none();
            }
        }

        @Override
        AHashMap<K,V>  doUpdated(K key, int hash, int level, V value) {
            final int index = (hash >>> level) & 0x1f;
            final int mask = (1 << index);
            final int offset = Integer.bitCount(bitmap & (mask - 1));
            if ((bitmap & mask) != 0) {
                final AHashMap<K,V> sub = elems[offset];

                final AHashMap<K,V> subNew = sub.doUpdated(key, hash, level + LEVEL_INCREMENT, value);
                if(subNew == sub) {
                    return this;
                }
                else {
                    final AHashMap<K,V>[] elemsNew = createArray(elems.length);
                    System.arraycopy(elems, 0, elemsNew, 0, elems.length);
                    elemsNew[offset] = subNew;
                    return new HashTrieMap<> (bitmap, elemsNew, size + (subNew.size() - sub.size()), equality);
                }
            }
            else {
                final AHashMap<K,V>[] elemsNew = createArray(elems.length + 1);
                System.arraycopy(elems, 0, elemsNew, 0, offset);
                elemsNew[offset] = new HashMap1<>(key, hash, value, equality);
                System.arraycopy(elems, offset, elemsNew, offset + 1, elems.length - offset);
                return new HashTrieMap<>(bitmap | mask, elemsNew, size + 1, equality);
            }
        }

        @Override
        AHashMap<K,V> doRemoved(K key, int hash, int level) {
            final int index = (hash >>> level) & 0x1f;
            final int mask = (1 << index);
            final int  offset = Integer.bitCount(bitmap & (mask - 1));

            if ((bitmap & mask) != 0) {
                final AHashMap<K,V> sub = elems[offset];
                final AHashMap<K,V> subNew = sub.doRemoved(key, hash, level + LEVEL_INCREMENT);

                if (subNew == sub) {
                    return this;
                }
                else if (subNew.isEmpty()) {
                    final int  bitmapNew = bitmap ^ mask;
                    if (bitmapNew != 0) {
                        final AHashMap<K,V>[] elemsNew = createArray(elems.length - 1);
                        System.arraycopy(elems, 0, elemsNew, 0, offset);
                        System.arraycopy(elems, offset + 1, elemsNew, offset, elems.length - offset - 1);
                        final int sizeNew = size - sub.size();
                        if (elemsNew.length == 1 && ! (elemsNew[0] instanceof HashTrieMap)) {
                            return elemsNew[0];
                        }
                        else {
                            return new HashTrieMap<>(bitmapNew, elemsNew, sizeNew, equality);
                        }
                    }
                    else {
                        return AHashMap.empty(equality);
                    }
                }
                else if(elems.length == 1 && ! (subNew instanceof HashTrieMap)) {
                    return subNew;
                }
                else {
                    final AHashMap<K,V>[] elemsNew = createArray(elems.length);
                    System.arraycopy(elems, 0, elemsNew, 0, elems.length);
                    elemsNew[offset] = subNew;
                    final int sizeNew = size + (subNew.size() - sub.size());
                    return new HashTrieMap<>(bitmap, elemsNew, sizeNew, equality);
                }
            } else {
                return this;
            }
        }
    }

    private Object readResolve() {
        // rebuild the map in case hashCodes of entries were changed by serialization

        AHashMap<K,V> result = AHashMap.empty (equality);

        for (AMapEntry<K,V> entry: this) {
            result = result.updated (entry.getKey (), entry.getValue ());
        }

        return result;
    }
}
