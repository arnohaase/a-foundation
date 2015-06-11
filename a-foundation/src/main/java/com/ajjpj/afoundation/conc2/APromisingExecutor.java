package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.function.AFunction0;


/**
 * @author arno
 */
public interface APromisingExecutor extends AExecutor {
    <T> APromise<T> submit (AFunction0<T, ? extends Exception> function); //TODO timeout?
}
