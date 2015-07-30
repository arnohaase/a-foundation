package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.*;

import java.io.Serializable;
import java.util.*;


/**
 * This class represents an object that may or may not be present. It is intended as a more explicit, less error prone
 *  replacement for using <code>null</code> to represent the absence of a value.<p>
 *
 * There are two flavors of <code>AOption</code>: <code>AOption.some(...)</code> and <code>AOption.none()</code>.
 *  <code>AOption.some(x)</code> represents the presence of a value, namely 'x', while <code>AOption.none()</code>
 *  represents 'no value'.<p>
 *
 * There are two key reasons for using this class instead of using <code>null</code> to encode the absence of a value.<p>
 *
 * Firstly, <code>AOption</code> explicitly shows in the code that a value is optional, forcing code to explicitly deal
 *  with that fact. A method returning a value that may or may not be present (e.g. a call to <code>get(...)</code> on
 *  a map) can specify AOption as its return type to make that characteristic explicit.<p>
 *
 * Secondly, code can operate on an <code>AOption</code> regardless of whether there is a value or not (e.g. by calling
 *  <code>map()</code>). This functional style of programming can make for code that is easier to read and less cluttered
 *  than having <code>if</code> statements checking for the presence of values all over the place.<p>
 *
 * This class was inspired by the <code>Option</code> class from the Scala standard library. Thanks for the excellent
 *  code, Scala team!
 *
 * @author arno
 */
public abstract class AOption<T> implements ACollection<T>, Serializable {
    /**
     * @return a 'some' filled with the literal passed in as a parameter
     */
    public static <T> AOption<T> some(T el) {
        return new ASome<>(el);
    }

    /**
     * @return the 'none'
     */
    @SuppressWarnings("unchecked")
    public static <T> AOption<T> none() {
        return  (AOption<T>) ANone.INSTANCE;
    }

    /**
     * This is a convenience method that creates an AOption based on Java conventions.
     *
     * @return 'none()' if the parameter is null, 'some(nullable)' otherwise
     */
    public static <T> AOption<T> fromNullable(T nullable) {
        return nullable != null ? some(nullable) : AOption.<T>none();
    }

    /**
     * @return true if this AOption holds a value
     */
    public abstract boolean isDefined();

    /**
     * @return true if this option does not hold a value
     */
    public boolean isEmpty() {
        return !isDefined();
    }

    /**
     * @return the element held in this AOption if there is one; otherwise, a NoSuchElementException is thrown
     */
    public abstract T get();

    /**
     * @return the element held in this AOption if there is one, and the default element passed in as  parameter otherwise
     */
    public abstract T getOrElse (T defaultValue);

    /**
     * @return the element held in this AOption if there is one, and the result of evaluating the function passed in otherwise. The function is
     *  guaranteed to be evaluated only if this AOption is empty.
     */
    public abstract <E extends Exception> T getOrElseEval (AFunction0<T,E> producer) throws E;

    /**
     * This method returns the element contained in this AOption if it is defined. Otherwise, it evaluates the function passed in and throws the exception
     *  returned by that function. The function is guaranteed to be evaluated only if this AOption is empty.
     */
    public abstract <E extends Exception> T getOrElseThrow (AFunction0NoThrow<E> producer) throws E;

    @Override public <X, E extends Exception> ACollection<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E {
        throw new UnsupportedOperationException("AOption can not be flattened");
    }

    @Override public <X> ACollection<X> flatten() {
        throw new UnsupportedOperationException("AOption can not be flattened");
    }

    @Override public String mkString() {
        return ACollectionHelper.mkString (this);
    }

    @Override public String mkString(String separator) {
        return ACollectionHelper.mkString(this, separator);
    }

    @Override public String mkString(String prefix, String separator, String suffix) {
        return ACollectionHelper.mkString(this, prefix, separator, suffix);
    }

    public abstract boolean equals(Object o);
    public abstract int hashCode();

    @Override public Collection<T> asJavaUtilCollection () {
        return new AbstractCollection<T> () {
            @SuppressWarnings ("NullableProblems")
            @Override public Iterator<T> iterator () {
                return AOption.this.iterator ();
            }
            @Override public int size () {
                return iterator ().hasNext () ? 1 : 0;
            }
        };
    }

    static class ASome<T> extends AOption<T> {
        private final T el;

        ASome(T el) {
            this.el = el;
        }

        @Override public T get() {
            return el;
        }

        @Override public T getOrElse (T defaultValue) {
            return el;
        }

        @Override public <E extends Exception> T getOrElseEval (AFunction0<T, E> producer) throws E {
            return el;
        }

        @Override public <E extends Exception> T getOrElseThrow (AFunction0NoThrow<E> producer) throws E {
            return el;
        }

        @Override public boolean isDefined() {
            return true;
        }

        @Override public ACollection<T> clear () {
            return none ();
        }

        @Override public <E extends Exception> void forEach(AStatement1<? super T, E> f) throws E {
            f.apply(el);
        }

        @Override public <E extends Exception> AOption<T> find(APredicate<? super T, E> pred) throws E {
            return filter(pred);
        }

        @Override public <X,E extends Exception> AOption<X> map(AFunction1<? super T, ? extends X, E> f) throws E {
            return some (f.apply (el));
        }

        @Override public <X, E extends Exception> ACollection<X> collect (APartialFunction1<? super T, ? extends X, E> pf) throws E {
            if (pf.isDefinedAt (el)) {
                return some (pf.apply (el));
            } else {
                return none();
            }
        }

        @Override public <E extends Exception> AOption<T> filter(APredicate<? super T, E> pred) throws E {
            if(pred.apply(el))
                return this;
            else
                return none();
        }

        @Override public <R, E extends Exception> R foldLeft (R startValue, AFunction2<R, ? super T, R, E> f) throws E {
            return f.apply(startValue, el);
        }

        @Override public int size() {
            return 1;
        }

        @Override public boolean nonEmpty() {
            return true;
        }

        @Override public <E extends Exception> boolean forAll(APredicate<? super T, E> pred) throws E {
            return find(pred).isDefined();
        }

        @Override public <E extends Exception> boolean exists(APredicate<? super T, E> pred) throws E {
            return find(pred).isDefined();
        }

        @Override public <X, E extends Exception> AMap<X, AOption<T>> groupBy(AFunction1<? super T, ? extends X, E> f) throws E {
            return groupBy(f, AEquality.EQUALS);
        }

        @Override public <X, E extends Exception> AMap<X, AOption<T>> groupBy(AFunction1<? super T, ? extends X, E> f, AEquality keyEquality) throws E { //TODO junit
            final AMap<X, AOption<T>> result = AHashMap.empty(keyEquality);
            return result.updated(f.apply(el), this);
        }

        @Override public AList<T> toList() {
            return AList.<T>nil().cons(el);
        }

        @Override public ASet<T> toSet() {
            return AHashSet.<T>empty ().added(el);
        }

        @Override public ASet<T> toSet(AEquality equality) {
            return AHashSet.<T>empty (equality).added(el);
        }

        @SuppressWarnings("NullableProblems")
        @Override public Iterator<T> iterator() {
            return Collections.singletonList(el).iterator();
        }

        @Override public String toString() {
            return "AOption.some(" + el + ")";
        }

        @SuppressWarnings("RedundantIfStatement")
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

        @Override public boolean contains(Object o) {
            return AEquality.EQUALS.equals(el, o);
        }

//        @SuppressWarnings("NullableProblems")
//        @Override public Object[] toArray() {
//            return new Object[] {el};
//        }
//
//        @SuppressWarnings({"NullableProblems", "SuspiciousToArrayCall"})
//        @Override public <T1> T1[] toArray(T1[] a) {
//            return Arrays.asList(el).toArray(a);
//        }
//
//        @Override public boolean add(T t) {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override public boolean remove(Object o) {
//            throw new UnsupportedOperationException();
//        }
//
//        @SuppressWarnings({"NullableProblems", "SimplifiableIfStatement"})
//        @Override public boolean containsAll(Collection<?> c) {
//            if(c.isEmpty()) {
//                return true;
//            }
//            return c.size() == 1 && c.contains(el);
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public boolean addAll(Collection<? extends T> c) {
//            throw new UnsupportedOperationException();
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public boolean removeAll(Collection<?> c) {
//            throw new UnsupportedOperationException();
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public boolean retainAll(Collection<?> c) {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override public void clear() {
//            throw new UnsupportedOperationException();
//        }
    }

    static class ANone extends AOption<Object> {
        public static final ANone INSTANCE = new ANone();

        private ANone() {}

        private Object readResolve() {
            return INSTANCE;
        }

        @Override public ACollection<Object> clear () {
            return this;
        }

        @Override public Object get() {
            throw new NoSuchElementException("no value for ANone");
        }

        @Override public Object getOrElse (Object defaultValue) {
            return defaultValue;

        }
        @Override public <E extends Exception> Object getOrElseEval (AFunction0<Object, E> producer) throws E {
            return producer.apply ();
        }

        @Override public <E extends Exception> Object getOrElseThrow (AFunction0NoThrow<E> producer) throws E {
            throw producer.apply ();
        }

        @Override public boolean isDefined() {
            return false;
        }

        @Override public <E extends Exception> void forEach(AStatement1<? super Object, E> f) {
            // nothing to be done
        }

        @Override public <E extends Exception> AOption<Object> find(APredicate<? super Object, E> pred) throws E {
            return none();
        }

        @Override public <X,E extends Exception> AOption<X> map(AFunction1<? super Object, ? extends X, E> f) {
            return none();
        }

        @Override public <X, E extends Exception> ACollection<X> collect (APartialFunction1<? super Object, ? extends X, E> pf) throws E {
            return none();
        }

        @Override public <R, E extends Exception> R foldLeft (R startValue, AFunction2<R, ? super Object, R, E> f) throws E {
            return startValue;
        }

        @Override public <E extends Exception> AOption<Object> filter(APredicate<? super Object, E> pred) {
            return none();
        }

        @Override public boolean nonEmpty() {
            return true;
        }

        @Override public int size() {
            return 0;
        }

        @Override public <E extends Exception> boolean forAll(APredicate<? super Object, E> pred) throws E {
            return true;
        }

        @Override public <E extends Exception> boolean exists(APredicate<? super Object, E> pred) throws E {
            return false;
        }

        @Override public <X, E extends Exception> AMap<X, AOption<Object>> groupBy(AFunction1<? super Object, ? extends X, E> f) throws E {
            return AHashMap.empty(); //TODO junit
        }

        @Override public <X, E extends Exception> AMap<X, AOption<Object>> groupBy(AFunction1<? super Object, ? extends X, E> f, AEquality keyEquality) throws E {
            return AHashMap.empty(keyEquality); //TODO junit
        }

        @Override public AList<Object> toList() {
            return AList.nil();
        }

        @Override public ASet<Object> toSet() {
            return AHashSet.empty ();
        }

        @Override public ASet<Object> toSet(AEquality equality) {
            return AHashSet.empty (equality);
        }

        @SuppressWarnings("NullableProblems")
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

        @Override public boolean contains(Object o) {
            return false;
        }

//        @SuppressWarnings("NullableProblems")
//        @Override public Object[] toArray() {
//            return new Object[0];
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public <T> T[] toArray(T[] a) {
//            if(a.length > 0) {
//                a[0] = null;
//            }
//            return a;
//        }
//
//        @Override
//        public boolean add(Object o) {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override public boolean remove(Object o) {
//            throw new UnsupportedOperationException();
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public boolean containsAll(Collection<?> c) {
//            return c.isEmpty();
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public boolean addAll(Collection<?> c) {
//            throw new UnsupportedOperationException();
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public boolean removeAll(Collection<?> c) {
//            throw new UnsupportedOperationException();
//        }
//
//        @SuppressWarnings("NullableProblems")
//        @Override public boolean retainAll(Collection<?> c) {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override
//        public void clear() {
//            throw new UnsupportedOperationException();
//        }
    }
}

