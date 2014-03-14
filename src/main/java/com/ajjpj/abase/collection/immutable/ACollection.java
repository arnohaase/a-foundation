package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;


/**
 * This interface contains methods provided by all a-base collections. Implementing <code>Iterable</code> allows
 *  collections to be used with Java's <code>for(....: collection)</code><p /> syntax introduced with version 1.5.<p />
 *
 * The first generic parameter T represents the collection's element type, while the second parameter represents
 *  the concrete collection class.
 *
 * @author arno
 */
public interface ACollection<T, C extends ACollection<T, C>> extends AFilterMonadic<T, C> {
    int size();
    boolean isEmpty();
    boolean nonEmpty();

    /**
     * Filters this collection's elements, this method returns a new collection comprised of only those elements that match
     *  a given predicate.
     */
    <E extends Exception> C filter(APredicate<T, E> pred) throws E;

    /**
     * Turns a collection of collections into a 'flat' collection, removing one layer of collections.
     */
    <X> ACollection<X, ? extends ACollection<X, ?>> flatten();

    <X, E extends Exception> AMap<X, C> groupBy(AFunction1<X,T,E> f) throws E; //TODO javadoc, junit
    <X, E extends Exception> AMap<X, C> groupBy(AFunction1<X,T,E> f, AEquality keyEquality) throws E; //TODO javadoc, junit
}

