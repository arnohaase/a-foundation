package com.ajjpj.abase.function;

import java.io.Serializable;


/**
 * @author arno
 */
public interface APredicate<T,E extends Exception> extends Serializable {
    boolean apply(T o) throws E;
}
