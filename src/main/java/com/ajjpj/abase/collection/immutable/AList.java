package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.ACollectionHelper;
import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

import java.lang.reflect.Array;
import java.util.*;


/**
 * This is an immutable linked list implementation. It provides "mutators" that return copies of the list without
 *  affecting the original ("copy on write").<p>
 *
 * The API is based on terminology of functional languages. NIL is the empty list, <code>head</code> is the list's
 *  first element, <code>tail</code> is the list without its first element, and <code>cons()</code> is the
 *  operation that prepends an element to an existing list.
 *
 * @author arno
 */
abstract public class AList<T> extends AbstractACollection<T, AList<T>> {
    private final int size;

    protected AList(int size) {
        this.size = size;
    }

    /**
     * Returns the empty list. All calls to this method are guaranteed to return the <em>same</em> instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> AList<T> nil() {
        return (AList<T>) Nil.INSTANCE;
    }

    /**
     * Creates an AList based on the contents of an existing <code>java.util.Iterable</code>, copying its contents.
     */
    public static <T> AList<T> create(Iterable<T> elements) {
        if(elements instanceof List) {
            return create((List<T>) elements);
        }
        AList<T> result = nil();

        for(T el: elements) {
            result = result.cons(el);
        }
        return result.reverse();
    }

    /**
     * Creates an AList based on the contents of an existing <code>java.util.List</code>, copying its content.
     */
    public static <T> AList<T> create(List<T> elements) {
        AList<T> result = nil();

        for(int i=elements.size()-1; i>=0; i--) {
            result = result.cons(elements.get(i));
        }
        return result;
    }

    /**
     * Creates an AList from a given list of elements.
     */
    @SafeVarargs
    public static <T> AList<T> create(T... elements) {
        return create(Arrays.asList(elements));
    }

    /**
     * Returns a read-only <code>java.util.List </code> view of this AList.
     */
    public java.util.List<T> asJavaUtilList() {
        return new JuListWrapper<>(this);
    }

    @Override protected AList<T> createInternal(Collection<T> elements) {
        return AList.create(elements);
    }

    @Override protected AEquality equalityForEquals() {
        return AEquality.EQUALS;
    }

    /**
     * Returns this AList's head, if any.
     */
    public abstract AOption<T> optHead();

    /**
     * Returns the list's head, i.e. its first element. If called on the empty list, it throws a <code>NoSuchElementException</code>.
     */
    public abstract T head();

    /**
     * Returns the list's tail, i.e. the list without its first element. If called on the empty list, it throws a
     *  <code>NoSuchElementException</code>.
     */
    public abstract AList<T> tail();

    /**
     * Returns a new AList with the new element prepended. This is the only operation to 'add' elements to an AList.
     */
    public AList<T> cons(T el) {
        return new AHead<>(el, this);
    }

    /**
     * Returns a copy of this AList with elements in reversed order.
     */
    public AList<T> reverse() {
        AList<T> remaining = this;
        AList<T> result = nil();

        while(! remaining.isEmpty()) {
            result = result.cons(remaining.head());
            remaining = remaining.tail();
        }

        return result;
    }

    public int size() {
        return size;
    }

    @Override public AList<T> toList() {
        // override as an optimization
        return this;
    }

    @Override public <X,E extends Exception> AList<X> map (AFunction1<? super T, ? extends X, E> f) throws E {
        return create(ACollectionHelper.map(this, f));
    }

    @Override public <X, E extends Exception> AList<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
        return create(ACollectionHelper.flatMap(this, f));
    }

    @SuppressWarnings("unchecked")
    @Override public <X> AList<X> flatten() {
        return (AList<X>) create(ACollectionHelper.flatten((Iterable<? extends Iterable<Object>>) this));
    }

    /**
     * Returns a <code>java.util.Iterator</code> over this AList's elements, allowing ALists to be used with Java's
     *  <code>for(...: list)</code> syntax introduced in version 1.5.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            AList<T> pos = AList.this;

            @Override
            public boolean hasNext() {
                return pos.nonEmpty();
            }

            @Override
            public T next() {
                final T result = pos.head();
                pos = pos.tail();
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    static class AHead<T> extends AList<T> {
        private final T head;
        private final AList<T> tail;

        AHead(T head, AList<T> tail) {
            super(tail.size() + 1);
            this.head = head;
            this.tail = tail;
        }

        @Override public AOption<T> optHead() {
            return AOption.some(head);
        }

        @Override public T head() {
            return head;
        }

        @Override public AList<T> tail() {
            return tail;
        }
    }

    static class Nil extends AList<Object> {
        public Nil() {
            super(0);
        }

        public static final Nil INSTANCE = new Nil();

        @Override public AOption<Object> optHead() {
            return AOption.none();
        }

        @Override public Object head() {
            throw new NoSuchElementException("no 'head' for an empty list.");
        }

        @Override public AList<Object> tail() {
            throw new NoSuchElementException("no 'tail' for an empty list.");
        }

        @Override public <E extends Exception> boolean forAll(APredicate<? super Object, E> pred) throws E {
            return true;
        }

        @Override public <E extends Exception> boolean exists(APredicate<? super Object, E> pred) throws E {
            return false;
        }
    }

// ------------------------------- java.com.ajjpj.abase.util wrappers

    static class JuListIteratorWrapper<T> implements java.util.ListIterator<T> {
        private AList<T> inner;
        private int idx;

        JuListIteratorWrapper(AList<T> inner, int idx) {
            this.inner = inner;
            this.idx = idx;
        }

        @Override
        public boolean hasNext() {
            return ! inner.isEmpty();
        }

        @Override
        public T next() {
            final T result = inner.head();
            inner = inner.tail();
            idx += 1;
            return result;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public T previous() {
            return null;
        }

        @Override
        public int nextIndex() {
            return idx+1;
        }

        @Override
        public int previousIndex() {
            return idx-1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T t) {
            throw new UnsupportedOperationException();
        }
    }

    static class JuListWrapper<T> implements java.util.List<T> {
        private final AList<T> inner;

        JuListWrapper(AList<T> inner) {
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

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for(Object o: c) {
                if(! contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public T get(int index) {
            AList<T> l = inner;

            for(int i=0; i<index; i++) {
                l = l.tail();
            }

            return l.head();
        }

        @Override
        public int indexOf(Object o) {
            int idx = 0;
            AList<T> l = inner;

            while(! inner.isEmpty()) {
                if(AEquality.EQUALS.equals(inner.head(), o)) {
                    return idx;
                }

                idx += 1;
                l = l.tail();
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            int idx = 0;
            AList<T> l = inner;
            int result = -1;

            while(! inner.isEmpty()) {
                if(AEquality.EQUALS.equals(inner.head(), o)) {
                    result = idx;
                }

                idx += 1;
                l = l.tail();
            }
            return result;
        }

        @Override
        public Iterator<T> iterator() {
            return listIterator();
        }

        @Override
        public Object[] toArray() {
            final Object[] result = new Object[inner.size()];
            int idx = 0;

            for(Object o: this) {
                result[idx] = o;
                idx += 1;
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T1> T1[] toArray(T1[] a) {
            if(a.length < size()) {
                a = (T1[]) Array.newInstance(a.getClass().getComponentType(), size());
            }

            if(a.length >= size()) {
                int idx = 0;

                for(Object o: this) {
                    a[idx] = (T1) o;
                    idx += 1;
                }

                if(a.length > size()) {
                    a[size()] = null;
                }
            }

            return a;
        }

        @Override
        public ListIterator<T> listIterator() {
            return new JuListIteratorWrapper<T>(inner, 0);
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            AList<T> l = inner;
            for(int i=0; i<index; i++) {
                l = l.tail();
            }
            return new JuListIteratorWrapper<T>(l, index);
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        // ------------------- mutators

        @Override
        public T set(int index, T element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, T element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}