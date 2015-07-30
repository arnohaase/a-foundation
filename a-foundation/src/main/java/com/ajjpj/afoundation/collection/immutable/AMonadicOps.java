package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.APartialFunction1;
import com.ajjpj.afoundation.function.APredicate;

/**
 * This interface contains those functional operations that are supported for all kinds of collections. This is the greatest common
 *  denominator of all a-base collection classes.
 *
 * @author arno
 */
public interface AMonadicOps<T> {
    /**
     * Filters this collection's elements, this method returns a new collection comprised of only those elements that match
     *  a given predicate.
     */
    <E extends Exception> AMonadicOps<T> filter(APredicate<? super T, E> pred) throws E;

    /**
     * Turns a collection of collections into a 'flat' collection, removing one layer of collections.
     */
    <X> AMonadicOps<X> flatten();

    /**
     * Applies a transformation function to each element, creating a new collection instance from the results. For a
     *  collection of strings, this could e.g. be used to create a collection of integer values with each string's length.
     */
    <X,E extends Exception> AMonadicOps<X> map(AFunction1<? super T, ? extends X, E> f) throws E;

    /**
     * Maps each element to a collection of values, flattening the results into a single collection. Let there be a
     *  collection of strings. Calling <code>flatMap</code> with a function that splits a string into tokens, the result
     *  would be a collection of all tokens of all original strings.
     */
    <X, E extends Exception> AMonadicOps<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E;

    /**
     * Applies a transformation function to all elements of a collection, where the partial function is defined for. Creates a new collection
     *   of the transformed elements only. So the number of result elements may be less than the number of elements in the source collection.
     */
    <X, E extends Exception> AMonadicOps<X> collect (APartialFunction1<? super T, ? extends X, E> pf) throws E;

    /**
     * Wraps a filter around the existing collection, making creation of the result a constant time operation. This comes
     *  at the price that the actual filtering is done for every iteration.
     */
//TODO    <E extends Exception> AMonadicOps<T, ? extends AFilterMonadic<T, ?>> withFilter(APredicate<T, E> pred) throws E;

    /**
     * Searches through this collection's elements and returns the first element matching a given predicate. if any.
     */
    <E extends Exception> AOption<T> find(APredicate<? super T, E> pred) throws E;

    /**
     * Returns true iff all elements of the collection match the predicate.
     */
    <E extends Exception> boolean forAll(APredicate<? super T, E> pred) throws E;

    /**
     * Returns true iff at least one element of the collection matches the predicate.
     */
    <E extends Exception> boolean exists(APredicate<? super T, E> pred) throws E;
}
