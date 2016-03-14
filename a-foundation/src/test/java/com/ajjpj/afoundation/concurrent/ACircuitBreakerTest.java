package com.ajjpj.afoundation.concurrent;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class ACircuitBreakerTest {
//    final AThreadPool threadPool = new AThreadPoolBuilder ().buildFixedSize (1);
//
//    @After
//    public void tearDown() {
//        threadPool.shutdown ();
//    }
//
//    @Test
//    public void testException() throws InterruptedException, ExecutionException {
//        final ATaskScheduler circuitBreaker = new ACircuitBreaker (threadPool, 3, 100, TimeUnit.MILLISECONDS);
//
//        // get the circuit breaker to break the circuit
//        for (int i=0; i<4; i++) {
//            try {
//                circuitBreaker.submit (new Callable<Object> () {
//                    @Override public Object call () throws Exception {
//                        throw new RuntimeException ();
//                    }
//                }, 1, TimeUnit.SECONDS).get ();
//                fail ("exception expected");
//            }
//            catch (ExecutionException e) {
//                // expected
//            }
//        }
//
//        // add lots of seriously expensive operations. Since the circuit breaker broke the circuit, these should
//        //  'fail' immediately because they are rejected.
//        for (int i=0; i<100; i++) {
//            try {
//                circuitBreaker.submit (new Callable<Object> () {
//                    @Override public Object call () throws Exception {
//                        Thread.sleep (1000);
//                        return null;
//                    }
//                }, 10, TimeUnit.SECONDS).get ();
//                fail ("exception expected");
//            }
//            catch (ExecutionException exc) {
//                assertEquals (RejectedByCircuitBreakerException.class, exc.getCause ().getClass ());
//                assertTrue (exc.getCause () instanceof TimeoutException);
//            }
//        }
//
//        // verify that the circuit breaker is still in 'rejecting' mode
//        try {
//            circuitBreaker.submit (new Callable<Object> () {
//                @Override public Object call () throws Exception {
//                    return "rejected";
//                }
//            }, 10, TimeUnit.SECONDS).get ();
//        }
//        catch (ExecutionException exc) {
//            assertEquals (RejectedByCircuitBreakerException.class, exc.getCause ().getClass ());
//            assertTrue (exc.getCause () instanceof TimeoutException);
//        }
//
//        // we wait until the circuit breaker is open to accept a *single* submission
//        Thread.sleep (150);
//
//        // we submit a task. It will actually be scheduled by the circuit breaker, but it will time out
//        final AFuture<Object> f = circuitBreaker.submit (new Callable<Object> () {
//            @Override public Object call () throws Exception {
//                Thread.sleep (100);
//                return null;
//            }
//        }, 10, TimeUnit.MILLISECONDS);
//
//        // the circuit breaker remains closed for other tasks while waiting for the single 'experiment' to finish
//        try {
//            circuitBreaker.submit (new Callable<Object> () {
//                @Override public Object call () throws Exception {
//                    return null;
//                }
//            }, 1, TimeUnit.SECONDS).get ();
//        }
//        catch (ExecutionException exc) {
//            assertEquals (RejectedByCircuitBreakerException.class, exc.getCause ().getClass ());
//            assertTrue (exc.getCause () instanceof TimeoutException);
//        }
//
//        // we wait for the 'experiment' task to actually time out
//        try {
//            f.get ();
//        }
//        catch (ExecutionException exc) {
//            assertEquals (TimeoutException.class, exc.getCause ().getClass ());
//        }
//
//        // we give the circuit breaker time to open again
//        Thread.sleep (150);
//
//        // circuit breaker should now let a single task pass
//        assertEquals ("success", circuitBreaker.submit (new Callable<Object> () {
//            @Override public Object call () throws Exception {
//                return "success";
//            }
//        }, 10, TimeUnit.MILLISECONDS).get ());
//
//        //... and subsequent tasks as well
//        assertEquals ("success", circuitBreaker.submit (new Callable<Object> () {
//            @Override public Object call () throws Exception {
//                return "success";
//            }
//        }, 10, TimeUnit.MILLISECONDS).get ());
//        assertEquals ("success", circuitBreaker.submit (new Callable<Object> () {
//            @Override public Object call () throws Exception {
//                return "success";
//            }
//        }, 10, TimeUnit.MILLISECONDS).get ());
//        assertEquals ("success", circuitBreaker.submit (new Callable<Object> () {
//            @Override public Object call () throws Exception {
//                return "success";
//            }
//        }, 10, TimeUnit.MILLISECONDS).get ());
//    }
}
