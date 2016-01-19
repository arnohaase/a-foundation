package com.ajjpj.afoundation.util;


import com.ajjpj.afoundation.function.AFunction0;
import com.ajjpj.afoundation.function.AStatement0;


/**
 * This class throws an arbitrary exception without requiring it to be declared in a throws clause
 *
 * @author arno
 */
public class AUnchecker {
    public static void throwUnchecked(Throwable th) {
        AUnchecker.<RuntimeException> doIt(th);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void doIt(Throwable th) throws T {
        throw (T) th;
    }

    @SuppressWarnings("unused")
    public static void executeUnchecked(AStatement0<? extends Throwable> callback) {
        try {
            callback.apply();
        }
        catch (Throwable exc) {
            throwUnchecked (exc);
        }
    }

    public static <R> R executeUnchecked(AFunction0<R, ? extends Throwable> callback) {
        try {
            return callback.apply();
        }
        catch (Throwable exc) {
            throwUnchecked (exc);
            return null; //for the compiler
        }
    }
}
