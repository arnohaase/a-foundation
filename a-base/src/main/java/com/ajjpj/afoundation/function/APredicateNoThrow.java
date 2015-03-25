package com.ajjpj.afoundation.function;

/**
 * @author arno
 */
public interface APredicateNoThrow<T> extends APredicate<T, RuntimeException> {
    boolean apply(T o);
}
