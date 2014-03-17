package com.ajjpj.abase.function;

/**
 * @author arno
 */
public interface AFunction0<R, E extends Exception> {
    R apply() throws E;
}
