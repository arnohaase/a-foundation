package com.ajjpj.afoundation.function;

/**
 * @author Roman Krüger
 */
public interface APartialFunction1<P, R, E extends Exception> extends AFunction1<P, R, E> {
    boolean isDefinedAt (P param);
}
