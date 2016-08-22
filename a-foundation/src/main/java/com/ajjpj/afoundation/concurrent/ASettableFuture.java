package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.immutable.ATry;


/**
 * This interface represents an {@link AFuture} with additional API for completing it explicitly, either as a success
 *  or as a failure. These methods are not part of {@link AFuture} intentionally: Most clients being handed a future
 *  should be able to wait for and handle the result, but <em>not</em> provide or modify it. ASettableFuture is
 *  usually not an interface applications want to pass around.<p>
 *
 * This is the equivalent to what is called a 'Promise' in Scala. The more explicit name ASettableFuture was used because
 *  'Future' and 'Promise' are used more or less interchangeably in most contexts, and discussions about the 'right'
 *  way to use them tend to be heated.
 */
public interface ASettableFuture<T> extends AFuture<T> {
    /**
     * This creates an ASettableFuture instance. Once created, it can be passed around as an {@link AFuture} safely, and
     *  be completed concurrently from a different thread.
     */
    static <T> ASettableFuture<T> create () {
        return create (AThreadPool.SYNC_THREADPOOL);
    }

    /**
     * This creates an ASettableFuture instance with a given thread pool as its internal thread pool. Once created, it
     *  can be passed around as an {@link AFuture} safely, and be completed concurrently from a different thread.
     */
    static <T> ASettableFuture<T> create (AThreadPool pool) {
        return new AFutureImpl<> (pool);
    }

    /**
     * This method completes this ASettableFuture. The ASettableFuture can be completed only once, subsequent attempts to
     *  do so will cause an IllegalStateException to be thrown.
     */
    void complete (ATry<T> o);

    /**
     * This method completes this ASettableFuture, return {@code true} if it was previously uncompleted, and {@code false}
     *  otherwise. {@code} true means that the ASettableFuture is now completed using the parameter, while {@code false}
     *  means the parameter was ignored.
     */
    boolean tryComplete (ATry<T> o);

    /**
     * Completes the ASettableFuture as a success with the given value. The ASettableFuture can be completed only once, subsequent attempts to
     *  do so will cause an IllegalStateException to be thrown.
     */
    default void completeAsSuccess (T o) {
        complete (ATry.success (o));
    }

    /**
     * Completes the ASettableFuture as a failure with the given Throwable. The ASettableFuture can be completed only once, subsequent attempts to
     *  do so will cause an IllegalStateException to be thrown.
     */
    default void completeAsFailure (Throwable th) {
        complete (ATry.failure (th));
    }
}
