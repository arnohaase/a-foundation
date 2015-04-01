package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AFunction1;

import java.io.Serializable;
import java.util.*;


/**
 * This is an {@link AListMap} specialized for primitive keys of type {@code long}. It duplicates its API to support
 *  both primitive {@code long} values and {@code Long} objects as keys, but uses only primitive {@code long}s internally.
 *
 * @author arno
 */
public class ALongListMap<V> implements AMap<Long,V>, Serializable {
    private static final ALongListMap EMPTY = new ALongListMap<> ();

    /**
     * Returns an empty AListMap instance. Calling this factory method instead
     *  of the constructor allows internal reuse of empty map instances since they are immutable.
     */
    public static <V> ALongListMap<V> empty() {
        return EMPTY;
    }

    /**
     * Returns an ALongListMap instance initialized from separate 'keys'
     *  and 'values' collections. Both collections are iterated exactly once, and are expected to have the same size.
     */
    public static <V> ALongListMap<V> fromKeysAndValues(Iterable<ATuple2<? extends Number,V>> elements) {
        ALongListMap<V> result = empty();

        for(ATuple2<? extends Number,V> el: elements) {
            result = result.updated(el._1.longValue (), el._2);
        }
        return result;
    }

    /**
     * Returns an AListMap instance initialized from separate 'keys'
     *  and 'values' collections. Both collections are iterated exactly once, and are expected to have the same size.
     */
    public static <V> ALongListMap<V> fromKeysAndValues(Iterable<? extends Number> keys, Iterable<V> values) {
        final Iterator<? extends Number> ki = keys.iterator();
        final Iterator<V> vi = values.iterator();

        ALongListMap<V> result = empty ();

        while(ki.hasNext()) {
            final Number key = ki.next();
            final V value = vi.next();

            result = result.updated(key.longValue (), value);
        }
        return result;
    }

    private Integer cachedHashcode = null; // intentionally not volatile - potentially recalculating for different threads is traded for better single threaded performance

    private ALongListMap () {
    }

    @Override public int size() {
        return 0;
    }
    @Override public boolean isEmpty() {
        return true;
    }
    @Override public boolean nonEmpty() {
        return false;
    }

    @Override public AOption<V> get(Long key) {
        return get (key.longValue ());
    }
    public AOption<V> get(long key) {
        return AOption.none();
    }

    @Override public V getRequired (Long key) {
        return getRequired (key.longValue ());
    }
    public V getRequired (long key) {
        return get(key).get();
    }

    public long key() {
        throw new NoSuchElementException("empty map");
    }

    public V value() {
        throw new NoSuchElementException("empty map");
    }

    @Override public boolean containsKey (Long key) {
        return containsKey (key.longValue ());
    }
    public boolean containsKey (long key) {
        return get(key).isDefined();
    }

    @Override public boolean containsValue(V value) {
        return false;
    }

    @Override public ALongListMap<V> updated (Long key, V value) {
        return updated (key.longValue (), value);
    }
    public ALongListMap<V> updated(long key, V value) {
        return new Node<>(key, value, this);
    }

    @Override public ALongListMap<V> removed (Long key) {
        return removed (key.longValue ());
    }
    public ALongListMap<V> removed (long key) {
        return this;
    }

    public ALongListMap<V> tail() {
        throw new NoSuchElementException("empty map");
    }

    @Override
    public Map<Long, V> asJavaUtilMap() {
        return new JavaUtilMapWrapper<>(this);
    }

    @Override
    public Iterator<ATuple2<Long,V>> iterator() {
        return new ListMapIterator<>(this);
    }

    @Override
    public Set<Long> keys() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        return new ValueCollection();
    }

    @SuppressWarnings({"NullableProblems", "unchecked", "SuspiciousToArrayCall"})
    class KeySet implements Set<Long> {
        @Override public int size() { return ALongListMap.this.size(); }
        @Override public boolean isEmpty() { return size() == 0; }
        @Override public boolean contains(Object o) { return containsKey((Long) o); }
        @Override public Iterator<Long> iterator() {
            final Iterator<ATuple2<Long,V>> lmi = ALongListMap.this.iterator();

            return new Iterator<Long>() {
                @Override public boolean hasNext() { return lmi.hasNext(); }
                @Override public Long next() { return lmi.next()._1; }
                @Override public void remove() { lmi.remove(); }
            };
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
        @Override public int size() { return ALongListMap.this.size(); }
        @Override public boolean isEmpty() { return size() == 0; }
        @Override public boolean contains(Object o) { return containsValue((V) o); }
        @Override public Iterator<V> iterator() {
            final Iterator<ATuple2<Long,V>> lmi = ALongListMap.this.iterator();

            return new Iterator<V>() {
                @Override public boolean hasNext() { return lmi.hasNext(); }
                @Override public V next() {return lmi.next()._2; }
                @Override public void remove() { lmi.remove(); }
            };
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
    public AMap<Long, V> withDefaultValue(V defaultValue) {
        return new AMapWithDefaultValue<>(this, defaultValue);
    }

    @Override
    public AMap<Long, V> withDefault(AFunction1<? super Long, ? extends V, ? extends RuntimeException> function) {
        return new AMapWithDefault<>(this, function);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder("{");

        boolean first = true;
        for(ATuple2<Long,V> el: this) {
            if(first) {
                first = false;
            }
            else {
                result.append(", ");
            }
            result.append(el._1).append(" -> ").append(el._2);
        }

        result.append("}");
        return result.toString();
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
            if(! Objects.equals(other.get(el._1), AOption.some(el._2))) {
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
                result = result ^ (31*el._1.hashCode () + Objects.hashCode (el._2));
            }
            cachedHashcode = result;
        }
        return cachedHashcode;
    }

    static class Node<V> extends ALongListMap<V> {
        private final long key;
        private final V value;
        private final ALongListMap<V> tail;

        Node(long key, V value, ALongListMap<V> tail) {
            super();

            this.key = key;
            this.value = value;
            this.tail = tail;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public boolean nonEmpty() {
            return true;
        }

        @Override
        public long key() {
            return key;
        }
        @Override
        public V value() {
            return value;
        }
        @Override
        public ALongListMap<V> tail() {
            return tail;
        }

        @Override
        public int size() {
            int result = 0;

            ALongListMap<V> m = this;
            while(m.nonEmpty()) {
                m = m.tail();
                result += 1;
            }
            return result;
        }

        @Override
        public AOption<V> get(Long key) {
            return get (key.longValue ());
        }
        public AOption<V> get(long key) {
            ALongListMap<V> m = this;

            while (m.nonEmpty()) {
                if(m.key() == key) {
                    return AOption.some(m.value());
                }
                m = m.tail();
            }
            return AOption.none();
        }

        @Override public boolean containsValue(V value) {
            return Objects.equals(this.value, value) || tail().containsValue(value);
        }

        @Override public ALongListMap<V> updated (Long key, V value) {
            return updated (key.longValue (), value);
        }
        public ALongListMap<V> updated (long key, V value) {
            final ALongListMap<V> m = removed(key);
            return new Node<>(key, value, m);
        }

        @Override public ALongListMap<V> removed (Long key) {
            return removed (key.longValue ());
        }
        public ALongListMap<V> removed (long key) {
            int idx = 0;

            ALongListMap<V> remaining = this;
            while(remaining.nonEmpty()) {
                if(remaining.key() == key) {
                    remaining = remaining.tail();
                    break;
                }
                idx += 1;
                remaining = remaining.tail();
            }

            ALongListMap<V> result = remaining;

            ALongListMap<V> iter = this;
            for (int i=0; i<idx; i++) {
                result = new Node<> (iter.key (), iter.value (), result);
                iter = iter.tail ();
            }

            if (idx >= size ()) {
                return this;
            }

            return result;
        }
    }

    //TODO interface LongMapIterator, LongIterator
    static class ListMapIterator<V> implements Iterator<ATuple2<Long, V>> {
        private ALongListMap<V> pos;

        ListMapIterator(ALongListMap<V> pos) {
            this.pos = pos;
        }

        @Override
        public boolean hasNext() {
            return pos.nonEmpty();
        }

        @Override
        public ATuple2<Long, V> next() {
            final ATuple2<Long,V> result = new ATuple2<> (pos.key(), pos.value());
            pos = pos.tail();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
