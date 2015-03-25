package com.ajjpj.afoundation.function;

import java.io.Serializable;


public interface APredicate2<T1, T2, E extends Exception> extends Serializable {
    boolean apply (T1 o1, T2 o2) throws E;
}
