package com.ajjpj.abase.collection.mutable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is a wrapper around a Queue. It limits the queue's size by <em>discarding</em> elements when adding a new
 *  element would exceed the queue's maximum size. Oldest elements are discarded first, i.e. when an element is
 *  added to the queue's tail, element(s) are discarded from the queue's head.<p>
 *
 * It is possible to register a callback that is triggered whenever an element is discarded. This is intended for
 *  logging / warning behavior, <em>not</em> for actually handling that element.<p>
 *
 * This is a non-blocking implementation, trading atomic behavior for performance. Its implementation first discards
 *  elements until there is room for a new element, then adds the new element. Since that is done without locking,
 *  concurrent writes can occur in such a way that the 'maximum' size is temporarily exceeded. It is however limited
 *  to ('max' + numWriters).
 *
 * @author arno
 */
public class ASoftlyLimitedQueue<T> implements Queue<T> {
    private final int maxSize;
    private final Queue<T> inner;
    private final Runnable onDiscarded;

    private static final Runnable NO_CALLBACK = new Runnable() {
        @Override public void run() {
        }
    };

    public ASoftlyLimitedQueue(int maxSize) {
        this(maxSize, new ConcurrentLinkedQueue<T>());
    }

    public ASoftlyLimitedQueue(int maxSize, Runnable onDiscarded) {
        this(maxSize, new ConcurrentLinkedQueue<T>(), onDiscarded);
    }

    public ASoftlyLimitedQueue(int maxSize, Queue<T> inner) {
        this(maxSize, inner, NO_CALLBACK);
    }

    public ASoftlyLimitedQueue(int maxSize, Queue<T> inner, Runnable onDiscarded) {
        this.maxSize = maxSize;
        this.inner = inner;
        this.onDiscarded = onDiscarded;
    }

    private void ensureCapacity() {
        while(size() >= maxSize) {
            onDiscarded.run();
            inner.poll();
        }
    }

    @Override public boolean add(T t) {
        ensureCapacity();
        return inner.add(t);
    }

    @Override public boolean offer(T t) {
        ensureCapacity();
        return inner.offer(t);
    }

    @Override public T remove() {
        return inner.remove();
    }

    @Override public T poll() {
        return inner.poll();
    }

    @Override public T element() {
        return inner.element();
    }

    @Override public T peek() {
        return inner.peek();
    }

    @Override public int size() {
        return inner.size();
    }

    @Override public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override public boolean contains(Object o) {
        return inner.contains(o);
    }

    @Override public Iterator<T> iterator() {
        return inner.iterator();
    }

    @Override public Object[] toArray() {
        return inner.toArray();
    }

    @SuppressWarnings ("SuspiciousToArrayCall")
    @Override public <T1> T1[] toArray(T1[] a) {
        return inner.toArray(a);
    }

    @Override public boolean remove(Object o) {
        return inner.remove(o);
    }

    @Override public boolean containsAll(Collection<?> c) {
        return inner.containsAll(c);
    }

    @Override public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        for(T el: c) {
            result = add(el) || result;
        }
        return result;
    }

    @Override public boolean removeAll(Collection<?> c) {
        return inner.removeAll(c);
    }

    @Override public boolean retainAll(Collection<?> c) {
        return inner.retainAll(c);
    }

    @Override public void clear() {
        inner.clear();
    }

    @SuppressWarnings ("EqualsWhichDoesntCheckParameterClass")
    @Override public boolean equals(Object o) {
        return inner.equals(o);
    }

    @Override public int hashCode() {
        return inner.hashCode();
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}
