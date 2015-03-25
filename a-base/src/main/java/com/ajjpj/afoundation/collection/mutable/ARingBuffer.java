package com.ajjpj.afoundation.collection.mutable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * This is a fixed-size data structure that is optimized for write performance with concurrent reads. Once
 *  the buffer is full, the least recent elements are overwritten.<p>
 *
 * Implementation note: This class uses <code>synchronized</code> instead of <code>ReentrantLock</code> or <code>ReentrantReadWriteLock</code>. The reason
 *  is that read access - even from several threads concurrently - leads to little lock contention, while single threaded write access can significantly
 *  profit from biased locking. A performance test with one writer thread and ten reader threads ran an order of magnitude faster with <code>synchronized</code>.
 *
 * @author arno
 */
public class ARingBuffer<T> implements Iterable<T> {
    /**
     * This is the number of bins allocated in addition to the nominal buffer size to allow room for buffering write
     *  lag when reading.
     */
    private final Class<T> elementClass;
    private final int bufSize;

    private final T[] buffer;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * points to the offset with the first (oldest) element, which is also the next one to be overwritten
     */
    private int next = 0;

    private boolean isFull = false;

    public ARingBuffer(Class<T> cls, int maxSize) {
        elementClass = cls;
        bufSize = maxSize;
        buffer = allocate();
    }

    public void put(T o) {
        synchronized(this) {
            buffer[next] = o;
            next = asBufIndex(next+1);

            if(next == 0) {
                isFull = true;
            }
        }
    }

    public void clear() {
        synchronized(this) {
            next = 0;
            isFull = false;
            Arrays.fill(buffer, null);
        }
    }

    @SuppressWarnings("unchecked")
    private T[] allocate() {
        return (T[]) Array.newInstance(elementClass, bufSize);
    }

    private int asBufIndex(int rawIdx) {
        return (rawIdx + bufSize) % bufSize;
    }

    /**
     * Iterators returned via this method are stable with regard to changes, i.e. changes may occur during iteration,
     *  but they do not affect the elements returned by the iterator.
     */
    @Override public Iterator<T> iterator() {
        synchronized(this) {
            return new Iterator<T>() {
                final T[] snapshot = allocate();

                final int end = next;
                int curPos;
                boolean hasNext;

                {
                    System.arraycopy(buffer, 0, snapshot, 0, bufSize);
                    curPos = isFull ? next : 0;
                    hasNext = isFull || end != 0;
                }

                @Override public boolean hasNext() {
                    return hasNext;
                }

                @Override public T next() {
                    if(! hasNext) {
                        throw new IndexOutOfBoundsException();
                    }

                    final T result = snapshot[curPos];
                    curPos = asBufIndex(curPos + 1);
                    hasNext = curPos != end;
                    return result;
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}