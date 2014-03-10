package com.ajjpj.abase.function;

/**
 * @author arno
 */
public interface AStatement1<P, E extends Exception> {
    void apply(P param) throws E;
}
