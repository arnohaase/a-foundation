package com.ajjpj.abase.concurrent;

import com.ajjpj.abase.collection.tuples.ATuple2;
import com.ajjpj.abase.collection.tuples.ATuple3;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.AStatement1NoThrow;
import com.ajjpj.abase.function.AStatement2NoThrow;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author arno
 */
public interface AFuture<T> {
    boolean cancel (boolean mayInterruptIfRunning);
    boolean isCancelled ();
    boolean isDone ();

    AFuture<T> withDefaultValue (T defaultValue);

    T get () throws InterruptedException, ExecutionException;
    T get(long timeout, TimeUnit timeoutUnit) throws InterruptedException, ExecutionException, TimeoutException;

    void onSuccess (AStatement1NoThrow<T> callback);
    void onFailure (AStatement1NoThrow<Throwable> callback);
    void onFinished (AStatement2NoThrow<T, Throwable> callback);

    <U, E extends Exception> AFuture<U> mapSync (AFunction1<T, U, E> f);
    <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f, long timeout, TimeUnit timeoutUnit);
    <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f, ATaskScheduler threadPool, long timeout, TimeUnit timeoutUnit);

    <U> AFuture<ATuple2<T,U>> zip (AFuture<U> other);
    <U,V> AFuture<ATuple3<T,U,V>> zip (AFuture<U> other1, AFuture<V> other2);


    //TODO ATuple3 --> a-base
    //TODO AStatement2 --> a-base
}
