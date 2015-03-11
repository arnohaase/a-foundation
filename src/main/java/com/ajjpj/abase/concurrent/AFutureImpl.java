package com.ajjpj.abase.concurrent;

import com.ajjpj.abase.collection.immutable.AList;
import com.ajjpj.abase.collection.immutable.AOption;
import com.ajjpj.abase.collection.tuples.ATuple2;
import com.ajjpj.abase.collection.tuples.ATuple3;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.AStatement1NoThrow;
import com.ajjpj.abase.function.AStatement2NoThrow;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author arno
 */
@SuppressWarnings ("Convert2Lambda")
class AFutureImpl<T> extends FutureTask<T> implements AFuture<T> {
    private final AtomicReference<AList<AStatement2NoThrow<T, Throwable>>> onFinishedListeners = new AtomicReference<> (AList.<AStatement2NoThrow<T, Throwable>>nil ());

    private final ATaskScheduler threadPool;
    private volatile boolean wasTimedOut = false;

    private static final Callable NO_CALLABLE = new Callable () {
        @Override public Object call () throws Exception {
            return null;
        }
    };

    <X> AFutureImpl<X> unscheduled () {
        return unscheduled (threadPool);
    }

    @SuppressWarnings ("unchecked")
    static <X> AFutureImpl<X> unscheduled (ATaskScheduler threadPool) {
        return new AFutureImpl<> (threadPool, NO_CALLABLE);
    }

    AFutureImpl (ATaskScheduler threadPool, Callable<T> callable) {
        super (callable);
        this.threadPool = threadPool;
    }
    AFutureImpl (ATaskScheduler threadPool, Runnable runnable, T value) {
        super (runnable, value);
        this.threadPool = threadPool;
    }

     @Override public AFuture<T> withDefaultValue (final T defaultValue) {
         final AFutureImpl<T> result = unscheduled ();

         onFinished (new AStatement2NoThrow<T, Throwable> () {
             @Override public void apply (T param1, Throwable param2) {
                 if (param2 == null) {
                     result.set (param1);
                 }
                 else {
                     result.set (defaultValue);
                 }
             }
         });
         return result;
     }

    @Override protected void set (T t) {
        super.set (t);
    }

    @Override protected void setException (Throwable t) {
        super.setException (t);
    }

    void setTimedOut() {
        wasTimedOut = true;
    }

    @Override protected void done () {
        notifyListeners ();
    }

    private void notifyListeners () {
        T result = null;
        Throwable th = null;

        if (wasTimedOut) {
            th = new TimeoutException ();
        }
        else {
            try {
                result = get ();
            }
            catch (Exception exc) {
                th = exc;
            }
        }

        // This method can be called several times, even concurrently - see the comment in onFinished(). Atomically removing all listeners from the list deals with
        //  that concurrency in a robust fashion.
        for (AStatement2NoThrow<T, Throwable> l: onFinishedListeners.getAndSet (AList.nil)) {
            l.apply (result, th);
        }
    }

    @Override public void onSuccess (final AStatement1NoThrow<T> callback) {
        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 == null) {
                    callback.apply (param1);
                }
            }
        });
    }

    @Override public void onFailure (final AStatement1NoThrow<Throwable> callback) {
        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 != null) {
                    callback.apply (param2);
                }
            }
        });
    }

    @Override public void onFinished (AStatement2NoThrow<T, Throwable> callback) {
        AList<AStatement2NoThrow<T, Throwable>> prev;
        do {
            prev = onFinishedListeners.get ();
        }
        while (! onFinishedListeners.compareAndSet (prev, prev.cons (callback)));

        if (isDone ()) {
            // This is necessary for notifying listeners that are registered *after* the future is done.
            // NB: Doing it this way takes care of the situation that the future is not done at the beginning of onFinished() but becomes so before the listener is registered.
            notifyListeners ();
        }
    }

    @SuppressWarnings ("unchecked")
    @Override public <U, E extends Exception> AFuture<U> mapSync (final AFunction1<T, U, E> f) {
        final AFutureImpl<U> result = new AFutureImpl<> (threadPool, NO_CALLABLE);

        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 != null) {
                    result.setException (param2);
                    return;
                }

                try {
                    result.set (f.apply (param1));
                }
                catch (Exception e) {
                    result.setException (e);
                }
            }
        });

        return result;
    }

    @Override public <U, E extends Exception> AFuture<U> mapAsync (AFunction1<T, U, E> f, long timeout, TimeUnit timeoutUnit) {
        return mapAsync (f, threadPool, timeout, timeoutUnit);
    }

    @SuppressWarnings ("unchecked")
    @Override public <U, E extends Exception> AFuture<U> mapAsync (final AFunction1<T, U, E> f, final ATaskScheduler threadPool, final long timeout, final TimeUnit timeoutUnit) {
        final AFutureImpl<U> result = new AFutureImpl<> (threadPool, NO_CALLABLE);

        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (final T param1, Throwable param2) {
                if (param2 != null) {
                    result.setException (param2);
                    return;
                }

                try {
                    final AFuture<U> mappedFuture = threadPool.submit (new Callable<U> () {
                        @Override public U call () throws Exception {
                            return f.apply (param1);
                        }
                    }, timeout, timeoutUnit);

                    mappedFuture.onFinished (new AStatement2NoThrow<U, Throwable> () {
                        @Override public void apply (U param1, Throwable param2) {
                            if (param2 != null) {
                                result.setException (param2);
                            }
                            else {
                                result.set (param1);
                            }
                        }
                    });
                }
                catch (Throwable exc) {
                    result.setException (exc);
                }
            }
        });

        return result;
    }

    @SuppressWarnings ("unchecked")
    @Override public <U> AFuture<ATuple2<T, U>> zip (AFuture<U> other) {
        final AFutureImpl<ATuple2<T,U>> result = new AFutureImpl<> (threadPool, NO_CALLABLE);

        final ResultCollector<T,U,Object> collector = new ResultCollector<> ();
        collector.set3 (collector);

        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 != null) {
                    result.setException (param2);
                }
                else {
                    final boolean finished = collector.set1 (param1);
                    if (finished) {
                        result.set (new ATuple2<> (collector._1.get (), collector._2.get ()));
                    }
                }
            }
        });
        other.onFinished (new AStatement2NoThrow<U, Throwable> () {
            @Override public void apply (U param1, Throwable param2) {
                if (param2 != null) {
                    result.setException (param2);
                }
                else {
                    final boolean finished = collector.set2 (param1);
                    if (finished) {
                        result.set (new ATuple2<> (collector._1.get (), collector._2.get ()));
                    }
                }
            }
        });

        return result;
    }

    @SuppressWarnings ("unchecked")
    @Override public <U, V> AFuture<ATuple3<T, U, V>> zip (AFuture<U> other1, AFuture<V> other2) {
        final AFutureImpl<ATuple3<T,U,V>> result = new AFutureImpl<> (threadPool, NO_CALLABLE);

        final ResultCollector<T,U,V> collector = new ResultCollector<> ();

        onFinished (new AStatement2NoThrow<T, Throwable> () {
            @Override public void apply (T param1, Throwable param2) {
                if (param2 != null) {
                    result.setException (param2);
                }
                else {
                    final boolean finished = collector.set1 (param1);
                    if (finished) {
                        result.set (new ATuple3<> (collector._1.get (), collector._2.get (), collector._3.get ()));
                    }
                }
            }
        });
        other1.onFinished (new AStatement2NoThrow<U, Throwable> () {
            @Override public void apply (U param1, Throwable param2) {
                if (param2 != null) {
                    result.setException (param2);
                }
                else {
                    final boolean finished = collector.set2 (param1);
                    if (finished) {
                        result.set (new ATuple3<> (collector._1.get (), collector._2.get (), collector._3.get ()));
                    }
                }
            }
        });
        other2.onFinished (new AStatement2NoThrow<V, Throwable> () {
            @Override public void apply (V param1, Throwable param2) {
                if (param2 != null) {
                    result.setException (param2);
                }
                else {
                    final boolean finished = collector.set3 (param1);
                    if (finished) {
                        result.set (new ATuple3<> (collector._1.get (), collector._2.get (), collector._3.get ()));
                    }
                }
            }
        });

        return result;
    }

    static class ResultCollector<R,S,T> {
        private AOption<R> _1 = AOption.none ();
        private AOption<S> _2 = AOption.none ();
        private AOption<T> _3 = AOption.none ();

        synchronized boolean set1 (R o) {
            _1 = AOption.some (o);
            return _2.isDefined () && _3.isDefined ();
        }
        synchronized boolean set2 (S o) {
            _2 = AOption.some (o);
            return _1.isDefined () && _3.isDefined ();
        }
        synchronized boolean set3 (T o) {
            _3 = AOption.some (o);
            return _1.isDefined () && _2.isDefined ();
        }
    }
}
