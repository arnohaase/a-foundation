package com.ajjpj.abase.collection;

import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.AFunction1NoThrow;

import java.util.*;

/**
 * This is an immutable hash map based on 32-way hash tries.
 *
 * @author arno
 */
public class AHashMap<K, V> implements AMap<K,V> {
    private static final int LEVEL_INCREMENT = 5;
    private static final AEquality DEFAULT_EQUALITY = AEquality.EQUALS;

    private static final AHashMap<Object, Object> emptyEquals = new AHashMap<>(AEquality.EQUALS);
    private static final AHashMap<Object, Object> emptyIdentity = new AHashMap<>(AEquality.IDENTITY);

    final AEquality equality;

    private Integer cachedHashcode = null; // intentionally not volatile: This class is immutable, so recalculating per thread works

    public static <K,V> AHashMap<K,V> empty() {
        return empty(AEquality.EQUALS);
    }

    @SuppressWarnings("unchecked")
    public static <K,V> AHashMap<K,V> empty(AEquality equality) {
        // for typical equality implementations, return pre-instantiated objects
        if(equality == AEquality.EQUALS) return (AHashMap<K, V>) emptyEquals;
        if(equality == AEquality.IDENTITY) return (AHashMap<K, V>) emptyIdentity;
        return new AHashMap<>(equality);
    }

    @SuppressWarnings("unused")
    public static <K,V> AHashMap<K,V> fromJavaUtilMap(Map<K,V> map) {
        return fromJavaUtilMap(DEFAULT_EQUALITY, map);
    }
    public static <K,V> AHashMap<K,V> fromJavaUtilMap(AEquality equality, Map<K,V> map) {
        AHashMap<K,V> result = new AHashMap<>(equality);

        for(Map.Entry<K,V> entry: map.entrySet()) {
            result = result.updated(entry.getKey(), entry.getValue());
        }

        return result;
    }

    @SuppressWarnings("unused")
    public static <K,V> AHashMap<K,V> fromKeysAndValues(Iterable<K> keys, Iterable<V> values) {
        return fromKeysAndValues(DEFAULT_EQUALITY, keys, values);
    }
    public static <K,V> AHashMap<K,V> fromKeysAndValues(AEquality equality, Iterable<K> keys, Iterable<V> values) {
        final Iterator<K> ki = keys.iterator();
        final Iterator<V> vi = values.iterator();

        AHashMap<K,V> result = AHashMap.empty(equality);

        while(ki.hasNext()) {
            final K key = ki.next();
            final V value = vi.next();

            result = result.updated(key, value);
        }
        return result;
    }

    @SuppressWarnings("unused")
    public static <K,V, E extends Exception> AHashMap<K,V> fromKeysAndFunction(Iterable<K> keys, AFunction1<V, K, E> f) throws E {
        return fromKeysAndFunction(DEFAULT_EQUALITY, keys, f);
    }
    public static <K,V, E extends Exception> AHashMap<K,V> fromKeysAndFunction(AEquality equality, Iterable<K> keys, AFunction1<V, K, E> f) throws E {
        final Iterator<K> ki = keys.iterator();

        AHashMap<K,V> result = AHashMap.empty(equality);

        while(ki.hasNext()) {
            final K key = ki.next();
            final V value = f.apply(key);

            result = result.updated(key, value);
        }
        return result;
    }


    public AHashMap(AEquality equality) {
        this.equality = equality;
    }

    @Override
    public int size() {
        return 0;
    }
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public boolean nonEmpty() {
        return size() > 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key).isDefined();
    }

    @Override
    public boolean containsValue(V value) {
        for(V cur: values()) {
            if(equality.equals(value, cur)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AOption<V> get(K key) {
        return doGet(key, computeHash(key, equality), 0);
    }

    @Override
    public V getRequired(K key) {
        return get(key).get();
    }

    @Override
    public AHashMap<K,V> updated(K key, V value) {
        return doUpdated(key, computeHash(key, equality), 0, value);
    }

    @Override
    public AHashMap<K,V> removed(K key) {
        return doRemoved(key, computeHash(key, equality), 0);
    }

    @Override
    public AMap<K, V> withDefaultValue(V defaultValue) {
        return new AMapWithDefaultValue<>(this, defaultValue);
    }

    @Override
    public AMap<K, V> withDefault(AFunction1NoThrow<V, K> function) {
        return new AMapWithDefault<>(this, function);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(! (o instanceof AHashMap)) {
            return false;
        }
        final AHashMap other = (AHashMap) o;

        if(size() != other.size()) {
            return false;
        }

        for(APair<K,V> el: this) {
            if(! equality.equals(other.get(el._1), AOption.some(el._2))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if(cachedHashcode == null) {
            int result = 0;

            for(APair<K,V> el: this) {
                result = result ^ (31*equality.hashCode(el._1) + equality.hashCode(el._2));
            }

            cachedHashcode = result;
        }

        return cachedHashcode;
    }

    @Override
    public Iterator<APair<K, V>> iterator() {
        return new Iterator<APair<K, V>>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public APair<K, V> next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override public Iterable<K> keys() {
        return Collections.emptyList();
    }

    @Override public Iterable<V> values() {
        return Collections.emptyList();
    }

    @Override
    public Map<K, V> asJavaUtilMap() {
        return new JavaUtilMapWrapper<>(this);
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

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder("{");
        boolean first = true;

        for(APair<K,V> e: this) {
            if(first) {
                first = false;
            }
            else {
                result.append(", ");
            }

            result.append(e._1).append("->").append(e._2);
        }

        result.append("}");
        return result.toString();
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


    static class HashMap1<K,V> extends AHashMap<K,V> {
        private final K key;
        private final int hash;
        private final V value;

        HashMap1(K key, int hash, V value, AEquality equality) {
            super(equality);

            this.key = key;
            this.hash = hash;
            this.value = value;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        AOption<V> doGet(K key, int hash, int level) {
            if(this.key.equals (key)) {
                return AOption.some(value);
            }
            return AOption.none();
        }

        @Override
        AHashMap<K,V> doUpdated(K key, int hash, int level, V value) {
            if (hash == this.hash && key.equals(this.key)) {
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

        @Override
        AHashMap<K,V> doRemoved(K key, int hash, int level) {
            if (hash == this.hash && key.equals(this.key)) {
                return empty(equality);
            }
            else {
                return this;
            }
        }

        @Override
        public Iterator<APair<K, V>> iterator() {
            return new Iterator<APair<K, V>>() {
                boolean initial = true;

                @Override
                public boolean hasNext() {
                    return initial;
                }

                @Override
                public APair<K, V> next() {
                    if(initial) {
                        initial = false;
                        return new APair<> (key, value);
                    }
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Iterable<K> keys() {
            return Arrays.asList(key);
        }

        @Override
        public Iterable<V> values() {
            return Arrays.asList(value);
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
                return AOption.none();
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
                    return new HashMap1<>(kvs1.key(), computeHash(kvs1.key(), equality), kvs1.value(), equality);
                }
                else {
                    return new HashMapCollision1<>(hash, kvs1);
                }
            }
            else {
                return this;
            }
        }

        @Override
        public Iterator<APair<K, V>> iterator() {
            return kvs.iterator();
        }

        @Override
        public Iterable<K> keys() {
            return kvs.keys();
        }

        @Override
        public Iterable<V> values() {
            return kvs.values();
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

        @Override
        public int size() {
            return size;
        }

        @Override
        public Iterator<APair<K, V>> iterator() {
            final List<Iterator<APair<K,V>>> innerIter = new ArrayList<>(elems.length);
            for(AHashMap<K,V> m: elems)  {
                innerIter.add(m.iterator());
            }
            return new ACompositeIterator<>(innerIter);
        }

        @Override
        public Iterable<K> keys() {
            return new Iterable<K>() {
                @Override public Iterator<K> iterator() {
                    final List<Iterator<K>> innerIter = new ArrayList<>(elems.length);
                    for(AHashMap<K,V> m: elems) {
                        innerIter.add(m.keys().iterator());
                    }
                    return new ACompositeIterator<>(innerIter);
                }
            };
        }

        @Override
        public Iterable<V> values() {
            return new Iterable<V>() {
                @Override public Iterator<V> iterator() {
                    final List<Iterator<V>> innerIter = new ArrayList<>(elems.length);
                    for(AHashMap<K,V> m: elems) {
                        innerIter.add(m.values().iterator());
                    }
                    return new ACompositeIterator<>(innerIter);
                }
            };
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
}
