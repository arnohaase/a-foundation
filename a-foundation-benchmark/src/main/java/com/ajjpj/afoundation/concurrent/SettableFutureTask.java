package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class SettableFutureTask<T> extends FutureTask<T> {
    public SettableFutureTask (Callable<T> callable) {
        super (callable);
    }

    @Override public void set (T t) {
        super.set (t);
    }

    @Override public void setException (Throwable t) {
        super.setException (t);
    }
}