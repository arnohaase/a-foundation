package com.ajjpj.abase.function;

import java.io.Serializable;


/**
 * Represents a function that takes four arguments and produces a result. This function does not throw a checked exception.
 *
 * @param <P1> 1st parameter type
 * @param <P2> 2nd parameter type
 * @param <P3> 3rd parameter type
 * @param <P4> 4th parameter type
 * @param <R> return type
 *
 * @author arno
 */
public interface AFunction4NoThrow<P1, P2, P3, P4, R> extends AFunction4<P1, P2, P3, P4, R, RuntimeException> {
    R apply (P1 param1, P2 param2, P3 param3, P4 param4);
}
