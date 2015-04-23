package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AStatement2;
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

    private final Future<?> innerFuture;

    public AFuture2 (ExecutorService es, final Callable<T> callable) {
        innerFuture = es.submit (new Runnable () {
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
            innerFuture.cancel (interruptIfRunning);
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
        do {
            AStatement2NoThrow<T, Throwable> l;
            AList<AStatement2NoThrow<T, Throwable>> before, after;
            do {
                before = listeners.get ();
                if (before.isEmpty ()) {
                    return;
                }
                after = before.tail ();
                l = before.head ();
            }
            while (!listeners.compareAndSet (before, after));
            l.apply (value, th);
        }
        while (true);
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
}
