package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.ACollectionHelper;
import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * This class represents an object that may or may not be present. It is intended as a more explicit, less error prone
 *  replacement for using <code>null</code> to represent the absence of a value.<p />
 *
 * There are two flavors of <code>AOption</code>: <code>AOption.some(...)</code> and <code>AOption.none()</code>.
 *  <code>AOption.some(x)</code> represents the presence of a value, namely 'x', while <code>AOption.none()</code>
 *  represents 'no value'.<p />
 *
 * There are two key reasons for using this class instead of using <code>null</code> to encode the absence of a value.<p />
 *
 * Firstly, <code>AOption</code> explicitly shows in the code that a value is optional, forcing code to explicitly deal
 *  with that fact. A method returning a value that may or may not be present (e.g. a call to <code>get(...)</code> on
 *  a map) can specify AOption as its return type to make that characteristic explicit.<p />
 *
 * Secondly, code can operate on an <code>AOption</code> regardless of whether there is a value or not (e.g. by calling
 *  <code>map()</code>). This functional style of programming can make for code that is easier to read and less cluttered
 *  than having <code>if</code> statements checking for the presence of values all over the place.<p />
 *
 * This class was inspired by the <code>Option</code> class from the Scala standard library. Thanks for the excellent
 *  code, Scala team!
 *
 * @author arno
 */
public abstract class AOption<T> implements ACollection<T, AOption<T>> {
    public static <T> AOption<T> some(T el) {
        return new ASome<>(el);
    }

    @SuppressWarnings("unchecked")
    public static <T> AOption<T> none() {
        return  (AOption<T>) ANone.INSTANCE;
    }

    public static <T> AOption<T> fromNullable(T nullable) {
        return nullable != null ? some(nullable) : AOption.<T>none();
    }

    public abstract boolean isDefined();
    public boolean isEmpty() {
        return !isDefined();
    }

    public abstract T get();
    public T getOrElse(T el) {
        return isDefined() ? get() : el;
    }

    @Override public <X, E extends Exception> AFilterMonadic<X, ? extends AFilterMonadic<X, ?>> flatMap(AFunction1<Iterable<X>, T, E> f) throws E {
        throw new UnsupportedOperationException("AOption can not be flattened");
    }

    @Override public <X> ACollection<X, ? extends ACollection<X, ?>> flatten() {
        throw new UnsupportedOperationException("AOption can not be flattened");
    }

    @Override public String mkString() {
        return ACollectionHelper.mkString(this);
    }

    @Override public String mkString(String separator) {
        return ACollectionHelper.mkString(this, separator);
    }

    @Override public String mkString(String prefix, String separator, String suffix) {
        return ACollectionHelper.mkString(this, prefix, separator, suffix);
    }


    public abstract boolean equals(Object o);
    public abstract int hashCode();

    static class ASome<T> extends AOption<T> {
        private final T el;

        ASome(T el) {
            this.el = el;
        }

        @Override public T get() {
            return el;
        }

        @Override public boolean isDefined() {
            return true;
        }

        @Override public <E extends Exception> AOption<T> find(APredicate<T, E> pred) throws E {
            return filter(pred);
        }

        @Override public <X,E extends Exception> AOption<X> map(AFunction1<X, T, E> f) throws E {
            return some(f.apply(el));
        }

        @Override public <E extends Exception> AOption<T> filter(APredicate<T, E> pred) throws E {
            if(pred.apply(el))
                return this;
            else
                return none();
        }

        @Override public int size() {
            return 1;
        }

        @Override public boolean nonEmpty() {
            return true;
        }

        @Override public <E extends Exception> boolean forAll(APredicate<T, E> pred) throws E {
            return find(pred).isDefined();
        }

        @Override public <E extends Exception> boolean exists(APredicate<T, E> pred) throws E {
            return find(pred).isDefined();
        }

        @Override public <X, E extends Exception> AMap<X, AOption<T>> groupBy(AFunction1<X, T, E> f) throws E {
            return groupBy(f, AEquality.EQUALS);
        }

        @Override public <X, E extends Exception> AMap<X, AOption<T>> groupBy(AFunction1<X, T, E> f, AEquality keyEquality) throws E { //TODO junit
            final AMap<X, AOption<T>> result = AHashMap.empty(keyEquality);
            return result.updated(f.apply(el), this);
        }

        @Override public AList<T> toList() {
            return AList.<T>nil().cons(el);
        }

        @Override public AHashSet<T> toSet() {
            return AHashSet.<T>empty().added(el);
        }

        @Override public AHashSet<T> toSet(AEquality equality) {
            return AHashSet.<T>empty(equality).added(el);
        }

        @Override public Iterator<T> iterator() {
            return Collections.singletonList(el).iterator();
        }

        @Override public String toString() {
            return "AOption.some(" + el + ")";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ASome aSome = (ASome) o;

            if (el != null ? !el.equals(aSome.el) : aSome.el != null) return false;

            return true;
        }

        @Override public int hashCode() {
            return el != null ? el.hashCode() : 0;
        }
    }

    static class ANone extends AOption<Object> {
        public static final ANone INSTANCE = new ANone();

        private ANone() {}

        @Override public Object get() {
            throw new NoSuchElementException("no value for ANone");
        }

        @Override public boolean isDefined() {
            return false;
        }

        @Override public <E extends Exception> AOption<Object> find(APredicate<Object, E> pred) throws E {
            return none();
        }

        @Override public <X,E extends Exception> AOption<X> map(AFunction1<X, Object, E> f) {
            return none();
        }

        @Override public <E extends Exception> AOption<Object> filter(APredicate<Object, E> pred) {
            return none();
        }

        @Override public boolean nonEmpty() {
            return true;
        }

        @Override public int size() {
            return 0;
        }

        @Override public <E extends Exception> boolean forAll(APredicate<Object, E> pred) throws E {
            return true;
        }

        @Override public <E extends Exception> boolean exists(APredicate<Object, E> pred) throws E {
            return false;
        }

        @Override public <X, E extends Exception> AMap<X, AOption<Object>> groupBy(AFunction1<X, Object, E> f) throws E {
            return AHashMap.empty(); //TODO junit
        }

        @Override public <X, E extends Exception> AMap<X, AOption<Object>> groupBy(AFunction1<X, Object, E> f, AEquality keyEquality) throws E {
            return AHashMap.empty(keyEquality); //TODO junit
        }

        @Override public AList<Object> toList() {
            return AList.nil();
        }

        @Override public AHashSet<Object> toSet() {
            return AHashSet.empty();
        }

        @Override public AHashSet<Object> toSet(AEquality equality) {
            return AHashSet.empty(equality);
        }

        @Override public Iterator<Object> iterator() {
            return Collections.emptyList().iterator();
        }

        @Override public String toString() {
            return "AOption.none()";
        }

        @Override public boolean equals(Object o) {
            return o == this;
        }

        @Override public int hashCode() {
            return 0;
        }
    }
}

