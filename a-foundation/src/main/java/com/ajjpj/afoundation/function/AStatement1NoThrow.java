package com.ajjpj.afoundation.function;


/**
 * @author arno
 */
public interface AStatement1NoThrow<T> extends AStatement1<T, RuntimeException> {
    @Override void apply(T param);
}
