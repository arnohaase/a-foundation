package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AFunction2;
import com.ajjpj.afoundation.function.AStatement2NoThrow;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class provides a collection of static helper functions for working with {@link AFuture}s.
 *
 * @author arno
 */
@SuppressWarnings ("Convert2Lambda")
public class AFutureHelper {

    /**
     * This method combines a collection of futures into a single future. <p>
     *
     * Conceptually, it holds a single value <code>u</code> with an initial value of <code>initial</code>. When a future finishes successfully,
     *  the function <code>function</code> is called with the old value of <code>u</code> and the future's value; the function's result is then
     *  used as the new value of <code>u</code>. When all futures' values have thus been applied, the final value of <code>u</code> is the resulting
     *  future's value.<p>
     *
     * This is of course done in a fully non-blocking way.<p>
     *
     * If one of the futures fails, the 'collected' future fails as well.
     */
    public static <T,U,E extends Exception> AFuture<U> collect (Collection<AFuture<T>> futures, final U initial, final AFunction2<U,T,U,E> function) {
        if (futures.isEmpty ()) {
            throw new IllegalArgumentException ("only non-empty collections can be lifted");
        }

        AFuture<U> result = null;
        for (final AFuture<T> f: futures) {
            if (result == null) {
                result = f.mapSync (new AFunction1<T, U, E> () {
                    @Override public U apply (T param) throws E {
                        return function.apply (initial, param);
                    }
                });
            }
            else {
                result = result.mapSync (new AFunction1<U, U, Exception> () {
                    @Override public U apply (U param) throws Exception {
                        return function.apply (param, f.get ());
                    }
                });
            }
        }
        return result;
    }

    /**
     * This method combines a collection of futures into a single future with the combined results of that list
     *  as its value. If one of the original futures fails (or times out), that causes the resulting future to
     *  fail with the same exception as the cause.<p>
     *
     * This the generalization of {@link AFuture#zip(AFuture)} and {@link AFuture#zip(AFuture, AFuture)}.
     */
    @SuppressWarnings ("unchecked")
    public static <T> AFuture<List<T>> lift (Collection<AFuture<T>> futures) {
        if (futures.isEmpty ()) {
            throw new IllegalArgumentException ("only non-empty collections can be lifted");
        }

        final AFutureImpl<List<T>> result = ((AFutureImpl) futures.iterator ().next ()).unscheduled ();
        final ListCollector<T> collector = new ListCollector<> (futures.size ());

        int idx = 0;
        for (AFuture<T> f: futures) {
            final int curIdx = idx;
            f.onFinished (new AStatement2NoThrow<T, Throwable> () {
                @Override public void apply (T param1, Throwable param2) {
                    if (param2 != null) {
                        result.setException (param2);
                    }
                    else {
                        if (collector.setValue (curIdx, param1) == 0) {
                            result.set ((List) Arrays.asList (collector.data));
                        }
                    }
                }
            });

            idx += 1;
        }
        return result;
    }

    public static <T> AFuture<T> anyOf (AFuture<T>... futures) {
        return anyOf (Arrays.asList (futures));
    }

    /**
     * Combines a collection of futures into a single future on a 'first come, first serve' base. The first
     *  of the original futures to finish successfully determines the value of the resulting future. Failures
     *  of some of the original futures are ignored; only if (and when) they all fail, the resulting future
     *  fails as well, giving the cause of failure of the last original future as its own cause of failure.
     */
    @SuppressWarnings ("unchecked")
    public static <T> AFuture<T> anyOf (Collection<AFuture<T>> futures) {
        if (futures.isEmpty ()) {
            throw new IllegalArgumentException ("'anyOf' only supports non-empty collections");
        }

        final AFutureImpl<T> result = ((AFutureImpl) futures.iterator ().next ()).unscheduled ();
        final AtomicInteger numUnfinished = new AtomicInteger (futures.size ());

        final AStatement2NoThrow<T, Throwable> listener = new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 == null) {
                    result.set (param1);
                }

                final int remaining = numUnfinished.decrementAndGet ();
                if (remaining == 0) {
                    result.setException (param2);
                }
            }
        };

        for (AFuture<T> f: futures) {
            f.onFinished (listener);
        }

        return result;
    }

    private static class ListCollector<T> {
        volatile Object[] data;
        final AtomicInteger numEmpty;

        public ListCollector (int size) {
            this.data = new Object[size];
            numEmpty = new AtomicInteger (size);
        }

        @SuppressWarnings ("SillyAssignment")
        int setValue (int idx, T value) {
            data[idx] = value;
            data = data; // to ensure visibility of the above write
            return numEmpty.decrementAndGet ();
        }
    }
}
