package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.APartialFunction;
import com.ajjpj.afoundation.function.AStatement1;
import com.ajjpj.afoundation.util.AUnchecker;


public abstract class ATry<T> {
    public static <T> ATry<T> success (T o) {
        return new ASuccess<> (o);
    }

    public static <T> ATry<T> failure (Throwable th) {
        return new AFailure<> (th);
    }

    public abstract <X extends Throwable> void foreach (AStatement1<T, X> f) throws X;

    public abstract ATry<Throwable> inverse ();

    public abstract <S, E extends Throwable> ATry<S> map (AFunction1<T, S, E> f) throws E;

    public abstract boolean isSuccess ();

    public boolean isFailure () {
        return !isSuccess ();
    }

    public abstract T getValue ();

    public abstract <E extends Throwable> ATry<T> recover (APartialFunction<Throwable, T, E> f) throws E;

    //TODO more operations

    static class ASuccess<T> extends ATry<T> {
        final T value;

        public ASuccess (T value) {
            this.value = value;
        }

        @Override
        public <X extends Throwable> void foreach (AStatement1<T, X> f) throws X {
            f.apply (value);
        }

        @Override
        public ATry<Throwable> inverse () {
            throw new IllegalStateException ("ASuccess.inverse");
        }

        @Override
        public <S, E extends Throwable> ATry<S> map (AFunction1<T, S, E> f) throws E {
            return new ASuccess<> (f.apply (value));
        }

        @Override
        public boolean isSuccess () {
            return true;
        }

        @Override
        public T getValue () {
            return value;
        }

        @Override
        public <E extends Throwable> ATry<T> recover (APartialFunction<Throwable, T, E> f) throws E {
            return this;
        }
    }

    static class AFailure<T> extends ATry<T> {
        final Throwable th;

        public AFailure (Throwable th) {
            this.th = th;
        }

        @Override
        public <X extends Throwable> void foreach (AStatement1<T, X> f) throws X {
        }

        @Override
        public ATry<Throwable> inverse () {
            return ATry.success (th);
        }

        @Override
        public <S, E extends Throwable> ATry<S> map (AFunction1<T, S, E> f) throws E {
            //noinspection unchecked
            return (ATry<S>) this;
        }

        @Override
        public boolean isSuccess () {
            return false;
        }

        @Override
        public T getValue () {
            AUnchecker.throwUnchecked (th);
            return null; // for the compiler
        }

        @Override
        public <E extends Throwable> ATry<T> recover (APartialFunction<Throwable, T, E> f) throws E {
            if (f.isDefinedAt (th)) return ATry.success (f.apply (th));
            return this;
        }
    }
}
