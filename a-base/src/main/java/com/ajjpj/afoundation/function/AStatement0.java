package com.ajjpj.afoundation.function;

import java.io.Serializable;


/**
 * @author arno
 */
public interface AStatement0<E extends Exception> extends Serializable {
    void apply() throws E;
}
