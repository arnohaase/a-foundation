package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.AFunction2;
import com.ajjpj.abase.function.APredicate;
import com.ajjpj.abase.function.AStatement1;


/**
 * This interface represents collections that can be traversed "as whole" but not necessarily stepwise. Persistent collections
 *  are typical examples of this: They require cleanup (resource deallocation) after iterating is through, and exporting a
 *  <code>java.util.Iterator</code> can not guarantee that.
 *
 * @author arno
 */
public interface ATraversable<T> extends AMonadicOps<T> {
    <E extends Exception> void forEach(AStatement1<? super T, E> f) throws E;

    @Override <E extends Exception> ATraversable<T> filter(APredicate<? super T, E> pred) throws E;
    @Override <X, E extends Exception> ATraversable<X> map(AFunction1<? super T, ? extends X, E> f) throws E;
    @Override <X, E extends Exception> ATraversable<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E;
    <R, E extends Exception> R foldLeft(R startValue, AFunction2<R, ? super T, R, E> f) throws E;

    @Override <E extends Exception> AOption<T> find(APredicate<? super T, E> pred) throws E;

    @Override <X> ATraversable<X> flatten();

    @Override <E extends Exception> boolean forAll(APredicate<? super T, E> pred) throws E;
    @Override <E extends Exception> boolean exists(APredicate<? super T, E> pred) throws E;
}
