package com.ajjpj.afoundation.collection.immutable;


import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.APartialFunction;
import com.ajjpj.afoundation.function.APredicate;


/**
 * This interface represents an immutable set. It inherits most methods from ACollection, but it
 *  prescribes uniqueness semantics.<p>
 *
 * Implementations of {@link ACollection#map(com.ajjpj.afoundation.function.AFunction1)} and
 *  {@link ACollection#flatMap(com.ajjpj.afoundation.function.AFunction1)} do <em>not</em>
 *  return instances of {@code ASet}, but rather generic collections without uniqueness guarantees. If {@code ASet} semantics are
 *  required, call {@link ACollection#toSet()} on the results.
 *
 * @author arno
 */
public interface ASet<T> extends ACollection<T> {
    @Override <E extends Throwable> ASet<T> filter(APredicate<? super T, E> pred) throws E;

    @Override <X, E extends Throwable> ASet<X> map(AFunction1<? super T, ? extends X, E> f) throws E;
    @Override <X, E extends Throwable> ASet<X> flatMap(AFunction1<? super T, ? extends Iterable<X>, E> f) throws E;
    @Override <X, E extends Throwable> ASet<X> collect (APartialFunction<? super T, ? extends X, E> pf) throws E;

    @Override <X> ASet<X> flatten ();

    /**
     * @return the specification of equality on which this set maintains uniqueness.
     *         {@link com.ajjpj.afoundation.collection.AEquality#EQUALS} uses {@link Object#equals(Object)}
     *         and represents the 'normal' equality used by {@link java.util.Set}.
     */
    AEquality equalityForEquals();

    /**
     * @return a {@link java.util.Set} 'view' of this set's values, i.e. an read-only instance
     *         of {@link java.util.Set} that contains the exact same elements as this {@code ASet}.
     *         This operation performs <em>no</em> copying, and the returned collection is based
     *         on this {@code ASet}'s equality.
     */
    java.util.Set<T> asJavaUtilSet();

    /**
     * @return a new {@link ASet} that is guaranteed to contain the new element, i.e. to contain an
     *         element which this set's {@link com.ajjpj.afoundation.collection.AEquality} considers
     *         equal to the parameter {@code el}.
     */
    ASet<T> added (T el);

    /**
     * @return a new {@link ASet} that is guaranteed not to contain the parameter {@code el}, i.e. not
     *          to contain an element that this set's {@link com.ajjpj.afoundation.collection.AEquality}
     *          considers equal to the parameter {@code el}.
     */
    ASet<T> removed (T el);
}
