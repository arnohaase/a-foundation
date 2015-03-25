package com.ajjpj.afoundation.function;

import java.io.Serializable;


public interface APredicate3<T1, T2, T3, E extends Exception> extends Serializable {
    boolean apply (T1 o1, T2 o2, T3 o3) throws E;
}
