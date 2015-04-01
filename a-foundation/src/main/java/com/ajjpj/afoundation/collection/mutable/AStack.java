package com.ajjpj.afoundation.collection.mutable;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.collection.immutable.AOption;

import java.io.Serializable;
import java.util.*;


/**
 * This is a mutable array-based implementation of a LIFO stack. Well, it is actually a wrapper around an
 *  instance of {@link java.util.Deque} with an API stripped down to just a stack, plus {@link #tryPop()} and
 *  {@link #tryPeek()}.<p>
 *
 * This class is <em>not</em> thread safe.
 *
 * @author arno
 */
public class AStack<T> implements Iterable<T>, Serializable {
    private final Deque<T> inner;

    public AStack () {
        this (new ArrayDeque<T> ());
    }

    public AStack (Deque<T> inner) {
        this.inner = inner;
    }

    /**
     * Adds an element to the top of the stack.
     */
    @SuppressWarnings("unchecked")
    public void push(T el) {
        inner.push (el);
    }

    /**
     * Removes and returns the element at the top of the stack, throwing a {@link java.util.NoSuchElementException} if the
     *  stack is empty.
     */
    public T pop() {
        return inner.pop ();
    }

    /**
     * Returns the element at the top of the stack without removing it, throwing a {@link java.util.NoSuchElementException}
     *  if the stack is empty.
     */
    public T peek() {
        return inner.peek ();
    }

    /**
     * Removes the element at the top of the stack and returns it in an {@link AOption#some(Object)} if the stack
     *  is non-empty, and returns {@link AOption#none()} otherwise.
     */
    public AOption<T> tryPop() {
        if (isEmpty()) {
            return AOption.none();
        }
        return AOption.some (pop());
    }

    /**
     * Returns the element at the top of the stack in an {@link AOption#some(Object)} without removing it, or returns
     *  {@link AOption#none()} if the stack is empty.
     */
    public AOption<T> tryPeek() {
        if (isEmpty()) {
            return AOption.none();
        }
        return AOption.some (inner.peek ());
    }


    public boolean contains(T el) {
        return inner.contains (el);
    }

    /**
     * iterates through the stack's elements in {@link #pop()} order without modifying the stack
     */
    public Iterator<T> iterator() {
        return inner.iterator ();
    }

    public int size() {
        return inner.size ();
    }

    public boolean isEmpty() {
        return inner.isEmpty ();
    }
    public boolean nonEmpty() {
        return ! inner.isEmpty ();
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        AStack aStack = (AStack) o;
        return inner.equals (aStack.inner);
    }

    @Override
    public int hashCode () {
        return inner.hashCode();
    }
    @Override
    public String toString() {
        return ACollectionHelper.mkString (this, "[", ", ", "]");
    }
}
