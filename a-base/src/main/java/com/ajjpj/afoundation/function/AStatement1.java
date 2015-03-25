package com.ajjpj.afoundation.function;

import java.io.Serializable;


/**
 * Represents a function that takes a single parameter and returns nothing.
 *
 * @author arno
 */
public interface AStatement1<P, E extends Exception> extends Serializable {
    void apply(P param) throws E;
}
