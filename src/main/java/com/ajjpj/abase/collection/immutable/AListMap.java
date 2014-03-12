package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.collection.AOption;
import com.ajjpj.abase.collection.APair;
import com.ajjpj.abase.function.AFunction1NoThrow;

import java.util.*;

/**
 * @author arno
 */
public class AListMap <K,V> implements AMap<K,V> {
    public static <K,V> AListMap<K,V> empty() {
        return new AListMap<>();
    }
    public static <K,V> AListMap<K,V> empty(AEquality equality) {
        return new AListMap<> (equality);
    }

    public static <K,V> AListMap<K,V> create(Iterable<APair<K,V>> elements) {
        return create(AEquality.EQUALS, elements);
    }
    public static <K,V> AListMap<K,V> create(AEquality equality, Iterable<APair<K,V>> elements) {
        AListMap<K,V> result = empty(equality);

        for(APair<K,V> el: elements) {
            result = result.updated(el._1, el._2);
        }
        return result;
    }

    final AEquality equality;

    private Integer cachedHashcode = null; // intentionally not volatile - potentially recalculating for different threads is traded for better single threaded performance

    public AListMap() {
        this(AEquality.EQUALS);
    }
    public AListMap(AEquality equality) {
        this.equality = equality;
    }

    @Override
    public int size() {
        return 0;
    }
    @Override
    public boolean isEmpty() {
        return true;
    }
    @Override
    public boolean nonEmpty() {
        return false;
    }

    @Override
    public AOption<V> get(K key) {
        return AOption.none();
    }

    @Override
    public V getRequired(K key) {
        return get(key).get();
    }

    public K key() {
        throw new NoSuchElementException("empty map");
    }

    public V value() {
        throw new NoSuchElementException("empty map");
    }

    @Override
    public boolean containsKey(K key) {
        return get(key).isDefined();
    }

    @Override
    public boolean containsValue(V value) {
        return false;
    }

    @Override
    public AListMap<K,V> updated(K key, V value) {
        return new Node<>(key, value, this);
    }

    @Override
    public AListMap<K,V> removed(K key) {
        return this;
    }

    public AListMap<K,V> tail() {
        throw new NoSuchElementException("empty map");
    }

    @Override
    public Map<K, V> asJavaUtilMap() {
        return new JavaUtilMapWrapper<>(this);
    }

    @Override
    public Iterator<APair<K,V>> iterator() {
        return new ListMapIterator<>(this);
    }

    @Override
    public Set<K> keys() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        return new ValueCollection();
    }

    @SuppressWarnings({"NullableProblems", "unchecked", "SuspiciousToArrayCall"})
    class KeySet implements Set<K> {
        @Override public int size() { return AListMap.this.size(); }
        @Override public boolean isEmpty() { return size() == 0; }
        @Override public boolean contains(Object o) { return containsKey((K) o); }
        @Override public Iterator<K> iterator() {
            final Iterator<APair<K,V>> lmi = AListMap.this.iterator();

            return new Iterator<K>() {
                @Override public boolean hasNext() { return lmi.hasNext(); }
                @Override public K next() { return lmi.next()._1; }
                @Override public void remove() { lmi.remove(); }
            };
        }

        @Override public Object[] toArray()     { return new ArrayList<>(this).toArray(); }
        @Override public <T> T[] toArray(T[] a) { return new ArrayList<>(this).toArray(a); }
        @Override public boolean add(K k) { throw new UnsupportedOperationException(); }
        @Override public boolean remove(Object o) { throw new UnsupportedOperationException(); }
        @Override public boolean containsAll(Collection<?> c) {
            for(Object o: c) {
                if(!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override public boolean addAll(Collection<? extends K> c) { throw new UnsupportedOperationException(); }
        @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
        @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
        @Override public void clear() { throw new UnsupportedOperationException(); }
    }

    @SuppressWarnings({"NullableProblems", "unchecked", "SuspiciousToArrayCall"})
    class ValueCollection implements Collection<V> {
        @Override public int size() { return AListMap.this.size(); }
        @Override public boolean isEmpty() { return size() == 0; }
        @Override public boolean contains(Object o) { return containsValue((V) o); }
        @Override public Iterator<V> iterator() {
            final Iterator<APair<K,V>> lmi = AListMap.this.iterator();

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
    public AMap<K, V> withDefaultValue(V defaultValue) {
        return new AMapWithDefaultValue<>(this, defaultValue);
    }

    @Override
    public AMap<K, V> withDefault(AFunction1NoThrow<V, K> function) {
        return new AMapWithDefault<>(this, function);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder("{");

        boolean first = true;
        for(APair<K,V> el: this) {
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
        if(! (o instanceof AListMap)) {
            return false;
        }
        final AListMap other = (AListMap) o;

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

    static class Node<K,V> extends AListMap<K,V> {
        private final K key;
        private final V value;
        private final AListMap<K,V> tail;

        Node(K key, V value, AListMap<K, V> tail) {
            super(tail.equality);

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
        public K key() {
            return key;
        }
        @Override
        public V value() {
            return value;
        }
        @Override
        public AListMap<K,V> tail() {
            return tail;
        }

        @Override
        public int size() {
            int result = 0;

            AListMap<K,V> m = this;
            while(m.nonEmpty()) {
                m = m.tail();
                result += 1;
            }
            return result;
        }

        @Override
        public AOption<V> get(K key) {
            AListMap<K,V> m = this;

            while(m.nonEmpty()) {
                if(equality.equals(m.key(), key)) {
                    return AOption.some(m.value());
                }
                m = m.tail();
            }
            return AOption.none();
        }

        @Override
        public boolean containsValue(V value) {
            return equality.equals(this.value, value) || tail().containsValue(value);
        }

        @Override
        public AListMap<K,V> updated(K key, V value) {
            final AListMap<K,V> m = removed(key);
            return new Node<>(key, value, m);
        }

        @Override
        public AListMap<K,V> removed(K key) {
            AList<APair<K,V>> raw = AList.nil();
            AListMap<K,V> remaining = this;

            while(remaining.nonEmpty()) {
                if(! equality.equals(remaining.key(), key)) {
                    raw = raw.cons(new APair<>(remaining.key(), remaining.value())); //TODO terminate - the key should have been unique
                }
                remaining = remaining.tail();
            }

            return AListMap.create(raw.reverse().asJavaUtilList());
        }
    }

    static class ListMapIterator<K,V> implements Iterator<APair<K,V>> {
        private AListMap<K,V> pos;

        ListMapIterator(AListMap<K, V> pos) {
            this.pos = pos;
        }

        @Override
        public boolean hasNext() {
            return pos.nonEmpty();
        }

        @Override
        public APair<K, V> next() {
            final APair<K,V> result = new APair<> (pos.key(), pos.value());
            pos = pos.tail();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
