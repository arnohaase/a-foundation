package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.immutable.ATry;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


//TODO this AFuture's thread pool as default thread pool

public interface AFuture<T> {
    void await (long atMost, TimeUnit timeUnit) throws TimeoutException, InterruptedException;
    T value (long atMost, TimeUnit timeUnit) throws TimeoutException, InterruptedException;

    //TODO onSuccess, onFailure with APartialStatement
    default AFuture<T> onSuccess  (AThreadPool tp, AStatement1<T, ?> handler) {
        return onComplete (tp, t -> t.foreach (handler));
    }
    default AFuture<T> onFailure  (AThreadPool tp, AStatement1<Throwable, ?> handler) {
        return onComplete (tp, t -> t.inverse ().foreach (handler));
    }

    AFuture<T> onComplete (AThreadPool tp, AStatement1<ATry<T>, ?> handler);

    boolean isComplete ();
    AOption<ATry<T>> optValue ();

    AFuture<Throwable> inverse ();

//    <S> AFuture<S> transform (AThreadPool tp, AFunction1<T, S, ?> s, AFunction1<Throwable, Throwable, ?> t);

    <S> AFuture<S> map (AThreadPool tp, AFunction1<T, S, ?> f);
    <S> AFuture<S> flatMap (AThreadPool tp, AFunction1<T, AFuture<S>, ?> f);

    AFuture<T> filter (AThreadPool tp, APredicate<T, ?> f);

    <S> AFuture<S> collect (AThreadPool tp, APartialFunction<T, S, ?> f);

    AFuture<T> recover (AThreadPool tp, APartialFunction<Throwable, T, ?> f);

    <S> AFuture<ATuple2<T,S>> zip (AFuture<S> that);
    AFuture<T> fallbackTo (AFuture<T> that);

    AFuture<T> andThen (AThreadPool tp, APartialStatement<ATry<T>, ?> f);


    static <T> AFuture<T> createSuccessful (T o) {
        return fromTry (ATry.success (o));
    }

    static <T> AFuture<T> createFailed (Throwable th) {
        return fromTry (ATry.failure (th));
    }

    static <T> AFuture<T> fromTry (ATry<T> t) {
        return AFutureImpl.fromTry (AThreadPool.SYNC_THREADPOOL, t);
    }

    static <T> AFuture<T> submit (AThreadPool tp, AFunction0<T, ?> f) {
        return StaticFutureMethods.submit (tp, f);
    }

    static <T> AFuture<List<T>> lift (AThreadPool tp, Iterable<AFuture<T>> futures) {
        return StaticFutureMethods.lift (tp, futures);
    }

    static <T> AFuture<T> firstCompleted (AThreadPool tp, Iterable<AFuture<T>> futures) {
        return StaticFutureMethods.firstCompleted (tp, futures);
    }

    static <T> AFuture<AOption<T>> find (AThreadPool tp, Iterable<AFuture<T>> futures, APredicate<T, ?> f) {
        return StaticFutureMethods.find (tp, futures, f);
    }

    static <T,R> AFuture<R> fold (AThreadPool tp, R start, Iterable<AFuture<T>> futures, AFunction2<R, T, R, ?> f) {
        return StaticFutureMethods.fold (tp, start, futures, f);
    }

    //TODO
//    static <T> AFuture<T> reduce (AThreadPool tp, Iterable<AFuture<T>> futures, AFunction2<T, T, T, ?> f) {
//        return StaticFutureMethods.reduce (tp, futures, f);
//    }

    static <T, R, E extends Throwable> AFuture<List<R>> traverse (AThreadPool tp, Iterable<T> values, AFunction1<T, AFuture<R>, E> f) throws E {
        return StaticFutureMethods.traverse (tp, values, f);
    }
}


