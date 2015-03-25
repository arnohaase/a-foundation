package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.tuples.ATuple2;

import java.util.*;

/**
 * @author arno
 */
class JavaUtilMapWrapper<K,V> implements java.util.Map<K,V> {
    private final AMap<K,V> inner;

    JavaUtilMapWrapper(AMap<K, V> inner) {
        this.inner = inner;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return inner.containsKey((K) key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue((V) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return inner.get((K) key).getOrElse(null);
    }

    /**
     * There is usually a performance gain to be had by overriding this default implementation
     */
    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {
                    final Iterator<ATuple2<K,V>> it = inner.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public K next() {
                        return it.next()._1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return inner.size();
            }
        };
    }

    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                return new Iterator<V>() {
                    final Iterator<ATuple2<K,V>> it = inner.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public V next() {
                        return it.next()._2;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return inner.size();
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K,V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    final Iterator<ATuple2<K,V>> it = inner.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        final ATuple2<K,V> kv = it.next();

                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return kv._1;
                            }

                            @Override
                            public V getValue() {
                                return kv._2;
                            }

                            @Override
                            public V setValue(V value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return inner.size();
            }
        };
    }

    //------------- mutators

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
