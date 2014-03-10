package com.ajjpj.abase.collection;

import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author arno
 */
public class AHashSet<T> implements Iterable<T> {
    private final AHashMap<T, Boolean> inner;

    public static <T> AHashSet<T> empty() {
        return empty(AEquality.EQUALS);
    }
    public static <T> AHashSet<T> empty(AEquality equality) {
        return new AHashSet<>(AHashMap.<T, Boolean>empty(equality));
    }

    public static <T> AHashSet<T> create(Iterable<T> elements) {
        return create(AEquality.EQUALS, elements);
    }
    public static <T> AHashSet<T> create(AEquality equality, Iterable<T> elements) {
        AHashSet<T> result = empty(equality);
        for(T el: elements) {
            result = result.added(el);
        }
        return result;
    }

    private AHashSet(AHashMap<T, Boolean> inner) {
        this.inner = inner;
    }

    public int size() {
        return inner.size();
    }
    public boolean isEmpty() {
        return inner.isEmpty();
    }
    public boolean nonEmpty() {
        return inner.nonEmpty();
    }

    public boolean contains(T el) {
        return inner.containsKey(el);
    }

    public AHashSet<T> added(T el) {
        return new AHashSet<> (inner.updated(el, true));
    }

    public AHashSet<T> removed(T el) {
        return new AHashSet<> (inner.removed(el));
    }

    public java.util.Set<T> asJavaUtilSet() {
        return inner.asJavaUtilMap().keySet();
    }

    @Override
    public Iterator<T> iterator() {
        return asJavaUtilSet().iterator();
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(! (o instanceof AHashSet)) {
            return false;
        }
        return inner.equals(((AHashSet)o).inner);
    }

    public AList<T> toList() {
        return AList.create(this);
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

    public <E extends Exception> AOption<T> find(APredicate<T, E> pred) throws E {
        for(T el: this) {
            if(pred.apply(el)) {
                return AOption.some(el);
            }
        }
        return AOption.none();
    }

    public <X, E extends Exception> AHashSet<X> map(AFunction1<X, T, E> f) throws E {
        final List<X> result = new ArrayList<>(); // list instead of set to support arbitrary equality implementations
        for(T el: this) {
            result.add(f.apply(el));
        }
        return create(inner.equality, result);
    }

    public <E extends Exception> AHashSet<T> filter(APredicate<T, E> pred) throws E {
        final List<T> result = new ArrayList<>();
        for(T el: this) {
            if(pred.apply(el)) {
                result.add(el);
            }
        }
        return create(inner.equality, result);
    }
}
