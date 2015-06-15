package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.APredicateNoThrow;
import com.ajjpj.afoundation.function.AStatement1NoThrow;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;


/**
 * @author arno
 */
class APromiseImpl<T> implements APromise<T>, AFuture<T> {
    private final APromisingExecutor implicitThreadPool;

    private final AtomicReference<State<T>> state = new AtomicReference<> (State.<T>initial ());

//    //TODO merge all state behind a single AtomicReference to minimize volatile accesses in 'tryComplete'?
//    private final Set<Thread> waiters = Collections.newSetFromMap (new ConcurrentHashMap<Thread, Boolean>());
//    private final AtomicReference<AList<AStatement1NoThrow<ATry<T>>>> listeners = new AtomicReference<> (AList.<AStatement1NoThrow<ATry<T>>>nil());
//
//    private final AtomicReference<ATry<T>> value = new AtomicReference<> ();

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
        State<T> before, after;
        do {
            before = state.get ();
            if (before.value != null) {
                return false;
            }

            after = State.<T>initial ().withValue (result);
        }
        //TODO weakCompareAndSet?
        while (! state.compareAndSet (before, after));

        for (Thread waiter: before.waiters) {
            LockSupport.unpark (waiter); //TODO Unsafe?
        }
        for (AStatement1NoThrow<ATry<T>> l: before.listeners) {
            l.apply (result);
        }
        return true;
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

    @Override public ATry<T> get () {
        ATry<T> result;

        while ((result = state.get().value) == null) {
            State<T> before;
            do {
                before = state.get ();
            }
            while (! state.compareAndSet (before, before.withWaiter (Thread.currentThread ())));

            LockSupport.park (); //TODO exception handling, especially InterruptedException

            do {
                before = state.get ();
            }
            while (! state.compareAndSet (before, before.withoutWaiter (Thread.currentThread ())));
        }

        return result;
    }

    @Override public boolean isDone () {
        return state.get ().value != null;
    }

    @Override public AOption<ATry<T>> value () {
        return AOption.fromNullable (state.get().value);
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
        State<T> before;

        do {
            before = state.get ();
            if (before.value != null) {
                // execute listener immediately if it is registered after the promise is fulfilled
                listener.apply (before.value);
                return;
            }
        }
        while (! state.compareAndSet (before, before.withListener (listener)));
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

    private static class State<T> {
        final ATry<T> value;
        final AList<Thread> waiters;
        final AList<AStatement1NoThrow<ATry<T>>> listeners;

        @SuppressWarnings ("unchecked")
        static final State INITIAL = new State (null, AList.nil (), AList.nil ());

        @SuppressWarnings ("unchecked")
        static <T> State<T> initial() {
            return INITIAL;
        }

        State (ATry<T> value, AList<Thread> waiters, AList<AStatement1NoThrow<ATry<T>>> listeners) {
            this.value = value;
            this.waiters = waiters;
            this.listeners = listeners;
        }

        State<T> withValue (ATry<T> value) {
            return new State<> (value, waiters, listeners);
        }

        State<T> withWaiter (Thread waiter) {
            return new State<> (value, waiters.cons (waiter), listeners);
        }
        State<T> withoutWaiter (final Thread waiter) {
            return new State<> (value, waiters.filter (new APredicateNoThrow<Thread> () {
                @Override public boolean apply (Thread o) {
                    return o != waiter;
                }
            }), listeners);
        }

        State<T> withListener (AStatement1NoThrow<ATry<T>> listener) {
            return new State<> (value, waiters, listeners.cons (listener));
        }

        @Override public String toString () {
            return "State{" +
                    "value=" + value +
                    ", waiters=" + waiters +
                    ", listeners=" + listeners +
                    '}';
        }
    }
}
