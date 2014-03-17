package com.ajjpj.abase.function;

/**
 * @author arno
 */
public interface AFunction0<R, E extends Exception> {
    R apply() throws E;
}

interface A <X extends A<X>> {

}

interface A0 extends A<A0> {

}

class B implements A0 {

}