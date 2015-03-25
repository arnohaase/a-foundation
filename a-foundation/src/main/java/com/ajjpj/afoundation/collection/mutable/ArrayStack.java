package com.ajjpj.afoundation.collection.mutable;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.collection.immutable.AOption;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * This is a mutable array-based implementation of a LIFO stack. It is intended as a replacement of Java's
 *  <code>java.util.Stack</code> that is broken in many ways.<p>
 *
 * This class is <em>not</em> thread safe.
 *
 * @author arno
 */
public class ArrayStack<T> implements Iterable<T> {
    private T[] data;
    private int size = 0;

    public ArrayStack() {
        this(10);
    }

    @SuppressWarnings("unchecked")
    public ArrayStack(int initialSize) {
        if(initialSize <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        data = (T[]) new Object[initialSize];
    }

    /**
     * Adds an element to the top of the stack.
     */
    @SuppressWarnings("unchecked")
    public void push(T el) {
        if(size >= data.length) {
            final T[] oldData = data;
            data = (T[]) new Object[2*oldData.length];
            System.arraycopy(oldData, 0, data, 0, oldData.length);
        }

        data[size] = el;
        size += 1;
    }

    /**
     * Removes and returns the element at the top of the stack, throwing a <code>NoSuchElementException</code> if the
     *  stack is empty.
     */
    public T pop() {
        if(isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        final T result = data[size-1];
        data[size-1] = null; // allow GC of previous element
        size -= 1;
        return result;
    }

    /**
     * Returns the element at the top of the stack without removing it, throwing a <code>NoSuchElementException</code>
     *  if the stack is empty.
     */
    public T peek() {
        if(isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        return data[size-1];
    }

    /**
     * Removes the element at the top of the stack and returns it in an <code>AOption.some(...)</code> if the stack
     *  is non-empty, and returns <code>AOption.none()</code> otherwise.
     */
    public AOption<T> tryPop() {
        if(isEmpty()) {
            return AOption.none();
        }
        return AOption.some(pop());
    }

    /**
     * Returns the element at the top of the stack in an <code>AOption.some(...)</code> without removing it, or returns
     *  <code>AOption.none()</code> if the stack is empty.
     */
    public AOption<T> tryPeek() {
        if(isEmpty()) {
            return AOption.none();
        }
        return AOption.some(data[size-1]);
    }


    public boolean contains(T el) {
        for(int i=0; i<size; i++) {
            if(nullSafeEq(el, data[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean nullSafeEq(Object o1, Object o2) {
        if(o1 == null) {
            return o2 == null;
        }
        else {
            return o1.equals(o2);
        }
    }

    /**
     * iterates through the stack's elements in <code>pop()</code> order without modifying the stack
     */
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int idx=size();

            @Override public boolean hasNext() {
                return idx>0;
            }

            @Override public T next() {
                idx -= 1;
                return data[idx];
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * iterates through the stack's elements in reverse <code>pop()</code> order, i.e. starting with the element that
     *  was added first end finishing with the most recently added element.
     */
    public Iterator<T> reverseIterator() {
        return new Iterator<T>() {
            int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < size();
            }

            @Override public T next() {
                return data[idx++];
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    public boolean nonEmpty() {
        return size > 0;
    }

    @Override
    public String toString() {
        return ACollectionHelper.mkString(this, "[", ", ", "]");
    }
}
