package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.APartialFunction1;
import com.ajjpj.afoundation.function.APredicate;

import java.util.Collection;


/**
 * This interface contains methods provided by all a-base collections. Implementing <code>Iterable</code> allows
 *  collections to be used with Java's <code>for(....: collection)</code> syntax introduced with version 1.5.<p>
 *
 * The first generic parameter T represents the collection's element type, while the second parameter represents
 *  the concrete collection class.
 *
 * @author arno
 */
public interface ACollection<T> extends ATraversable<T>, Iterable<T> {
    /**
     * @return an empty {@link ACollection} of the same size as this one.
     */
    ACollection<T> clear();

    /**
     * @return the number of elements in this {@link ACollection}.
     */
    int size();

    /**
     * @return true if and only if this collection's size is 0
     */
    boolean isEmpty();

    /**
     * @return true if and only if this collection's size is greater than 0
     */
    boolean nonEmpty();

    /**
     * This method checks if the collection has an element that is 'equal' to the parameter {@code el}. For collections
     *  that have a configurable {@link com.ajjpj.afoundation.collection.AEquality}, that equality is used to check
     *  containment. This applies to {@link ASet} instances in particular.
     */
    boolean contains (T el);

    /**
     * Filters this collection's elements, this method returns a new collection comprised of only those elements that match
     *  a given predicate.
     */
    @Override <E extends Exception> ACollection<T> filter(APredicate<? super T, E> pred) throws E;

    @Override <X, E extends Exception> ACollection<X> map(AFunction1<? super T, ? extends X, E> f) throws E;
    @Override <X, E extends Exception> ACollection<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E;

    /**
     * Turns a collection of collections into a 'flat' collection, removing one layer of collections.
     */
    <X> ACollection<X> flatten();

    @Override <X, E extends Exception> ACollection<X> collect (APartialFunction1<? super T, ? extends X, E> pf) throws E;

    /**
     * Creates a map from this collection, applying a function to every element in order to determine that element's key. All
     *  elements with the same key (more precisely, with equal keys) are stored in a collection, and the resulting map
     *  holds that collection of elements for every key.<p>
     *
     * This method can e.g. be used to transform a collection of strings into a map from string lengths to all elements of a
     *  given length.
     */
    <X, E extends Exception> AMap<X, ? extends ACollection<T>> groupBy(AFunction1<? super T, ? extends X, E> f) throws E;

    /**
     * Creates a map from this collection, applying a function to every element in order to determine that element's key. All
     *  elements with the same key (in terms of the provided equality strategy) are stored in a collection, and the resulting map
     *  holds that collection of elements for every key.<p>
     *
     * This method can e.g. be used to transform a collection of strings into a map from string lengths to all elements of a
     *  given length.
     */
    <X, E extends Exception> AMap<X, ? extends ACollection<T>> groupBy(AFunction1<? super T, ? extends X, E> f, AEquality keyEquality) throws E;

    /**
     * Returns an AList instance with this collection's elements.
     */
    AList<T> toList();

    /**
     * Returns an AHashSet instance with this collection's elements.
     */
    ASet<T> toSet();

    /**
     * Returns an AHashSet instance with this collection's elements.
     */
    ASet<T> toSet(AEquality equality);

    /**
     * Creates a string with all elements of this collections, separated by commas.
     */
    String mkString();

    /**
     * Creates a string with all elements of this collections, separated by a provided separator.
     */
    String mkString(String separator);

    /**
     * Creates a string with all elements of this collections, separated by a configurable separator and
     *  delimited by a provided prefix and suffix.
     */
    String mkString(String prefix, String separator, String suffix);

    /**
     * Creates a java.util.Collection wrapper around this collection and returns that view.
     */
    Collection<T> asJavaUtilCollection();
}

