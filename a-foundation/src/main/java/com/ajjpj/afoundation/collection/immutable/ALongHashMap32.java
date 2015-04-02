package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.ACompositeIterator;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AFunction1;

import java.io.Serializable;
import java.util.*;


/**
 * This is an {@link AHashMap} that is specialized for keys of type long, i.e. primitive numbers rather than objects. It duplicates its API
 *  to support both efficient primitive 'long' values and generified 'Long's,
 *
 * @author arno
 */
public class ALongHashMap32<V> implements AMap<Long,V>, Serializable {
    private static final int LEVEL_INCREMENT = 5;

    transient private Integer cachedHashcode = null; // intentionally not volatile: This class is immutable, so recalculating per thread works

    private static final ALongHashMap32 EMPTY = new ALongHashMap32 ();


    /**
     * Returns an empty ALongHashMap instance. Calling this factory method instead of
     *  the constructor allows internal reuse of empty map instances since they are immutable.
     */
    @SuppressWarnings("unchecked")
    public static <V> ALongHashMap32<V> empty() {
        return EMPTY;
    }

    public static <V> ALongHashMap32<V> fromJavaUtilMap(Map<? extends Number,V> map) {
        ALongHashMap32<V> result = empty ();

        for(Map.Entry<? extends Number,V> entry: map.entrySet()) {
            result = result.updated(entry.getKey().longValue (), entry.getValue());
        }

        return result;
    }

    /**
     * Returns an ALongHashMap initialized from separate 'keys' and 'values' collections. Both collections
     *  are iterated exactly once and are expected to have the same size.
     */
    public static <V> ALongHashMap32<V> fromKeysAndValues(Iterable<? extends Number> keys, Iterable<V> values) {
        final Iterator<? extends Number> ki = keys.iterator();
        final Iterator<V> vi = values.iterator();

        ALongHashMap32<V> result = ALongHashMap32.empty ();

        while(ki.hasNext()) {
            final Number key = ki.next();
            final V value = vi.next();

            result = result.updated(key.longValue (), value);
        }
        return result;
    }

    /**
     * Returns an ALongHashMap instance initialized from a collection of
     *  keys and a function. For each element of the <code>keys</code> collection, the function is called once to
     *  determine the corresponding value, and the pair is then stored in the map.
     */
    @SuppressWarnings("unused")
    public static <K extends Number, V, E extends Exception> ALongHashMap32<V> fromKeysAndFunction(Iterable<K> keys, AFunction1<? super K, ? extends V, E> f) throws E {
        final Iterator<K> ki = keys.iterator();

        ALongHashMap32<V> result = empty ();

        while(ki.hasNext()) {
            final K key = ki.next();
            final V value = f.apply(key);

            result = result.updated(key.longValue (), value);
        }
        return result;
    }

    private ALongHashMap32 () {
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
    public boolean containsKey(Long key) {
        return containsKey (key.longValue ());
    }
    public boolean containsKey(long key) {
        return get(key).isDefined();
    }

    @Override
    public boolean containsValue(V value) {
        for (V cur: values()) {
            if (Objects.equals (value, cur)) {
                return true;
            }
        }
        return false;
    }

    @Override public AOption<V> get (Long key) {
        return get (key.longValue ());
    }
    public AOption<V> get(long key) {
        return doGet(key, computeHash(key), 0);
    }

    @Override public V getRequired (Long key) {
        return getRequired (key.longValue());
    }
    public V getRequired (long key) {
        return get(key).get();
    }

    @Override public ALongHashMap32<V> updated (Long key, V value) {
        return updated (key.longValue (), value);
    }
    public ALongHashMap32<V> updated (long key, V value) {
        return doUpdated(key, computeHash(key), 0, value);
    }

    @Override public ALongHashMap32<V> removed (Long key) {
        return removed (key.longValue ());
    }
    public ALongHashMap32<V> removed (long key) {
        return doRemoved(key, computeHash(key), 0);
    }

    @Override public AMap<Long, V> withDefaultValue(V defaultValue) {
        return new AMapWithDefaultValue<>(this, defaultValue);
    }

    @Override public AMap<Long, V> withDefault(AFunction1<? super Long, ? extends V, ? extends RuntimeException> function) {
        return new AMapWithDefault<>(this, function);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(! (o instanceof AMap)) {
            return false;
        }
        final AMap other = (AMap) o;

        if(size() != other.size()) {
            return false;
        }

        for(ATuple2<Long,V> el: this) {
            final AOption<V> otherValue = other.get(el._1);
            if(otherValue.isEmpty()) {
                return false;
            }

            if(! Objects.equals(el._2, otherValue.get())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if(cachedHashcode == null) {
            int result = 0;

            for(ATuple2<Long,V> el: this) {
                result = result ^ (31*el._1.hashCode () + Objects.hashCode(el._2));
            }

            cachedHashcode = result;
        }

        return cachedHashcode;
    }

    @Override
    public Iterator<ATuple2<Long, V>> iterator() {
        return new Iterator<ATuple2<Long, V>>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public ATuple2<Long, V> next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override public Set<Long> keys() {
        return Collections.emptySet();
    }

    @Override public Collection<V> values() {
        return Collections.emptyList();
    }

    @Override public Map<Long, V> asJavaUtilMap() {
        return new JavaUtilMapWrapper<>(this);
    }

    /**
     * @param level number of least significant bits of the hash to discard for local hash lookup. This mechanism
     *              is used to create a 32-way hash trie - level increases by 5 at each level
     */
    AOption<V> doGet(long key, int hash, int level) {
        return AOption.none();
    }

    ALongHashMap32<V> doUpdated(long key, int hash, int level, V value) {
        return new LongHashMap1<> (key, hash, value);
    }

    ALongHashMap32<V> doRemoved(long key, int hash, int level) {
        return this;
    }

    private static int computeHash (long key) {
        int h = new Long(key).hashCode (); // this is caught by Escape Analysis and then inlined, so efficiency is not impacted by creating a temp object
        h = h + ~(h << 9);
        h = h ^ (h >>> 14);
        h = h + (h << 4);
        return h ^ (h >>> 10);
    }

    @SuppressWarnings("unchecked")
    private static <K,V> ALongHashMap32<V>[] createArray(int size) {
        return new ALongHashMap32[size];
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder("{");
        boolean first = true;

        for(ATuple2<Long, V> e: this) {
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
    private static<K,V> LongHashTrieMap<V> mergeLeafMaps(int hash0, ALongHashMap32<V> elem0, int hash1, ALongHashMap32<V> elem1, int level, int size) {
        final int index0 = (hash0 >>> level) & 0x1f;
        final int index1 = (hash1 >>> level) & 0x1f;
        if(index0 != index1) {
            final int bitmap = (1 << index0) | (1 << index1);
            final ALongHashMap32<V>[] elems = createArray(2);
            if(index0 < index1) {
                elems[0] = elem0;
                elems[1] = elem1;
            }
            else {
                elems[0] = elem1;
                elems[1] = elem0;
            }
            return new LongHashTrieMap<>(bitmap, elems, size);
        }
        else {
            final ALongHashMap32<V>[] elems = createArray(1);
            final int bitmap = (1 << index0);
            // try again, based on the
            elems[0] = mergeLeafMaps(hash0, elem0, hash1, elem1, level + LEVEL_INCREMENT, size);
            return new LongHashTrieMap<>(bitmap, elems, size);
        }
    }


    static class LongHashMap1<V> extends ALongHashMap32<V> {
        private final long key;
        private final int hash;
        private final V value;

        LongHashMap1(long key, int hash, V value) {
            super();

            this.key = key;
            this.hash = hash;
            this.value = value;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        AOption<V> doGet(long key, int hash, int level) {
            if(this.key == key) {
                return AOption.some(value);
            }
            return AOption.none();
        }

        @Override ALongHashMap32<V> doUpdated(long key, int hash, int level, V value) {
            if (key == this.key) {
                if(this.value == value) {
                    return this;
                }
                else {
                    return new LongHashMap1<>(key, hash, value);
                }
            }
            else {
                if (hash != this.hash) {
                    // they have different hashes, but may collide at this level - find a level at which they don't
                    final ALongHashMap32<V> that = new LongHashMap1<>(key, hash, value);
                    return mergeLeafMaps(this.hash, this, hash, that, level, 2);
                }
                else {
                    // hash collision --> store all elements in the same bin
                    return new LongHashMapCollision1<> (hash, ALongListMap.<V>empty().updated(this.key,this.value).updated(key,value));
                }
            }
        }

        @Override ALongHashMap32<V> doRemoved(long key, int hash, int level) {
            if (key == this.key) {
                return empty();
            }
            else {
                return this;
            }
        }

        @Override
        public Iterator<ATuple2<Long, V>> iterator() {
            return new Iterator<ATuple2<Long, V>>() {
                boolean initial = true;

                @Override
                public boolean hasNext() {
                    return initial;
                }

                @Override
                public ATuple2<Long, V> next() {
                    if(initial) {
                        initial = false;
                        return new ATuple2<> (key, value);
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
        public Set<Long> keys() {
            return Collections.singleton(key);
        }

        @Override
        public Collection<V> values() {
            return Collections.singletonList(value);
        }
    }

    static class LongHashMapCollision1<V> extends ALongHashMap32<V> {
        private final int hash;
        private final ALongListMap<V> kvs;

        LongHashMapCollision1(int hash, ALongListMap<V> kvs) {
            super();

            this.hash = hash;
            this.kvs = kvs;
        }

        @Override
        public int size() {
            return kvs.size();
        }

        @Override
        AOption<V> doGet(long key, int hash, int level) {
            if (hash == this.hash) {
                return kvs.get(key);
            }
            else {
                return AOption.none();
            }
        }

        @Override ALongHashMap32<V> doUpdated(long key, int hash, int level, V value) {
            if (hash == this.hash) {
                return new LongHashMapCollision1<>(hash, kvs.updated(key, value));
            }
            else {
                final LongHashMap1<V> that = new LongHashMap1<>(key, hash, value);
                return mergeLeafMaps(this.hash, this, hash, that, level, size() + 1);
            }
        }

        @Override ALongHashMap32<V> doRemoved(long key, int hash, int level) {
            if (hash == this.hash) {
                final ALongListMap<V> kvs1 = kvs.removed(key);
                if (kvs1.isEmpty()) {
                    return ALongHashMap32.empty ();
                }
                else if(kvs1.tail().isEmpty()) {
                    return new LongHashMap1<>(kvs1.key(), computeHash(kvs1.key()), kvs1.value());
                }
                else {
                    return new LongHashMapCollision1<>(hash, kvs1);
                }
            }
            else {
                return this;
            }
        }

        @Override
        public Iterator<ATuple2<Long, V>> iterator() {
            return kvs.iterator();
        }

        @Override
        public Set<Long> keys() {
            return kvs.keys();
        }

        @Override
        public Collection<V> values() {
            return kvs.values();
        }
    }


    static class LongHashTrieMap<V> extends ALongHashMap32<V> {
        final int bitmap;
        final ALongHashMap32<V>[] elems;
        final int size;

        LongHashTrieMap(int bitmap, ALongHashMap32<V>[] elems, int size) {
            super();

            this.bitmap = bitmap;
            this.elems = elems;
            this.size = size;
        }

        @Override public int size() {
            return size;
        }

        @Override
        public Iterator<ATuple2<Long, V>> iterator() {
            final List<Iterator<ATuple2<Long,V>>> innerIter = new ArrayList<>(elems.length);
            for(ALongHashMap32<V> m: elems)  {
                innerIter.add(m.iterator());
            }
            return new ACompositeIterator<> (innerIter);
        }

        @Override public Set<Long> keys() {
            return new KeySet();
        }

        @Override
        public Collection<V> values() {
            return new ValueCollection();
        }

        @SuppressWarnings({"NullableProblems", "unchecked", "SuspiciousToArrayCall"})
        class KeySet implements Set<Long> {
            @Override public int size() { return size; }
            @Override public boolean isEmpty() { return size == 0; }
            @Override public boolean contains(Object o) { return containsKey((Long) o); }
            @Override public Iterator<Long> iterator() {
                final List<Iterator<Long>> innerIter = new ArrayList<>(elems.length);
                for(ALongHashMap32<V> m: elems) {
                    innerIter.add(m.keys().iterator());
                }
                return new ACompositeIterator<>(innerIter);
            }

            @Override public Object[] toArray()     { return new ArrayList<>(this).toArray(); }
            @Override public <T> T[] toArray(T[] a) { return new ArrayList<>(this).toArray(a); }
            @Override public boolean add(Long k) { throw new UnsupportedOperationException(); }
            @Override public boolean remove(Object o) { throw new UnsupportedOperationException(); }
            @Override public boolean containsAll(Collection<?> c) {
                for(Object o: c) {
                    if(!contains(o)) {
                        return false;
                    }
                }
                return true;
            }

            @Override public boolean addAll(Collection<? extends Long> c) { throw new UnsupportedOperationException(); }
            @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
            @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
            @Override public void clear() { throw new UnsupportedOperationException(); }
        }

        @SuppressWarnings({"NullableProblems", "unchecked", "SuspiciousToArrayCall"})
        class ValueCollection implements Collection<V> {
            @Override public int size() { return size; }
            @Override public boolean isEmpty() { return size == 0; }
            @Override public boolean contains(Object o) { return containsValue((V) o); }
            @Override public Iterator<V> iterator() {
                final List<Iterator<V>> innerIter = new ArrayList<>(elems.length);
                for(ALongHashMap32<V> m: elems) {
                    innerIter.add(m.values().iterator());
                }
                return new ACompositeIterator<>(innerIter);
            }

            @Override public Object[] toArray()     { return new ArrayList<>(this).toArray(); }
            @Override public <T> T[] toArray(T[] a) { return new ArrayList<>(this).toArray(a); }
            @Override public boolean add(V v) { throw new UnsupportedOperationException(); }
            @Override public boolean remove(Object o) { throw new UnsupportedOperationException(); }
            @Override public boolean containsAll(Collection<?> c) {
                for(Object o: c) {
                    if(!contains(o)) {
                        return false;
                    }
                }
                return true;
            }

            @Override public boolean addAll(Collection<? extends V> c) { throw new UnsupportedOperationException(); }
            @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
            @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
            @Override public void clear() { throw new UnsupportedOperationException(); }
        }

        @Override
        AOption<V> doGet(long key, int hash, int level) {
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

        @Override ALongHashMap32<V> doUpdated(long key, int hash, int level, V value) {
            final int index = (hash >>> level) & 0x1f;
            final int mask = (1 << index);
            final int offset = Integer.bitCount(bitmap & (mask - 1));
            if ((bitmap & mask) != 0) {
                final ALongHashMap32<V> sub = elems[offset];

                final ALongHashMap32<V> subNew = sub.doUpdated(key, hash, level + LEVEL_INCREMENT, value);
                if(subNew == sub) {
                    return this;
                }
                else {
                    final ALongHashMap32<V>[] elemsNew = createArray(elems.length);
                    System.arraycopy(elems, 0, elemsNew, 0, elems.length);
                    elemsNew[offset] = subNew;
                    return new LongHashTrieMap<> (bitmap, elemsNew, size + (subNew.size() - sub.size()));
                }
            }
            else {
                final ALongHashMap32<V>[] elemsNew = createArray(elems.length + 1);
                System.arraycopy(elems, 0, elemsNew, 0, offset);
                elemsNew[offset] = new LongHashMap1<>(key, hash, value);
                System.arraycopy(elems, offset, elemsNew, offset + 1, elems.length - offset);
                return new LongHashTrieMap<>(bitmap | mask, elemsNew, size + 1);
            }
        }

        @Override ALongHashMap32<V> doRemoved (long key, int hash, int level) {
            final int index = (hash >>> level) & 0x1f;
            final int mask = (1 << index);
            final int  offset = Integer.bitCount(bitmap & (mask - 1));

            if ((bitmap & mask) != 0) {
                final ALongHashMap32<V> sub = elems[offset];
                final ALongHashMap32<V> subNew = sub.doRemoved(key, hash, level + LEVEL_INCREMENT);

                if (subNew == sub) {
                    return this;
                }
                else if (subNew.isEmpty()) {
                    final int  bitmapNew = bitmap ^ mask;
                    if (bitmapNew != 0) {
                        final ALongHashMap32<V>[] elemsNew = createArray(elems.length - 1);
                        System.arraycopy(elems, 0, elemsNew, 0, offset);
                        System.arraycopy(elems, offset + 1, elemsNew, offset, elems.length - offset - 1);
                        final int sizeNew = size - sub.size();
                        if (elemsNew.length == 1 && ! (elemsNew[0] instanceof LongHashTrieMap)) {
                            return elemsNew[0];
                        }
                        else {
                            return new LongHashTrieMap<>(bitmapNew, elemsNew, sizeNew);
                        }
                    }
                    else {
                        return ALongHashMap32.empty ();
                    }
                }
                else if(elems.length == 1 && ! (subNew instanceof LongHashTrieMap)) {
                    return subNew;
                }
                else {
                    final ALongHashMap32<V>[] elemsNew = createArray(elems.length);
                    System.arraycopy(elems, 0, elemsNew, 0, elems.length);
                    elemsNew[offset] = subNew;
                    final int sizeNew = size + (subNew.size() - sub.size());
                    return new LongHashTrieMap<>(bitmap, elemsNew, sizeNew);
                }
            } else {
                return this;
            }
        }
    }
}
