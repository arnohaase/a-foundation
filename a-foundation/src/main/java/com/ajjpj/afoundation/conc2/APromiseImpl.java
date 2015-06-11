package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.function.APredicateNoThrow;
import com.ajjpj.afoundation.function.AStatement1NoThrow;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;


/**
 * @author arno
 */
class APromiseImpl<T> implements APromise<T>, AFuture<T> {
    //TODO merge all state behind a single AtomicReference to minimize volatile accesses in 'tryComplete'?
    private final Set<Thread> waiters = Collections.newSetFromMap (new ConcurrentHashMap<Thread, Boolean>());
    private final AtomicReference<AList<AStatement1NoThrow<ATry<T>>>> listeners = new AtomicReference<> (AList.<AStatement1NoThrow<ATry<T>>>nil());

    private final AtomicReference<ATry<T>> value = new AtomicReference<> ();

    @Override public AFuture<T> asFuture () {
        return this;
    }

    @Override public boolean tryCompleteSuccessfully (T value) {
        return tryComplete (ATry.fromValue (value));
    }

    @Override public void completeSuccessfully (T value) {
        complete (ATry.fromValue (value));
    }

    @Override public boolean tryCompleteExceptionally (Throwable throwable) {
        return tryComplete (ATry.<T>fromException (throwable));
    }

    @Override public void completeExceptionally (Throwable throwable) {
        complete (ATry.<T>fromException (throwable));
    }

    @Override public boolean tryComplete (ATry<T> result) {
        if (value.compareAndSet (null, result)) { //TODO putObjectOrdered would suffice --> ?!
            for (Thread thread: waiters) {
                LockSupport.unpark (thread);
            }
            waiters.clear ();

            for (AStatement1NoThrow<ATry<T>> l: listeners.getAndSet (AList.<AStatement1NoThrow<ATry<T>>>nil())) {
                l.apply (result);
            }

            return true;
        }
        return false;
    }

    @Override public void complete (ATry<T> result) {
        if (! tryComplete (result)) throw new IllegalStateException ("already completed");
    }

    @Override public void completeWith (AFuture<T> f) {
        f.onFinished (new AStatement1NoThrow<ATry<T>> () {
            @Override public void apply (ATry<T> param) {
                tryComplete (param);
            }
        });
    }

    @Override public boolean isDone () {
        return value.get () != null;
    }

    @Override public ATry<T> get () {
        ATry<T> result;

        while ((result = value.get ()) == null) {
            waiters.add (Thread.currentThread ());
            LockSupport.park ();
            waiters.remove (Thread.currentThread ());
        }

        return result;
    }

    @Override public void onSuccess (final AStatement1NoThrow<T> listener) {
        onFinished (new AStatement1NoThrow<ATry<T>> () {
            @Override public void apply (ATry<T> param) {
                if (param.isSuccess ()) {
                    listener.apply (param.get ());
                }
            }
        });
    }

    @Override public void onFailure (final AStatement1NoThrow<Throwable> listener) {
        onFinished (new AStatement1NoThrow<ATry<T>> () {
            @Override public void apply (ATry<T> param) {
                if (param.isFailure ()) {
                    listener.apply (param.getFailure ().get ());
                }
            }
        });
    }

    @Override public void onFinished (final AStatement1NoThrow<ATry<T>> listener) {
        AList<AStatement1NoThrow<ATry<T>>> before;

        do {
            before = listeners.get ();
        }
        while (! listeners.compareAndSet (before, before.cons (listener)));

        // The following code deals with registration of listeners after the promise was completed. The somewhat roundabout approach of first registering
        //  the listener and then potentially immediately removing it handles races when registration of a listener and completion of the promise happen
        //  concurrently.
        final ATry<T> result = value.get ();
        if (result != null) {
            do {
                before = listeners.get ();
            }
            while (! listeners.compareAndSet (before, before.filter (
                    new APredicateNoThrow<AStatement1NoThrow<ATry<T>>> () {
                        @Override public boolean apply (AStatement1NoThrow<ATry<T>> o) {
                            return o != listener;
                        }
                    })
            ));

            listener.apply (result);
        }
    }
}
