package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AStatement2NoThrow;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author arno
 */
public class ACircuitBreakerTest {
    final AThreadPool threadPool = new AThreadPoolBuilder ().buildFixedSize (1);

    @After
    public void tearDown() {
        threadPool.shutdown ();
    }

    @Test
    public void testException() throws InterruptedException, ExecutionException {
        final ATaskScheduler circuitBreaker = new ACircuitBreaker (threadPool, 3, 100, TimeUnit.MILLISECONDS);

        // get the circuit breaker to break the circuit
        for (int i=0; i<4; i++) {
            try {
                circuitBreaker.submit (new Callable<Object> () {
                    @Override public Object call () throws Exception {
                        System.out.println ("initial failure");
                        throw new RuntimeException ();
                    }
                }, 1, TimeUnit.SECONDS).get ();
                fail ("exception expected");
            }
            catch (ExecutionException e) {
                // expected
            }
        }

        // add lots of seriously expensive operations. Since the circuit breaker broke the circuit, these should
        //  'fail' immediately because they are rejected.
        for (int i=0; i<100; i++) {
            try {
                circuitBreaker.submit (new Callable<Object> () {
                    @Override public Object call () throws Exception {
                        Thread.sleep (1000);
                        return null;
                    }
                }, 10, TimeUnit.SECONDS).get ();
                fail ("exception expected");
            }
            catch (ExecutionException e) {
                assertEquals (TimeoutException.class, e.getCause ().getClass ());
            }
        }

        try {
            // verify that the circuit breaker is still in 'rejecting' mode
            circuitBreaker.submit (new Callable<Object> () {
                @Override public Object call () throws Exception {
                    return "rejected";
                }
            }, 10, TimeUnit.SECONDS).get ();
        }
        catch (ExecutionException e) {
            assertEquals (TimeoutException.class, e.getCause ().getClass ());
        }

        Thread.sleep (150);

        //TODO test single test failed
        //TODO test rejection during single test

        // circuit breaker should now let a single task pass
        assertEquals ("success", circuitBreaker.submit (new Callable<Object> () {
            @Override public Object call () throws Exception {
                return "success";
            }
        }, 10, TimeUnit.MILLISECONDS).get ());
    }
}
