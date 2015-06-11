package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.function.AStatement1NoThrow;


/**
 * @author arno
 */
class AKeptPromise<T> extends APromiseImpl<T> {
    private final ATry<T> value;

    AKeptPromise (APromisingExecutor implicitThreadPool, ATry<T> value) {
        super (implicitThreadPool);
        this.value = value;
    }

    @Override public ATry<T> get () {
        return value;
    }
    @Override public AOption<ATry<T>> value () {
        return AOption.some (value);
    }

    @Override public boolean isDone () {
        return true;
    }

    @Override public boolean tryComplete (ATry<T> result) {
        return false;
    }

    @Override public void onFinished (AStatement1NoThrow<ATry<T>> listener) {
        listener.apply (value);
    }
}
