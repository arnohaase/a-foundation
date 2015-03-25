package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AFunction1NoThrow;
import com.ajjpj.afoundation.function.AStatement2NoThrow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author arno
 */
public class ACircuitBreaker implements ATaskScheduler {
    private final ATaskScheduler threadPool;

    private final AtomicLong retryAt = new AtomicLong ();
    private final AtomicInteger numFailures = new AtomicInteger ();

    private final int maxNumFailures;
    private final long recoveryMillis;

    public ACircuitBreaker (ATaskScheduler threadPool) {
        this (threadPool, 3, 5, TimeUnit.MINUTES);
    }

    public ACircuitBreaker (ATaskScheduler threadPool, int maxNumFailures, long recoveryDelay, TimeUnit recoveryTimeUnit) {
        this.threadPool = threadPool;
        this.maxNumFailures = maxNumFailures;
        this.recoveryMillis = recoveryTimeUnit.toMillis (recoveryDelay);
    }

    @Override public <T> AFuture<T> submit (Callable<T> task, long timeout, TimeUnit timeoutUnit) {
        if (shouldSubmit ()) {
            return withCircuitBreaker (threadPool.submit (task, timeout, timeoutUnit));
        }
        return failure();
    }

    @Override public <T> AFuture<T> submit (Runnable task, T result, long timeout, TimeUnit timeoutUnit) {
        if (shouldSubmit ()) {
            return withCircuitBreaker (threadPool.submit (task, result, timeout, timeoutUnit));
        }
        return failure();
    }

    @Override public <T, R, E extends Exception> List<AFuture<R>> submitAll (List<T> params, AFunction1<T, R, E> taskFunction, long timeout, TimeUnit timeoutUnit) {
        if (shouldSubmit ()) {
            final List<AFuture<R>> raw = threadPool.submitAll (params, taskFunction, timeout, timeoutUnit);
            return ACollectionHelper.map (raw, new AFunction1NoThrow<AFuture<R>, AFuture<R>> () {
                @Override public AFuture<R> apply (AFuture<R> param) {
                    return withCircuitBreaker (param);
                }
            });
        }
        return failureList (params.size ());
    }

    @Override public <T, R, E extends Exception> List<AFuture<R>> submitAllWithDefaultValue (List<T> params, AFunction1<T, R, E> taskFunction, long timeout, TimeUnit timeoutUnit, R defaultValue) {
        if (shouldSubmit ()) {
            final List<AFuture<R>> raw = threadPool.submitAllWithDefaultValue (params, taskFunction, timeout, timeoutUnit, defaultValue);
            return ACollectionHelper.map (raw, new AFunction1NoThrow<AFuture<R>, AFuture<R>> () {
                @Override public AFuture<R> apply (AFuture<R> param) {
                    return withCircuitBreaker (param);
                }
            });
        }
        return failureList (params.size ());
    }

    private <R> List<AFuture<R>> failureList (int size) {
        final List<AFuture<R>> result = new ArrayList<> ();
        for (int i=0; i<size; i++) {
            result.add (this.<R>failure ());
        }
        return result;
    }

    private <R> AFuture<R> failure () {
        final AFutureImpl<R> result = AFutureImpl.unscheduled (threadPool);
        result.setException (new TimeoutException ("recovering from previous failures (CircuitBreaker)"));
        return result;
    }

    private boolean shouldSubmit () {
        if (numFailures.get () < maxNumFailures) {
            return true;
        }

        //store 'System.currentTimeMillis()' in a variable for speedup: we can reuse the value below
        final long now = System.currentTimeMillis ();

        // we need to remember this initial value of 'retryAt' for both the initial check and the later compareAndSet(), otherwise we would have a race condition
        final long retryAtValue = retryAt.get ();
        if (retryAtValue > now) {
            return false;
        }

        return retryAt.compareAndSet (retryAtValue, now + recoveryMillis);
    }

    private <T> AFuture<T> withCircuitBreaker (AFuture<T> f) {
        f.onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 == null) {
                    numFailures.incrementAndGet ();
                    retryAt.set (System.currentTimeMillis () + recoveryMillis);
                }
                else {
                    numFailures.set (0);
                }
            }
        });
        return f;
    }
}
