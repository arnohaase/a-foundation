package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AFunction1;
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
    private final APromisingExecutor implicitThreadPool;

    //TODO merge all state behind a single AtomicReference to minimize volatile accesses in 'tryComplete'?
    private final Set<Thread> waiters = Collections.newSetFromMap (new ConcurrentHashMap<Thread, Boolean>());
    private final AtomicReference<AList<AStatement1NoThrow<ATry<T>>>> listeners = new AtomicReference<> (AList.<AStatement1NoThrow<ATry<T>>>nil());

    private final AtomicReference<ATry<T>> value = new AtomicReference<> ();

    public APromiseImpl (APromisingExecutor implicitThreadPool) {
        this.implicitThreadPool = implicitThreadPool;
    }

    private <X> APromiseImpl<X> newPromise() {
        return new APromiseImpl<> (implicitThreadPool);
    }

    @Override public APromisingExecutor getImplicitThreadPool () {
        return implicitThreadPool;
    }

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

    @Override public AOption<ATry<T>> value () {
        return AOption.fromNullable (value.get ());
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

    //--------------------------------------- AFuture comprehensions -----------------------------------------------

    @Override public AFuture<T> withDefaultValue (final T defaultValue) {
        final APromise<T> result = newPromise ();

        onFinished (new AStatement1NoThrow<ATry<T>> () {
            @Override public void apply (ATry<T> param) {
                result.completeSuccessfully (param.getOrElse (defaultValue));
            }
        });

        return result.asFuture ();
    }

    @Override public <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f) {
        return mapAsync (getImplicitThreadPool (), f);
    }
    @Override public <U, E extends Exception> AFuture<U> mapAsync (APromisingExecutor threadPool, AFunction1<T, U, E> f) {
        return null;
    }

    @Override public <U> AFuture<ATuple2<T, U>> zip (final AFuture<U> other) {
        final APromise<ATuple2<T,U>> result = newPromise ();

        onFinished (new AStatement1NoThrow<ATry<T>> () {
            @Override public void apply (final ATry<T> result1) {
                if (result1.isFailure ()) result.completeExceptionally (result1.getFailure ().get ());
                else other.onFinished (new AStatement1NoThrow<ATry<U>> () {
                    @Override public void apply (ATry<U> result2) {
                        if (result2.isSuccess ()) result.completeSuccessfully (new ATuple2<> (result1.get (), result2.get ()));
                        else result.completeExceptionally (result2.getFailure ().get ());
                    }
                });
            }
        });

        return result.asFuture ();
    }
}
