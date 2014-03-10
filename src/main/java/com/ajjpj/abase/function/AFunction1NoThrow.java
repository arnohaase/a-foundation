package com.ajjpj.abase.function;


/**
 * @author arno
 */
public interface AFunction1NoThrow<R, T> extends AFunction1<R, T, RuntimeException> {
    @Override R apply(T param);
}
