package com.ajjpj.abase.function;

import java.io.Serializable;


/**
 * Represents a function that takes two parameters and returns nothing.
 *
 * @author arno
 */
public interface AStatement2<P1, P2, E extends Exception> extends Serializable {
    void apply (P1 param1, P2 param2) throws E;
}
