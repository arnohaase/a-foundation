package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.function.AStatement1NoThrow;


/**
 * @author arno
 */
public interface AFuture<T> {
    boolean isDone (); //TODO terminology - 'done', 'finished', 'completed', ... --> ?!?!
    ATry<T> get ();

    void onFinished (AStatement1NoThrow<ATry<T>> listener);
}
