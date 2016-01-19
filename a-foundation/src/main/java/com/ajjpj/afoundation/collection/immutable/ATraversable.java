package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.function.*;


/**
 * This interface represents collections that can be traversed "as whole" but not necessarily stepwise. Persistent collections
 *  are typical examples of this: They require cleanup (resource deallocation) after iterating is through, and exporting a
 *  <code>java.util.Iterator</code> can not guarantee that.
 *
 * @author arno
 */
public interface ATraversable<T> extends AMonadicOps<T> {
    /**
     * Executes the statement {@code f} for each element of the {@code ATraversable}.
     */
    <E extends Throwable> void foreach(AStatement1<? super T, E> f) throws E;

    @Override <E extends Throwable> ATraversable<T> filter(APredicate<? super T, E> pred) throws E;
    @Override <X, E extends Throwable> ATraversable<X> map(AFunction1<? super T, ? extends X, E> f) throws E;
    @Override <X, E extends Throwable> ATraversable<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E;
    @Override <X, E extends Throwable> ATraversable<X> collect (APartialFunction<? super T, ? extends X, E> pf) throws E;

    /**
     * This method 'folds' the elements of this collection into a single value. It iterates over the elements, passing the
     *  intermediate result and the element to the aggregation function {@code f}. <p>
     *
     * For example, if this collections holds elements of type Integer, you could compute the sum of all elements by calling
     *  {@code foldLeft (0, (x,y) -> x+y); }.
     *
     * @param startValue initial value that is used to initialize the intermediate result
     * @param f the aggregation function
     * @return the resulting value of the aggregation
     */
    <R, E extends Throwable> R foldLeft(R startValue, AFunction2<R, ? super T, R, E> f) throws E;

    @Override <E extends Throwable> AOption<T> find(APredicate<? super T, E> pred) throws E;

    @Override <X> ATraversable<X> flatten();

    @Override <E extends Throwable> boolean forAll(APredicate<? super T, E> pred) throws E;
    @Override <E extends Throwable> boolean exists(APredicate<? super T, E> pred) throws E;
}
