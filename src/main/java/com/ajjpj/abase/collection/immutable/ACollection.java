package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AEquality;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.APredicate;

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
public interface ACollection<T> extends ATraversable<T>, Collection<T> { //TODO Iterable<T> instead of Collection<T>
    int size();
    boolean isEmpty();
    boolean nonEmpty();

    /**
     * Filters this collection's elements, this method returns a new collection comprised of only those elements that match
     *  a given predicate.
     */
    <E extends Exception> ACollection<T> filter(APredicate<? super T, E> pred) throws E;

    /**
     * Turns a collection of collections into a 'flat' collection, removing one layer of collections.
     */
    <X> ACollection<X> flatten();

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
    AHashSet<T> toSet();

    /**
     * Returns an AHashSet instance with this collection's elements.
     */
    AHashSet<T> toSet(AEquality equality);

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
}

