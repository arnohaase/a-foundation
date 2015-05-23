package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AStatement2NoThrow;

import java.util.concurrent.*;


/**
 * @author arno
 */
public class AThreadPool2 {

    private final ExecutorService executor;
    private final ScheduledThreadPoolExecutor timer;

    public AThreadPool2 (ExecutorService executor) {
        this.executor = executor;
        timer = new ScheduledThreadPoolExecutor (1, AThreadFactory.createWithRunningPoolNumber ("AThreadPool2_timeoutChecker", true));
        timer.setRemoveOnCancelPolicy (true);
    }

    public <V> AFuture2<V> submit(final Callable<V> callable, long timeout, TimeUnit timeUnit) {
        if (timer.isShutdown ()) throw new IllegalStateException (); // TODO comment unify
        final AFuture2<V> result = new AFuture2<> (this, callable);
        final Future<Void> timerFuture = timer.schedule (new Callable<Void> () {
            @Override public Void call () throws Exception {
                result.set (null, new TimeoutException ());
                return null;
            }
        }, timeout, timeUnit);
        result.onFinished (new AStatement2NoThrow<V, Throwable> () {
            @Override public void apply (V param1, Throwable param2) {
                timerFuture.cancel (false);
            }
        });
        return result;
    }

    ExecutorService getExecutorService () {
        return executor;
    }
    public void shutdown () {
        timer.shutdown ();
    }
}
