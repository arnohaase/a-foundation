package com.ajjpj.afoundation.function;

/**
 * Represents a function that takes three arguments and produces a result.
 * This function does not throw an exception.
 *
 * @param <P1> 1st parameter type
 * @param <P2> 2nd parameter type
 * @param <P3> 3rd parameter type
 * @param <R> return type
 *
 * @author arno
 */
public interface AFunction3NoThrow<P1, P2, P3, R> extends AFunction3<P1, P2, P3, R, RuntimeException> {
    @Override R apply (P1 param1, P2 param2, P3 param3);
}
