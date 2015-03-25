package com.ajjpj.afoundation.function;

import java.io.Serializable;


/**
 * @author arno
 */
public interface AFunction0<R, E extends Exception> extends Serializable {
    R apply() throws E;
}
