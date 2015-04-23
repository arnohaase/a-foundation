package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AStatement2NoThrow;

import java.util.concurrent.*;


/**
 * @author arno
 */
public class AThreadPool2 {

    private final ExecutorService executor;
    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool (1);

    public AThreadPool2 (ExecutorService executor) {
        this.executor = executor;
    }

    public <V> AFuture2<V> submit(final Callable<V> callable, long timeout, TimeUnit timeUnit) {
        final AFuture2<V> result = new AFuture2<> (executor, callable);
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

}
