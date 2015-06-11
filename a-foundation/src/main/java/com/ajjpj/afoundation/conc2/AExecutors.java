package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.function.AFunction0;


/**
 * @author arno
 */
public class AExecutors {
    public static <T> APromise<T> calcAsync (AExecutor executor, final AFunction0<T, ? extends Exception> function) {
        final APromiseImpl<T> result = new APromiseImpl<> ();

        executor.submit (new Runnable () {
            @Override public void run () {
                // skip execution if the promise was completed in some other way --> 'cancellation'
                if (result.isDone ()) return;
                result.tryComplete (ATry.fromEval (function));
            }
        });

        return result;
    }
}
