package com.ajjpj.afoundation.concurrent.pool;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.collection.tuples.ATuple3;
import com.ajjpj.afoundation.conc2.AFuture;
import com.ajjpj.afoundation.conc2.APromisingExecutor;
import com.ajjpj.afoundation.conc2.ATry;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AStatement1NoThrow;

import java.util.concurrent.Future;


/**
 * @author arno
 */
class WrappingAFuture<T> implements AFuture<T> {
    private final Future<T> inner;

    public WrappingAFuture (Future<T> inner) {
        this.inner = inner;
    }

    @Override public boolean isDone () {
        return inner.isDone ();
    }

    @Override public void onFinished (AStatement1NoThrow<ATry<T>> listener) {
        throw new UnsupportedOperationException ();
    }
    @Override public void onSuccess (AStatement1NoThrow<T> listener) {
        throw new UnsupportedOperationException ();
    }
    @Override public void onFailure (AStatement1NoThrow<Throwable> listener) {
        throw new UnsupportedOperationException ();
    }

    @Override public ATry<T> get () {
        return ATry.fromEval (inner::get);
    }
    @Override public AOption<ATry<T>> value () {
        return null;
    }
    @Override public APromisingExecutor getImplicitThreadPool () {
        return null;
    }
    @Override public AFuture<T> withDefaultValue (T defaultValue) {
        return null;
    }
    @Override public <U, E extends Exception> AFuture<U> mapAsync (APromisingExecutor threadPool, AFunction1<T, U, E> f) {
        return null;
    }
    @Override public <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f) {
        return null;
    }
    @Override public <U> AFuture<ATuple2<T, U>> zip (AFuture<U> other) {
        return null;
    }
}
