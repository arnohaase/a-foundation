package com.ajjpj.abase.function;

public interface APredicate2NoThrow<T1, T2> extends APredicate2<T1, T2, RuntimeException> {
    boolean apply (T1 o1, T2 o2);
}
