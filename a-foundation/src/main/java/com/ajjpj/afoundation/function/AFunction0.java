package com.ajjpj.afoundation.function;

import java.io.Serializable;


/**
 * @author arno
 */
public interface AFunction0<R, E extends Throwable> extends Serializable {
    R apply() throws E;
}
