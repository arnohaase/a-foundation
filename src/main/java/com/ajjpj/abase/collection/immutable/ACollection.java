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
public interface ACollection<T, C extends ACollection<T, C>> extends Iterable<T> {
    int size();
    boolean isEmpty();
    boolean nonEmpty();

    /**
     * Applies a transformation function to each element, creating a new collection instance from the results. For a
     *  collection of strings, this could e.g. be used to create a collection of integer values with each string's length.
     */
    <X,E extends Exception> ACollection<X, ? extends ACollection<X, ?>> map(AFunction1<X, T, E> f) throws E;

    /**
     * Filters this collection's elements, this method returns a new collection comprised of only those elements that match
     *  a given predicate.
     */
    <E extends Exception> C filter(APredicate<T, E> pred) throws E;

    /**
     * Wraps a filter around the existing collection, making creation of the result a constant time operation. This comes
     *  at the price that the actual filtering is done for every iteration.
     */
//    <E extends Exception> ACollection<T, ? extends ACollection<T, ?>> withFilter(APredicate<T, E> pred) throws E; //TODO withFilter

    /**
     * Searches through this collection's elements and returns the first element matching a given predicate. if any.
     */
    <E extends Exception> AOption<T> find(APredicate<T, E> pred) throws E;

    /**
     * Returns true iff all elements of the collection match the predicate.
     */
    <E extends Exception> boolean forAll(APredicate<T, E> pred) throws E;

    /**
     * Returns true iff at least one element of the collection matches the predicate.
     */
    <E extends Exception> boolean exists(APredicate<T, E> pred) throws E;

    AList<T> toList();
    AHashSet<T> toSet();
    AHashSet<T> toSet(AEquality equality);

    String mkString();
    String mkString(String separator);
    String mkString(String prefix, String separator, String suffix);
}
//TODO javadoc
