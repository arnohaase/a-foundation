package com.ajjpj.abase.function;

/**
 * Represents a function that takes a single parameter and returns nothing.
 *
 * @author arno
 */
public interface AStatement1<P, E extends Exception> {
    void apply(P param) throws E;
}
