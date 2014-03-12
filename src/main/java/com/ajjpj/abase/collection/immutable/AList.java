package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.collection.AOption;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

import java.lang.reflect.Array;
import java.util.*;


/**
 * This is an immutable linked list implementation. It provides "mutators" that return copies of the list without
 *  affecting the original ("copy on write").
 *
 * @author arno
 */
abstract public class AList<T> implements Iterable<T> {
    private final int size;

    protected AList(int size) {
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <T> AList<T> nil() {
        return (AList<T>) Nil.INSTANCE;
    }

    public static <T> AList<T> create(Iterable<T> elements) {
        AList<T> result = nil();

        for(T el: elements) {
            result = result.cons(el);
        }
        return result.reverse();
    }

    public static <T> AList<T> create(List<T> elements) {
        AList<T> result = nil();

        for(int i=elements.size()-1; i>=0; i--) {
            result = result.cons(elements.get(i));
        }
        return result;
    }

    public java.util.List<T> asJavaUtilList() {
        return new JuListWrapper<>(this);
    }

    public abstract T head();
    public abstract AList<T> tail();

    public AList<T> cons(T el) {
        return new AHead<>(el, this);
    }

    public AList<T> reverse() {
        AList<T> remaining = this;
        AList<T> result = nil();

        while(! remaining.isEmpty()) {
            result = result.cons(remaining.head());
            remaining = remaining.tail();
        }

        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    public boolean nonEmpty() {
        return size != 0;
    }

    public int size() {
        return size;
    }

    public String mkString(String prefix, String infix, String postfix) {
        final StringBuilder result = new StringBuilder(prefix);

        boolean first = true;
        for(T o: this) {
            if(first) {
                first = false;
            }
            else {
                result.append(infix);
            }
            result.append(o);
        }

        result.append(postfix);
        return result.toString();
    }

    public String mkString(String infix) {
        return mkString("", infix, "");
    }

    public AHashSet<T> toSet() {
        return AHashSet.create(this);
    }

    public <E extends Exception> AList<T> filter (APredicate<T,E> cond) throws E {
        final List<T> result = new ArrayList<>();

        for(T el: this) {
            if(cond.apply(el)) {
                result.add(el);
            }
        }

        return create(result);
    }

    public <E extends Exception> AOption<T> find (APredicate<T,E> cond) throws E{
        for(T el: this) {
            if(cond.apply(el)) {
                return AOption.some(el);
            }
        }
        return AOption.none();
    }

    public <X,E extends Exception> AList<X> map (AFunction1<X, T, E> f) throws E{
        final List<X> result = new ArrayList<>(size());

        for(T el: this) {
            result.add(f.apply(el));
        }
        return create(result);
    }

    @Override
    public String toString() {
        return mkString("[", ", ", "]");
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AList<?> _this = this;
        AList<?> _that = (AList<?>) o;
        if(size() != _that.size()) return false;

        while(_this.nonEmpty()) {
            if(! AEquality.EQUALS.equals(_this.head(), _that.head())) {
                return false;
            }

            _this = _this.tail();
            _that = _that.tail();
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;

        for(T o: this) {
            result = 31*result + (o != null ? o.hashCode() : 0);
        }

        return result;
    }

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

        @Override
        public T head() {
            return head;
        }

        @Override
        public AList<T> tail() {
            return tail;
        }
    }

    static class Nil extends AList<Object> {
        public Nil() {
            super(0);
        }

        public static final Nil INSTANCE = new Nil();

        @Override
        public Object head() {
            throw new NoSuchElementException("no 'head' for an empty list.");
        }

        @Override
        public AList<Object> tail() {
            throw new NoSuchElementException("no 'tail' for an empty list.");
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