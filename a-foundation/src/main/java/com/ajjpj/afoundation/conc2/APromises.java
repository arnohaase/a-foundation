package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.function.AFunction0;
import com.ajjpj.afoundation.function.AStatement1NoThrow;


/**
 * @author arno
 */
public class APromises {
    public static <T> APromise<T> calcAsync (AExecutor executor, final boolean interruptOnCancel, final AFunction0<T, ? extends Exception> code) {
        final APromiseImpl<T> result = new APromiseImpl<> ();

        final ASubmitted submitted = executor.submit (new Runnable () {
            @Override public void run () {
                result.complete (ATry.fromEval (code));
            }
        });

        result.onFinished (new AStatement1NoThrow<ATry<T>> () {
            @Override public void apply (ATry<T> param) {
                submitted.cancel (interruptOnCancel);
            }
        });

        return result;
    }
}
