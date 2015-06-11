package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.function.AFunction0;
import com.ajjpj.afoundation.util.AExceptionFilter;
import com.ajjpj.afoundation.util.AUnchecker;


/**
 * TODO JUnit test
 * TODO documentation
 * TODO flatten, map, ...
 *
 * @author arno
 */
public abstract class ATry<T> { //TODO move to other package
    public static <T> ATry<T> fromEval (AFunction0<T, ? extends Exception> code) {
        try {
            return new ASuccess<> (code.apply ());
        }
        catch (Throwable th) {
            return new AFailure<> (AExceptionFilter.forLocalHandling (th));
        }
    }

    public static <T> ATry<T> fromValue (T value) {
        return new ASuccess<> (value);
    }

    public static <T> ATry<T> fromException (Throwable th) {
        return new AFailure<> (th);
    }

    abstract boolean isSuccess();
    abstract boolean isFailure();

    abstract T get();

    abstract AOption<Throwable> getFailure();

    public AOption<T> toOption() {
        if (isSuccess ()) return AOption.some (get ());
        return AOption.none ();
    }



    private static class ASuccess<T> extends ATry<T> {
        private final T value;

        public ASuccess (T value) {
            this.value = value;
        }

        @Override boolean isSuccess () {
            return true;
        }
        @Override boolean isFailure () {
            return false;
        }

        @Override T get () {
            return value;
        }
        @Override AOption<Throwable> getFailure () {
            return AOption.none ();
        }
    }

    private static class AFailure<T> extends ATry<T> {
        private final Throwable throwable;

        public AFailure (Throwable throwable) {
            this.throwable = throwable;
        }

        @Override boolean isSuccess () {
            return false;
        }
        @Override boolean isFailure () {
            return true;
        }

        @Override T get () {
            AUnchecker.throwUnchecked (throwable);
            return null; // for the compiler
        }

        @Override AOption<Throwable> getFailure () {
            return AOption.some (throwable);
        }
    }
}

