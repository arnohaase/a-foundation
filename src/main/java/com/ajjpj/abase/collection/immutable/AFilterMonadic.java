package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;


/**
 * @author arno
 */
public interface AFilterMonadic<T, C extends AFilterMonadic<T, C>> extends Iterable<T> {
    /**
     * Applies a transformation function to each element, creating a new collection instance from the results. For a
     *  collection of strings, this could e.g. be used to create a collection of integer values with each string's length.
     */
    <X,E extends Exception> AFilterMonadic<X, ? extends AFilterMonadic<X, ?>> map(AFunction1<X, T, E> f) throws E;

    /**
     * Maps each element to a collection of values, flattening the results into a single collection. Let there be a
     *  collection of strings. Calling <code>flatMap</code> with a function that splits a string into tokens, the result
     *  would be a collection of all tokens of all original strings.
     */
    <X, E extends Exception> AFilterMonadic<X, ? extends AFilterMonadic<X, ?>> flatMap(AFunction1<Iterable<X>, T, E> f) throws E;

    /**
     * Wraps a filter around the existing collection, making creation of the result a constant time operation. This comes
     *  at the price that the actual filtering is done for every iteration.
     */
//TODO    <E extends Exception> AFilterMonadic<T, ? extends AFilterMonadic<T, ?>> withFilter(APredicate<T, E> pred) throws E;

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

    AList<T> toList(); //TODO javadoc
    AHashSet<T> toSet();
    AHashSet<T> toSet(AEquality equality);

    String mkString();
    String mkString(String separator);
    String mkString(String prefix, String separator, String suffix);
}
