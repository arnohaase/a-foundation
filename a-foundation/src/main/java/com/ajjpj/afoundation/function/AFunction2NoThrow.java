package com.ajjpj.afoundation.function;

/**
 * Represents a function that takes two arguments and produces a result.
 * This function does not throw an exception.
 *
 * @param <P1> 1st parameter type
 * @param <P2> 2nd parameter type
 * @param <R> return type
 *
 * @author bitmagier
 */
public interface AFunction2NoThrow<P1, P2, R> extends AFunction2<P1, P2, R, RuntimeException> {
    @Override R apply (P1 param1, P2 param2);
}
