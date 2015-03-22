package com.ajjpj.abase.concurrent;

import com.ajjpj.abase.function.AFunction1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * @author arno
 */
class ASyncThreadPool implements AThreadPool { //TODO test
    private volatile boolean isShutdown = false;

    @Override public void shutdown () {
        isShutdown = true;
    }

    @Override public List<Runnable> shutdownNow () {
        shutdown ();
        return Collections.emptyList ();
    }
    @Override public boolean isShutdown () {
        return isShutdown;
    }
    @Override public boolean isTerminated () {
        return isShutdown;
    }

    @Override public boolean awaitTermination (long timeout, TimeUnit unit) throws InterruptedException {
        return isShutdown;
    }

    @Override public <T> AFuture<T> submit (Callable<T> task, long timeout, TimeUnit timeoutUnit) {
        final AFutureImpl<T> result = AFutureImpl.unscheduled (this);

        try {
            result.set (task.call ());
        }
        catch (Throwable th) {
            result.setException (th);
        }
        return result;
    }

    @Override public <T> AFuture<T> submit (Runnable task, T resultValue, long timeout, TimeUnit timeoutUnit) {
        final AFutureImpl<T> result = AFutureImpl.unscheduled (this);

        try {
            task.run ();
            result.set (resultValue);
        }
        catch (Throwable th) {
            result.setException (th);
        }
        return result;
    }

    @Override public <T, R, E extends Exception> List<AFuture<R>> submitAll (List<T> params, final AFunction1<T, R, E> taskFunction, long timeout, TimeUnit timeoutUnit) throws E {
        final List<AFuture<R>> result = new ArrayList<> ();

        for (final T t: params) {
            result.add (submit (new Callable<R> () {
                @Override public R call () throws Exception {
                    return taskFunction.apply (t);
                }
            }, timeout, timeoutUnit));
        }

        return result;
    }

    @Override public <T, R, E extends Exception> List<AFuture<R>> submitAllWithDefaultValue (List<T> params, final AFunction1<T, R, E> taskFunction, long timeout, TimeUnit timeoutUnit, final R defaultValue) throws E {
        final List<AFuture<R>> result = new ArrayList<> ();

        for (final T t: params) {
            result.add (submit (new Callable<R> () {
                @Override public R call () throws Exception {
                    try {
                        return taskFunction.apply (t);
                    }
                    catch (Throwable e) {
                        return defaultValue;
                    }
                }
            }, timeout, timeoutUnit));
        }

        return result;
    }
}

