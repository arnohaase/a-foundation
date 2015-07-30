package com.ajjpj.afoundation.function;

/**
 * @author Roman Krüger
 */
public interface APartialFunction1NoThrow<P, R> extends APartialFunction1<P, R, RuntimeException> {
    @Override R apply (P param);
}
