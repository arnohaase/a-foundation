package com.ajjpj.afoundation.function;

/**
 * @author Roman Krüger
 */
public interface APartialFunctionNoThrow<P, R> extends APartialFunction<P, R, RuntimeException> {
    @Override R apply (P param);
}
