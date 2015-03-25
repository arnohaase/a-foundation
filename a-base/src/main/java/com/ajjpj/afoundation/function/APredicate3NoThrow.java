package com.ajjpj.afoundation.function;

public interface APredicate3NoThrow<T1, T2, T3> extends APredicate3<T1, T2, T3, RuntimeException> {
    boolean apply (T1 o1, T2 o2, T3 o3);
}
