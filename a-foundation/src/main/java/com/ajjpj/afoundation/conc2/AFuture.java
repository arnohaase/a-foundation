package com.ajjpj.afoundation.conc2;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AStatement1NoThrow;


/**
 * @author arno
 */
public interface AFuture<T> {
    APromisingExecutor getImplicitThreadPool();

    boolean isDone (); //TODO terminology - 'done', 'finished', 'completed', ... --> ?!?!
    ATry<T> get ();
    AOption<ATry<T>> value();

    void onFinished (AStatement1NoThrow<ATry<T>> listener);
    void onSuccess (AStatement1NoThrow<T> listener);
    void onFailure (AStatement1NoThrow<Throwable> listener);

    /**
     * @return a new <code>AFuture</code> with a lifecycle that is bound to this <code>AFuture</code>'s. The only difference is that if this future fails, the returned
     *  future will instead finish successfully with the provided <code>defaultValue</code>.
     */
    AFuture<T> withDefaultValue (T defaultValue);

    //TODO Awaitable, Await ???
//    T get(long timeout, TimeUnit timeoutUnit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Creates a new <code>AFuture</code>. If this future fails, the newly created future fails as well. If this future completes successfully, a new task is scheduled, applying
     *  the transformation function to this future's result. This method does for futures what
     *  {@link com.ajjpj.afoundation.collection.immutable.ACollection#map(com.ajjpj.afoundation.function.AFunction1)} does for collections.<p>
     *
     * Scheduling is done in the explicitly provided thread pool, using the timeout parameter.
     */
    <U, E extends Exception> AFuture<U> mapAsync (APromisingExecutor threadPool, AFunction1<T, U, E> f);

    <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f);

    /**
     * Combines this <code>AFuture</code> with another <code>AFuture</code> into a single future with an {@link com.ajjpj.afoundation.collection.tuples.ATuple2} of the two values. If either
     *  of the original futures fails, the newly created future fails. If both futures succeed, the newly created future succeeds with the combined value.
     */
    <U> AFuture<ATuple2<T,U>> zip (AFuture<U> other);

    /**
     * Combines this <code>AFuture</code> with two other <code>AFuture</code>s into a single future with an {@link com.ajjpj.afoundation.collection.tuples.ATuple3} of the three values.
     *  If one of the original futures fails, the newly created future fails. If all three futures succeed, the newly created future succeeds with the combined value.<p>
     *
     * If you want to combine more than three futures into a single future, use {@link com.ajjpj.afoundation.concurrent.AFutureHelper#lift(java.util.Collection)}.
     */
//    <U,V> AFuture<ATuple3<T,U,V>> zip (AFuture<U> other1, AFuture<V> other2);

    //TODO recover,
    //TODO collect,
    //TODO filter,
    //TODO flatMap,
    //TODO transform,
    //TODO failed (i.e. a projection),
    //TODO andThen
}
