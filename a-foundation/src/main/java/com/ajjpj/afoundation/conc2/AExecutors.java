package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.function.AFunction0;


/**
 * @author arno
 */
public class AExecutors {
    public static <T> APromise<T> calcAsync (APromisingExecutor executor, final AFunction0<T, ? extends Exception> function) {
        final APromiseImpl<T> result = new APromiseImpl<> (executor);

        executor.submit (new Runnable () {
            @Override public void run () {
                // skip execution if the promise was completed in some other way --> 'cancellation'
                if (result.isDone ()) return; //TODO optimize this!
                result.tryComplete (ATry.fromEval (function));
            }
        });

        return result;
    }

    private static final APromisingExecutor NO_THREAD_POOL = null; //TODO

    public static <T> APromise<T> openPromise () {
        return openPromise (NO_THREAD_POOL);
    }

    public static <T> APromise<T> openPromise (APromisingExecutor implicitThreadPool) {
        return new APromiseImpl<> (implicitThreadPool);
    }

    public static <T> APromise<T> keptPromise (ATry<T> value) {
        return keptPromise (NO_THREAD_POOL, value);
    }

    public static <T> APromise<T> keptPromise (APromisingExecutor implicitThreadPool, ATry<T> value) {
        return new AKeptPromise<> (implicitThreadPool, value);
    }

    public static <T> APromise<T> keptPromiseFromValue (T value) {
        return keptPromiseFromValue (NO_THREAD_POOL, value);
    }

    public static <T> APromise<T> keptPromiseFromValue (APromisingExecutor implicitThreadPool, T value) {
        return keptPromise (implicitThreadPool, ATry.fromValue (value));
    }

    public static <T> APromise<T> keptPromiseFromException (Throwable th) {
        return keptPromiseFromException (NO_THREAD_POOL, th);
    }
    public static <T> APromise<T> keptPromiseFromException (APromisingExecutor implicitThreadPool, Throwable th) {
        return keptPromise (implicitThreadPool, ATry.<T>fromException (th));
    }
}
