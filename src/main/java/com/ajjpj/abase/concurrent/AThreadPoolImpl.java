package com.ajjpj.abase.concurrent;

import com.ajjpj.abase.collection.ACollectionHelper;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.AFunction1NoThrow;
import com.ajjpj.abase.function.AStatement2NoThrow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * @author arno
 */
class AThreadPoolImpl extends ThreadPoolExecutor implements AThreadPool {
    private final boolean shouldInterruptOnTimeout;

    final ScheduledThreadPoolExecutor timeoutChecker;

    AThreadPoolImpl (int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler, boolean shouldInterruptOnTimeout) {
        super (corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.shouldInterruptOnTimeout = shouldInterruptOnTimeout;

        timeoutChecker = new ScheduledThreadPoolExecutor (1, AThreadFactory.createWithRunningPoolNumber ("timeoutChecker", true));
        timeoutChecker.setRemoveOnCancelPolicy (true);
    }

    @Override public void shutdown () {
        super.shutdown ();
        timeoutChecker.shutdown ();
    }

    @Override public List<Runnable> shutdownNow () {
        timeoutChecker.shutdownNow ();
        return super.shutdownNow ();
    }

    @Override protected <T> RunnableFuture<T> newTaskFor (Runnable runnable, T value) {
        return new AFutureImpl<> (this, runnable, value);
    }
    @Override protected <T> RunnableFuture<T> newTaskFor (Callable<T> callable) {
        return new AFutureImpl<> (this, callable);
    }

    @Override public <T> AFuture<T> submit (Callable<T> task, long timeout, TimeUnit timeoutUnit) {
        return register ((AFutureImpl<T>) submit (task), timeout, timeoutUnit);
    }

    @Override public <T> AFuture<T> submit (Runnable task, T result, long timeout, TimeUnit timeoutUnit) {
        return register ((AFutureImpl<T>) submit (task, result), timeout, timeoutUnit);
    }

    @Override public <T, R, E extends Exception> List<AFuture<R>> submitAll (List<T> params, AFunction1<T, Callable<R>, E> taskFunction, long timeout, TimeUnit timeoutUnit) throws E {
        // first transform all tasks to callables to submit either all or none, even if an exception occurs during transformation
        final List<Callable<R>> callables = new ArrayList<> ();
        for (T param: params) {
            callables.add (taskFunction.apply (param));
        }

        final List<AFuture<R>> result = new ArrayList<> ();
        for (Callable<R> c: callables) {
            result.add (submit (c, timeout, timeoutUnit));
        }
        return result;
    }

    @Override public <T, R, E extends Exception> List<AFuture<R>> submitAllWithDefaultValue (List<T> params, AFunction1<T, Callable<R>, E> taskFunction, long timeout, TimeUnit timeoutUnit, final R defaultValue) throws E {
        final List<AFuture<R>> raw = submitAll (params, taskFunction, timeout, timeoutUnit);
        return ACollectionHelper.map (raw, new AFunction1NoThrow<AFuture<R>, AFuture<R>> () {
            @Override public AFuture<R> apply (AFuture<R> param) {
                return param.withDefaultValue (defaultValue);
            }
        });
    }

    private <T> AFutureImpl<T> register (final AFutureImpl<T> f, long timeout, TimeUnit timeoutUnit) {
        final Runnable timeoutCanceler = new Runnable () {
            @Override public void run () {
                f.setTimedOut ();
                f.cancel (shouldInterruptOnTimeout);
            }
        };

        final ScheduledFuture<?> timeoutFuture = timeoutChecker.schedule (timeoutCanceler, timeout, timeoutUnit);
        f.onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                timeoutFuture.cancel (false);
            }
        });

        return f;
    }
}
