package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.collection.immutable.AList;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.immutable.ATry;
import com.ajjpj.afoundation.function.AFunction0;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.function.AFunction2;
import com.ajjpj.afoundation.function.APredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


class StaticFutureMethods {
    public static <T, E extends Throwable> AFuture<T> submit (AThreadPool tp, AFunction0<T, E> f) {
        final AFutureImpl<T> result = new AFutureImpl<> (tp);
        tp.submit (() -> {
            try {
                result.completeAsSuccess (f.apply ());
            }
            catch (Throwable th) {
                result.completeAsFailure (th);
                //TODO distinguish between safe and unsafe throwables
            }
        });
        return result;
    }

    public static <T> AFuture<AList<T>> lift (AThreadPool tp, Iterable<AFuture<T>> futures) {
        final AFutureImpl<AList<T>> result = new AFutureImpl<> (tp);

        int numFutures = 0;
        for (AFuture<T> ignored: futures) numFutures += 1;
        final AtomicInteger numUnbound = new AtomicInteger (numFutures);

        final List<T> list = new ArrayList<> ();
        for (AFuture<T> f: futures) {
            int idx = list.size();
            list.add (null);

            f.onComplete (AThreadPool.SYNC_THREADPOOL, tr -> {
                if (tr.isSuccess ()) {
                    list.set (idx, tr.getValue ());
                    if (numUnbound.decrementAndGet () == 0) {
                        result.tryComplete (ATry.success (AList.create (list)));
                    }
                }
                else {
                    //noinspection unchecked
                    result.tryComplete ((ATry<AList<T>>) tr);
                }
            });
        }

        if (list.isEmpty ()) {
            result.completeAsSuccess (AList.nil ());
        }

        return result;
    }


    public static <T> AFuture<T> firstCompleted (AThreadPool tp, Iterable<AFuture<T>> futures) {
        final AFutureImpl<T> result = new AFutureImpl<> (tp);

        for (AFuture<T> f: futures) {
            f.onComplete (AThreadPool.SYNC_THREADPOOL, result::tryComplete);
        }

        return result;
    }

    public static <T> AFuture<AOption<T>> find (AThreadPool tp, Iterable<AFuture<T>> futures, APredicate<T, ?> f) {
        int numFutures = 0;
        for (AFuture<T> ignored: futures) numFutures += 1;
        final AtomicInteger numOpen = new AtomicInteger (numFutures);

        if (numFutures == 0) {
            return AFuture.createSuccessful (AOption.none ());
        }

        final AFutureImpl<AOption<T>> result = new AFutureImpl<> (tp);

        for (AFuture<T> future: futures) {
            future.onComplete (tp, tr -> {
                if (tr.isSuccess () && f.apply (tr.getValue ())) {
                    result.tryComplete (tr.map (AOption::some));
                }
                else {
                    if (numOpen.decrementAndGet () == 0) {
                        result.tryComplete (ATry.success (AOption.none ()));
                    }
                }
            });
        }

        return result;
    }

    public static <R, T, E extends Throwable> AFuture<R> fold (AThreadPool tp, R start, Iterable<AFuture<T>> futures, AFunction2<R, T, R, E> f) {
        if (! futures.iterator ().hasNext ()) return AFuture.createSuccessful (start);

        final AFuture<AList<T>> lifted = StaticFutureMethods.lift (tp, futures);
        return lifted.map (tp, x -> x.foldLeft (start, f));
    }

    //TODO AMonadicOps.reduceLeft
//    public static <T, E extends Throwable> AFuture<T> reduce (AThreadPool tp, Iterable<AFuture<T>> futures, AFunction2<T, T, T, E> f) {
//        final AFuture<List<T>> lifted = StaticFutureMethods.lift (tp, futures);
//        return lifted.map (tp, x -> ACollectionHelper.asACollectionView (x).<R,E>reduceLeft (f));
//    }

    public static <R, T, E extends Throwable> AFuture<AList<R>> traverse (AThreadPool tp, Iterable<T> values, AFunction1<T, AFuture<R>, E> f) throws E {
        final List<AFuture<R>> result = new ArrayList<> ();
        for (T o: values) {
            result.add (f.apply (o));
        }
        return lift (tp, result);
    }
}
