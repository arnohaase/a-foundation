package com.ajjpj.abase.function;

/**
 * @author arno
 */
public interface APredicate<T,E extends Exception> {
    boolean apply(T o) throws E;
}
