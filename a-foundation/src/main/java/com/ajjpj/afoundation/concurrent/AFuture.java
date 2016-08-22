package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.immutable.ATry;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * This interface represents a value that is is being computed and may or may not be present. An AFuture can complete
 *  <em>successfully</em>, in which case it provides a value, or <em>fail</em>, in which case it can provide a Throwable
 *  that caused it to fail.<p>
 *
 * Most operations on an AFuture are asynchronous and require an {@link AThreadPool} to be passed in, which is then
 *  used to schedule any underlying operations.<p>
 *
 * The typical way to create an AFuture is to submit some code to a thread pool calling the static method {@link AFuture#submit}.
 *  Processing the result can be done in a non-blocking way, registering callbacks using {@link AFuture#onComplete} or
 *  {@link AFuture#onSuccess(AThreadPool, AStatement1)} and {@link AFuture#onFailure(AThreadPool, AStatement1)}, respectively. <p>
 *
 * This API and the underlying abstractions were strongly influenced by the Scala standard library - thanks for the
 *  great work done there!
 */
public interface AFuture<T> {
    /**
     * This method blocks until the AFuture completes (successfully or failing), waiting at most for a given timeout.
     *  While this wastes resources - the calling thread is not available for work during the blocking wait - it can
     *  simplify some code and is included in the API as a pragmatic concession. <p>
     * CAUTION: AThreadPool instances have a bounded number of threads, so calling this method from a worker thread can
     *  actually starve the entire thread pool, leading to dead lock!
     */
    void await (long atMost, TimeUnit timeUnit) throws TimeoutException, InterruptedException;

    /**
     * This method blocks until the AFuture completes (successfully or failing) and returns its result, waiting at most for a given timeout.
     *  While this wastes resources - the calling thread is not available for work during the blocking wait - it can
     *  simplify some code and is included in the API as a pragmatic concession. <p>
     * CAUTION: AThreadPool instances have a bounded number of threads, so calling this method from a worker thread can
     *  actually starve the entire thread pool, leading to dead lock!
     */
    T value (long atMost, TimeUnit timeUnit) throws TimeoutException, InterruptedException;

    /**
     * This method registers a callback that is guaranteed to be called exactly once when the AFuture completes
     *  successfully; for details see {@link AFuture#onComplete(AThreadPool, AStatement1)}. The AFuture's result is
     *  passed to the callback as a parameter.
     */
    default AFuture<T> onSuccess  (AThreadPool tp, AStatement1<T, ?> callback) {
        return onComplete (tp, t -> t.foreach (callback));
    }

    /**
     * This method registres a callback that is guaranteed to be called exactly once when the AFuture completes
     *  as a failure; for details see {@link AFuture#onComplete(AThreadPool, AStatement1)}. The Throwable that caused
     *  the AFuture to fail is passed to the callback as a parameter.
     */
    default AFuture<T> onFailure  (AThreadPool tp, AStatement1<Throwable, ?> callback) {
        return onComplete (tp, t -> t.inverse ().foreach (callback));
    }

    /**
     * Same as {@link AFuture#onSuccess(AThreadPool, AStatement1)} except that the callback is only called if it
     *  {@link APartialStatement#isDefinedAt(Object) is defined at} the AFuture's result.
     */
    default AFuture<T> onSuccess  (AThreadPool tp, APartialStatement<T, ?> callback) {
        return onComplete (tp, t -> t.foreach (x -> {
            if (callback.isDefinedAt (x)) callback.apply (x);
        }));
    }

    /**
     * Same as {@link AFuture#onFailure(AThreadPool, AStatement1)} except that the callback is only called if it
     *  {@link APartialStatement#isDefinedAt(Object) is defined at} the Throwable causing the AFuture to fail.
     */
    default AFuture<T> onFailure (AThreadPool tp, APartialStatement<Throwable, ?> callback) {
        return onComplete (tp, t -> t.inverse ().foreach (x -> {
            if (callback.isDefinedAt (x)) callback.apply (x);
        }));
    }

    /**
     * This method registers a callback that is guaranteed to be called exactly once when the AFuture completes, either
     *  successfully or failing. The callback will also be called if it is registered <em>after</em> the AFuture
     *  completes.<p>
     * In either case, the callback will be executed by submitting it to the thread pool that is passed in as the first
     *  parameter. This is done for consistency and to avoid stack overflows in case of long cascades of futures.<p>
     * The callback's parameter is an instance of {@link ATry}, holding either the AFuture's result as {@link ATry#success(Object)}
     *  or the causing Throwable as {@link ATry#failure(Throwable)}.
     */
    AFuture<T> onComplete (AThreadPool tp, AStatement1<ATry<T>, ?> callback);

    /**
     * @return true if and only if the AFuture is completed, either successfully or as a failure. NB: A result of
     *  {@code false} may be outdated by the time calling code evaluates it, and registering a callback is
     *  usually a better alternative to polling for completion.
     *
     */
    boolean isComplete ();

    /**
     * This method returns the AFuture's result if it is completed, or {@link AOption#none()} otherwise. NB: A result
     *  of none() may be outdated by the time calling code evaluates it, and registering a callback is
     *  usually a better alternative to polling for completion.
     */
    AOption<ATry<T>> optValue ();

    /**
     * This method returns a new AFuture with inverse 'success' semantics of the original AFuture: If the original AFuture
     *  fails, it completes successfully with the Throwable as its value, and if the original AFuture completes
     *  successfully, the newly created AFuture fails with an {@link SuccessfulCompletionException} as its cause.
     */
    AFuture<Throwable> inverse ();

//    <S> AFuture<S> transform (AThreadPool tp, AFunction1<T, S, ?> s, AFunction1<Throwable, Throwable, ?> t);

    /**
     * Creates a new AFuture by applying a function to the successful result of
     *  this future. If this future is completed with an exception then the new
     *  future will also contain this exception.
     */
    <S> AFuture<S> map (AThreadPool tp, AFunction1<T, S, ?> f);

    /**
     * Creates a new AFuture by applying a function to the successful result of
     *  this future, and returns the result of the function as the new future.
     *  If this future is completed with an exception then the new future will
     *  also contain this exception.
     */
    <S> AFuture<S> flatMap (AThreadPool tp, AFunction1<T, AFuture<S>, ?> f);

    /**
     * Creates a new future by filtering the value of the current future with a predicate.
     *
     *  If the current future contains a value which satisfies the predicate, the new future will also hold that value.
     *  Otherwise, the resulting future will fail with a `NoSuchElementException`.
     *
     *  If the current future fails, then the resulting future also fails.
     */
    AFuture<T> filter (AThreadPool tp, APredicate<T, ?> f);

    /**
     * Creates a new future by mapping the value of the current future, if the given partial function is defined at that value.
     *
     *  If the current future contains a value for which the partial function is defined, the new future will also hold that value.
     *  Otherwise, the resulting future will fail with a `NoSuchElementException`.
     *
     *  If the current future fails, then the resulting future also fails.
     */
    <S> AFuture<S> collect (AThreadPool tp, APartialFunction<T, S, ?> f);

    /**
     * Creates a new future that will handle any matching throwable that this
     *  future might contain. If there is no match, or if this future contains
     *  a valid result then the new future will contain the same.
     */
    AFuture<T> recover (AThreadPool tp, APartialFunction<Throwable, T, ?> f);

    /**
     * Creates a new future that will handle any matching throwable that this
     *  future might contain by assigning it a value of another future. <p>
     *
     * If there is no match, or if this future contains
     *  a valid result then the new future will contain the same result. <p>
     *
     * This method is similar to {@link AFuture#recover}, the difference being that
     *  this method's recovery function returns an AFuture rather than the actual
     *  recovery value.
     */
    AFuture<T> recoverWith (AThreadPool tp, APartialFunction<Throwable, AFuture<T>, ?> f);

    /**
     * Zips the values of 'this' and 'that' future, and creates a new future holding the tuple of their results.<p>
     *
     * If 'this' future fails, the resulting future is failed with the throwable stored in 'this'. Otherwise, if
     *  'that' future fails, the resulting future is failed with the throwable stored in 'that'.
     */
    <S> AFuture<ATuple2<T,S>> zip (AFuture<S> that);

    /**
     * Creates a new future which holds the result of this future if it was completed successfully, or, if not,
     *  the result of the 'that' future if 'that' is completed successfully. If both futures are failed, the
     *  resulting future holds the throwable object of the first future.<p>
     *
     * Using this method will not cause concurrent programs to become nondeterministic.
     */
    AFuture<T> fallbackTo (AFuture<T> that);

    /**
     * Applies the side-effecting function to the result of this future, and returns a new future with the result of this future.
     *  This method allows one to enforce that the callbacks are executed in a specified order.<p>
     *
     * Note that if one of the chained `andThen` callbacks throws an exception, that exception is not propagated to the
     *  subsequent 'andThen' callbacks. Instead, the subsequent 'andThen' callbacks are given the original value of this future.
     */
    AFuture<T> andThen (AThreadPool tp, APartialStatement<ATry<T>, ?> f);


    /**
     * This method returns a new AFuture that is completed successfully with the given value.
     */
    static <T> AFuture<T> createSuccessful (T o) {
        return fromTry (ATry.success (o));
    }

    /**
     * This method returns a new AFuture that is failed with the given Throwable.
     */
    static <T> AFuture<T> createFailed (Throwable th) {
        return fromTry (ATry.failure (th));
    }

    /**
     * This method creates a new AFuture that is completed with the given ATry.
     */
    static <T> AFuture<T> fromTry (ATry<T> t) {
        return AFutureImpl.fromTry (AThreadPool.SYNC_THREADPOOL, t);
    }

    /**
     * This method submits a given function to a given AThreadPool, returning an AFuture that completes once the
     *  function completes. The returned AFuture will complete successfully with the function's return value if the
     *  function completes normally, or fail if the function throws a Throwable.
     */
    static <T> AFuture<T> submit (AThreadPool tp, AFunction0<T, ?> f) {
        return StaticFutureMethods.submit (tp, f);
    }

    /**
     * This method turns a collection of AFutures into a single AFuture of a list of futures. It is the rough
     *  equivalent of joining different threads, but doing any blocking.
     */
    static <T> AFuture<AList<T>> lift (AThreadPool tp, Iterable<AFuture<T>> futures) {
        return StaticFutureMethods.lift (tp, futures);
    }

    /**
     * This method returns a new AFuture that completes once the first of a given collection of futures completes,
     *  completing with that future's result. If the first of the AFutures fails, the newly created AFuture fails
     *  as well.<p>
     *
     * NB: This method does not affect the other futures from running and
     *  completing, it just ignores their outcome.
     */
    static <T> AFuture<T> firstCompleted (AThreadPool tp, Iterable<AFuture<T>> futures) {
        return StaticFutureMethods.firstCompleted (tp, futures);
    }

    /**
     * This method matches the (successful) outcomes of a given collection of AFutures against a predicate, returning
     *  the first matching result wrapped in an AFuture, or AOption.none() wrapped in an AFuture if none of the AFutures
     *  complete successfully with a value matching the predicate.<p>
     * The returned AFuture never fails, even if all given AFutures fail.
     */
    static <T> AFuture<AOption<T>> find (AThreadPool tp, Iterable<AFuture<T>> futures, APredicate<T, ?> f) {
        return StaticFutureMethods.find (tp, futures, f);
    }

    /**
     * A non-blocking fold over the specified futures, with the start value of the given zero.
     *  The fold is performed on the thread where the last future is completed,
     *  the result will be the first failure of any of the futures, or any failure in the actual fold,
     *  or the result of the fold.
     */
    static <T,R> AFuture<R> fold (AThreadPool tp, R start, Iterable<AFuture<T>> futures, AFunction2<R, T, R, ?> f) {
        return StaticFutureMethods.fold (tp, start, futures, f);
    }

    //TODO
//    static <T> AFuture<T> reduce (AThreadPool tp, Iterable<AFuture<T>> futures, AFunction2<T, T, T, ?> f) {
//        return StaticFutureMethods.reduce (tp, futures, f);
//    }

    /**
     * Transforms an {@code Iterable<T>} into an {@code AFuture<AList<R>>} using the provided function
     *  {@code T -> AFuture<R>}. This is useful for performing a parallel map. For example, to apply a function to all items of a list
     *  in parallel.
     */
    static <T, R, E extends Throwable> AFuture<AList<R>> traverse (AThreadPool tp, Iterable<T> values, AFunction1<T, AFuture<R>, E> f) throws E {
        return StaticFutureMethods.traverse (tp, values, f);
    }
}


