package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.function.AStatement1NoThrow;


/**
 * @author arno
 */
public interface AFuture<T> {
    boolean isDone (); //TODO terminology - 'done', 'finished', 'completed', ... --> ?!?!
    ATry<T> get ();

    void onFinished (AStatement1NoThrow<ATry<T>> listener);
    void onSuccess (AStatement1NoThrow<T> listener);
    void onFailure (AStatement1NoThrow<Throwable> listener);
}
