package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AStatement2NoThrow;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author arno
 */
public class AFuture2<T> {
    private final AtomicReference<ATuple2<T, Throwable>> result = new AtomicReference<> (null);
    private final CountDownLatch latch = new CountDownLatch (1);
    private final AtomicReference<AList<AStatement2NoThrow<T, Throwable>>> listeners = new AtomicReference<> (AList.<AStatement2NoThrow<T, Throwable>>nil ());

    private final AThreadPool2 pool;

    private final Future<?> innerFuture;

    public AFuture2 (AThreadPool2 pool, final Callable<T> callable) {
        this.pool = pool;
        if (callable != null) {
            innerFuture = pool.getExecutorService().submit (new Runnable () {
                @Override public void run () {
                    try {
                        set (callable.call (), null);
                    }
                    catch (Throwable th) {
                        set (null, th);
                    }
                }
            }, null);
        }
        else {
            innerFuture = null;
        }
    }

    public T get() throws InterruptedException, ExecutionException {
        latch.await ();
        final ATuple2<T,Throwable> raw = result.get ();
        if (raw._2 != null) {
            throw new ExecutionException (raw._2);
        }
        return raw._1;

    }

    public void cancel (boolean interruptIfRunning) {
        if (set (null, new CancellationException ())) {
            if (innerFuture!=null) {
                innerFuture.cancel (interruptIfRunning);
            } else{
                // TODO mapsync?
            }
        }
    }

    public boolean isFinished() {
        return result.get () != null;
    }

    boolean set (T value, Throwable th) {
        final boolean justFinished = result.compareAndSet (null, new ATuple2<> (value, th));
        if (justFinished) {
            latch.countDown ();
            fireListeners (value, th);
        }
        return justFinished;
    }

    private void fireListeners(T value, Throwable th) {
        final AList<AStatement2NoThrow<T,Throwable>> all = listeners.getAndSet (AList.<AStatement2NoThrow<T,Throwable>>nil ());

        for (AStatement2NoThrow<T,Throwable> l: all) {
            l.apply (value, th);
        }
    }

    public void onFinished(AStatement2NoThrow<T, Throwable> listener){
        AList<AStatement2NoThrow<T, Throwable>> before, after;
        do {
            before = listeners.get ();
            after = before.cons (listener);
        }
        while(!listeners.compareAndSet (before, after));

        if (isFinished ()) {
            final ATuple2<T,Throwable> r = result.get ();
            fireListeners (r._1, r._2);
        }
    }

    public <U, E extends Exception> AFuture2<U> mapSync (final AFunction1<T, U, E> f) {
        final AFuture2<U> result = new AFuture2<> (null, null);
        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 != null) {
                    result.set (null, param2);
                }
                else {
                    try {
                        result.set (f.apply (param1), null);
                    }
                    catch (Throwable th) {
                        result.set (null, th);
                    }
                }
            }
        });
        return result;
    }

    public <U, E extends Exception> AFuture2<U> mapAsync (final AFunction1<T, U, E> f, final long timeout, final TimeUnit timeUnit) {
        final AFuture2<U> result = new AFuture2<> (null, null);
        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 != null) {
                    result.set (null, param2);
                }
                else {
                    pool.submit (new Callable<U> () {
                        @Override public U call () throws Exception {
                            return f.apply (get ());
                        }
                    }, timeout, timeUnit)
                            .onFinished (new AStatement2NoThrow<U, Throwable> () {
                                @Override public void apply (U param1, Throwable param2) {
                                    result.set (param1, param2);
                                }
                            });
                }
            }
        });
        return result;
    }
}
