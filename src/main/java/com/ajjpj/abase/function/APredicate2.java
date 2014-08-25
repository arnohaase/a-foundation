package com.ajjpj.abase.function;

public interface APredicate2<T1, T2, E extends Exception> {
    boolean apply (T1 o1, T2 o2) throws E;
}
