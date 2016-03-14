package com.ajjpj.afoundation.function;


/**
 * Represents a partial function that returns nothing, having {@code void} as its return value.
 */
public interface APartialStatement<P,T extends Throwable> extends AStatement1<P,T> {
    boolean isDefinedAt (P param);
}
