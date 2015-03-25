package com.ajjpj.afoundation.function;

import java.io.Serializable;


/**
 * Represents a function that takes a single argument and produces a result.
 *
 * @param <P> parameter type
 * @param <R> return type
 *
 * @author arno
 */
public interface AFunction1<P, R, E extends Exception> extends Serializable {
    R apply(P param) throws E;
}
