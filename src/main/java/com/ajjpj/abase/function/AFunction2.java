package com.ajjpj.abase.function;

/**
 * Represents a function that takes two arguments and produces a result.
 *
 * @param <P1> 1st parameter type
 * @param <P2> 2nd parameter type
 * @param <R> return type
 *
 * @author bitmagier
 */
public interface AFunction2<P1, P2, R, E extends Exception> {
    R apply (P1 param1, P2 param2) throws E;
}
