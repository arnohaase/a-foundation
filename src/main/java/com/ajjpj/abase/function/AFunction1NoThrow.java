package com.ajjpj.abase.function;


/**
 * @author arno
 */
public interface AFunction1NoThrow<R, T> extends AFunction1<T, R, RuntimeException> {
    @Override R apply(T param);
}
