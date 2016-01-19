package com.ajjpj.afoundation.function;

import java.io.Serializable;


/**
 * @author arno
 */
public interface AStatement0<E extends Throwable> extends Serializable {
    void apply() throws E;
}
