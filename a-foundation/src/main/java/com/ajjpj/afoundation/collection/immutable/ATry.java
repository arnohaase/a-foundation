package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.concurrent.SuccessfulCompletionException;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.APartialFunction;
import com.ajjpj.afoundation.function.AStatement1;
import com.ajjpj.afoundation.util.AUnchecker;


/**
 * An ATry represents either a value ('success') or a Throwable that occurred trying to provide the value ('failure').
 */
public abstract class ATry<T> {
    /**
     * This method creates a successful ATry.
     */
    public static <T> ATry<T> success (T o) {
        return new ASuccess<> (o);
    }

    /**
     * This method creates a failed ATry.
     */
    public static <T> ATry<T> failure (Throwable th) {
        return new AFailure<> (th);
    }

    /**
     * This method executed a statement for the value (in case of success), or does nothing (in case of failure).
     */
    public abstract <X extends Throwable> void foreach (AStatement1<T, X> f) throws X;

    /**
     * @return This method returns a new ATry with inverted 'success' semantics: If the original ATry is a success,
     *  the newly created ATry is a failure with a {@link SuccessfulCompletionException} as its cause, and if the
     *  original ATry is a failure, the newly created ATry is a success with the causing Throwable as its value.
     */
    public abstract ATry<Throwable> inverse ();

    /**
     * This method creates a new ATry applying a given function to the value if this ATry is successful, or returns
     *  this ATry's failure unchanged.
     */
    public abstract <S, E extends Throwable> ATry<S> map (AFunction1<T, S, E> f) throws E;

    /**
     * @return true if and only if this ATry is a success.
     */
    public abstract boolean isSuccess ();

    /**
     * @return true if and only if this ATry is a failure.
     */
    public boolean isFailure () {
        return !isSuccess ();
    }

    /**
     * @return this ATry's value if it is a success, and the underlying Throwable if it is a failure
     */
    public abstract T getValue ();

    /**
     * If this is a failure and the given function {@link APartialFunction#isDefinedAt is defined at} the Throwable,
     *  a new successful ATry is created with the function's result. Otherwise this ATry is returned unmodified.
     */
    public abstract <E extends Throwable> ATry<T> recover (APartialFunction<Throwable, T, E> f) throws E;

    /**
     * If this is a failure and the given function {@link APartialFunction#isDefinedAt is defined at} the Throwable,
     *  the function's result is returned. Otherwise this ATry is returned unmodified.
     */
    public abstract <E extends Throwable> ATry<T> recoverWith (APartialFunction<Throwable, ATry<T>, E> f) throws E;

    //TODO more operations

    static class ASuccess<T> extends ATry<T> {
        final T value;

        private ASuccess (T value) {
            this.value = value;
        }

        @Override public <X extends Throwable> void foreach (AStatement1<T, X> f) throws X {
            f.apply (value);
        }

        @Override public ATry<Throwable> inverse () {
            return ATry.failure (new SuccessfulCompletionException (value));
        }

        @Override public <S, E extends Throwable> ATry<S> map (AFunction1<T, S, E> f) throws E {
            return new ASuccess<> (f.apply (value));
        }

        @Override public boolean isSuccess () {
            return true;
        }

        @Override public T getValue () {
            return value;
        }

        @Override public <E extends Throwable> ATry<T> recover (APartialFunction<Throwable, T, E> f) throws E {
            return this;
        }

        @Override public <E extends Throwable> ATry<T> recoverWith (APartialFunction<Throwable, ATry<T>, E> f) throws E {
            return this;
        }
    }

    static class AFailure<T> extends ATry<T> {
        final Throwable th;

        private AFailure (Throwable th) {
            this.th = th;
        }

        @Override public <X extends Throwable> void foreach (AStatement1<T, X> f) throws X {
        }

        @Override public ATry<Throwable> inverse () {
            return ATry.success (th);
        }

        @Override public <S, E extends Throwable> ATry<S> map (AFunction1<T, S, E> f) throws E {
            //noinspection unchecked
            return (ATry<S>) this;
        }

        @Override public boolean isSuccess () {
            return false;
        }

        @Override public T getValue () {
            AUnchecker.throwUnchecked (th);
            return null; // for the compiler
        }

        @Override public <E extends Throwable> ATry<T> recover (APartialFunction<Throwable, T, E> f) throws E {
            if (f.isDefinedAt (th)) return ATry.success (f.apply (th));
            return this;
        }

        @Override public <E extends Throwable> ATry<T> recoverWith (APartialFunction<Throwable, ATry<T>, E> f) throws E {
            if (f.isDefinedAt (th)) return f.apply (th);
            return this;
        }
    }
}
