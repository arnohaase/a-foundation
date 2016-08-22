package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.immutable.ATry;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.*;
import com.ajjpj.afoundation.util.AUnchecker;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;


class AFutureImpl<T> implements ASettableFuture<T> {
    private final AThreadPool internalThreadPool;
    private final AtomicReference<State<T>> state = new AtomicReference<> (new State<> (null, AList.nil ()));

    public AFutureImpl (AThreadPool internalThreadPool) {
        this.internalThreadPool = internalThreadPool;
    }

    public static <T> AFutureImpl<T> fromTry (AThreadPool tp, ATry<T> o) {
        final AFutureImpl<T> result = new AFutureImpl<> (tp);
        result.complete (o);
        return result;
    }

    @Override public AFuture<T> onComplete (AThreadPool tp, AStatement1<ATry<T>, ?> handler) {
        State<T> before, after;
        do {
            before = state.get ();
            if (before.isComplete ()) {
                fireListener (tp, handler, before.value);
                return this;
            }
            after = before.withListener (tp, handler);
        }
        while (! state.compareAndSet (before, after));
        return this;
    }

    @Override public boolean isComplete () {
        return state.get ().isComplete ();
    }

    @Override public AOption<ATry<T>> optValue () {
        return AOption.fromNullable (state.get ().value);
    }

    @Override public void await (long atMost, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        final CompletionLatch l = new CompletionLatch ();
        onComplete (AThreadPool.SYNC_THREADPOOL, x -> l.releaseShared (1));
        if (! l.tryAcquireNanos (1, timeUnit.toNanos (atMost))) {
            throw new TimeoutExceptionWithoutStackTrace ();
        }
    }

    @Override public T value (long atMost, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        await (atMost, timeUnit);
        return optValue ().get ().getValue ();
    }

    @Override public AFuture<Throwable> inverse() {
        final AFutureImpl<Throwable> result = new AFutureImpl<> (AThreadPool.SYNC_THREADPOOL);

        onComplete (AThreadPool.SYNC_THREADPOOL, res -> {
            result.complete (res.inverse ());
        });

        return result;
    }

    @Override public void complete (ATry<T> o) {
        if (!tryComplete (o)) throw new IllegalStateException ("trying to complete an already completed future");
    }

    private void fireListener (AThreadPool tp, AStatement1<ATry<T>, ?> f, ATry<T> value) {
        tp.submit (() -> {
            try {
                f.apply (value);
            }
            catch (Throwable th) {
                AUnchecker.throwUnchecked (th);
            }
        });
    }

    @Override public boolean tryComplete (ATry<T> o) {
        State<T> before, after;

        do {
            before = state.get ();
            if (before.isComplete ()) return false;

            // set the result, and remove all listeners at the same time
            after = new State<> (o, AList.nil ());
        }
        while (! state.compareAndSet (before, after));

        // fire all listeners registered *before* the call to complete
        before.listeners.foreach (l -> fireListener (l._1, l._2, o));

        // ... and fire all listeners registered concurrently. After this call, new listeners will be executed immediately rather than stored, so this is sufficient
        state.get().listeners.foreach (l -> fireListener (l._1, l._2, o));

        return true;
    }

    //TODO completeWith, tryCompleteWith

//    @Override public <S> AFuture<S> transform (AThreadPool tp, AFunction1<T, S, ?> s, AFunction1<Throwable, Throwable, ?> t) {
//        final AFutureImpl<S> result = new AFutureImpl<> ();
//
//        onComplete (tp, res -> {
//            TODO NonFatal?!
//        });
//
//        return result;
//    }

    @Override public <S> AFuture<S> map (AThreadPool tp, AFunction1<T, S, ?> f) {
        final AFutureImpl<S> result = new AFutureImpl<> (tp);

        onComplete (tp, v -> {
            try {
                result.complete (v.map (f));
            }
            catch (Throwable th) {
                result.completeAsFailure (th);
            }
        });

        return result;
    }

    @Override public <S> AFuture<S> flatMap (AThreadPool tp, AFunction1<T, AFuture<S>, ?> f) {
        final AFutureImpl<S> result = new AFutureImpl<> (tp);

        onComplete (tp, v -> {
            if (v.isFailure()) {
                //noinspection unchecked
                result.complete ((ATry<S>) v);
            }
            else {
                f.apply (v.getValue()).onComplete (tp, result::complete); //TODO Scala: NonFatal vs. Fatal exceptions?!
            }
        });

        return result;
    }

    @Override public AFuture<T> filter (AThreadPool tp, APredicate<T, ?> f) {
        return map (tp, x -> {
            if (f.apply (x)) return x;
            else throw new NoSuchElementException ();
        });
    }

    @Override public <S> AFuture<S> collect (AThreadPool tp, APartialFunction<T, S, ?> f) {
        return map (tp, x -> {
            if (f.isDefinedAt (x)) return f.apply (x);
            else throw new NoSuchElementException ();
        });
    }

    @Override public AFuture<T> recover (AThreadPool tp, APartialFunction<Throwable, T, ?> f) {
        final AFutureImpl<T> result = new AFutureImpl<> (tp);
        onComplete (tp, x -> result.complete (x.recover (f)));
        return result;
    }

    @Override public AFuture<T> recoverWith (AThreadPool tp, APartialFunction<Throwable, AFuture<T>, ?> f) {
        final AFutureImpl<T> result = new AFutureImpl<> (tp);

        onSuccess (AThreadPool.SYNC_THREADPOOL, result::completeAsSuccess);
        onFailure (AThreadPool.SYNC_THREADPOOL, th -> {
            if (! f.isDefinedAt (th)) result.completeAsFailure (th);
            else {
                f.apply (th).onComplete (tp, result::complete);
            }
        });

        return result;
    }

    @Override public <S> AFuture<ATuple2<T, S>> zip (AFuture<S> that) {
        final AFutureImpl<ATuple2<T,S>> result = new AFutureImpl<> (internalThreadPool);
        onComplete (internalThreadPool, first -> {
            if (first.isFailure ()) {
                //noinspection unchecked
                result.complete ((ATry) first);
            }
            else {
                that.onComplete (internalThreadPool, second -> {
                    result.complete (second.map (v -> new ATuple2<> (first.getValue (), v)));
                });
            }
        });
        return result;
    }

    @Override public AFuture<T> fallbackTo (AFuture<T> that) {
        final AFutureImpl<T> result = new AFutureImpl<> (internalThreadPool);
        onComplete (internalThreadPool, first -> {
            if (first.isSuccess()) result.complete (first);
            else {
                that.onComplete (internalThreadPool, second -> {
                    if (second.isSuccess ()) result.complete (second);
                    else result.complete (first); // if both failed, complete with the first AFuture's throwable
                });
            }
        });
        return result;
    }

    @Override public AFuture<T> andThen (AThreadPool tp, APartialStatement<ATry<T>, ?> f) {
        final AFutureImpl<T> result = new AFutureImpl<> (tp);
        onComplete (tp, v -> {
            try {
                if (f.isDefinedAt (v)) f.apply (v);
            }
            finally {
                result.complete (v);
            }
        });
        return result;
    }


    static class State<T> {
        final ATry<T> value;
        final AList<ATuple2<AThreadPool, AStatement1<ATry <T>, ?>>> listeners;

        State (ATry<T> value, AList<ATuple2<AThreadPool, AStatement1<ATry<T>, ?>>> listeners) {
            this.value = value;
            this.listeners = listeners;
        }

        State<T> withListener (AThreadPool tp, AStatement1<ATry<T>, ?> l) {
            return new State<> (value, listeners.cons (new ATuple2<> (tp, l)));
        }

        boolean isComplete() {
            return value != null;
        }

        @Override public String toString () {
            return "State{" +
                    "value=" + value +
                    ", listeners=" + listeners +
                    '}';
        }
    }

    static class CompletionLatch extends AbstractQueuedSynchronizer {
        @Override protected int tryAcquireShared (int ignored) {
            return getState () == 0 ? 1 : -1;
        }

        @Override protected boolean tryReleaseShared (int ignored) {
            setState (1);
            return true;
        }
    }
}
