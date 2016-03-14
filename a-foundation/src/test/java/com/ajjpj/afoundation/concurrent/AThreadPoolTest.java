package com.ajjpj.afoundation.concurrent;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AThreadPoolTest {
    @Test
    public void testIsPrime() {
        assertTrue  (AThreadPoolImpl.isPrime (1));
        assertTrue  (AThreadPoolImpl.isPrime (2));
        assertTrue  (AThreadPoolImpl.isPrime (3));
        assertFalse (AThreadPoolImpl.isPrime (4));
        assertTrue  (AThreadPoolImpl.isPrime (5));
        assertFalse (AThreadPoolImpl.isPrime (6));
        assertTrue  (AThreadPoolImpl.isPrime (7));
        assertFalse (AThreadPoolImpl.isPrime (8));
        assertFalse (AThreadPoolImpl.isPrime (9));
    }

    @Test
    public void testPrime() {
        assertEquals (1,  AThreadPoolImpl.prime (0, Collections.emptySet ()));
        assertEquals (2,  AThreadPoolImpl.prime (1, Collections.emptySet ()));
        assertEquals (3,  AThreadPoolImpl.prime (2, Collections.emptySet ()));
        assertEquals (5,  AThreadPoolImpl.prime (3, Collections.emptySet ()));
        assertEquals (7,  AThreadPoolImpl.prime (4, Collections.emptySet ()));
        assertEquals (11, AThreadPoolImpl.prime (5, Collections.emptySet ()));
        assertEquals (13, AThreadPoolImpl.prime (6, Collections.emptySet ()));

        assertEquals (1, AThreadPoolImpl.prime (0, new HashSet<> (Arrays.asList (2, 5))));
        assertEquals (3, AThreadPoolImpl.prime (1, new HashSet<> (Arrays.asList (2, 5))));
        assertEquals (7, AThreadPoolImpl.prime (2, new HashSet<> (Arrays.asList (2, 5))));
        assertEquals (11, AThreadPoolImpl.prime (3, new HashSet<> (Arrays.asList (2, 5))));
        assertEquals (13, AThreadPoolImpl.prime (4, new HashSet<> (Arrays.asList (2, 5))));
        assertEquals (17, AThreadPoolImpl.prime (5, new HashSet<> (Arrays.asList (2, 5))));
        assertEquals (19, AThreadPoolImpl.prime (6, new HashSet<> (Arrays.asList (2, 5))));
    }

    @Test
    public void testPrimeFactors() {
        assertEquals (new HashSet<Integer>(Arrays.asList ()), AThreadPoolImpl.primeFactors (1));
        assertEquals (new HashSet<Integer>(Arrays.asList (2)), AThreadPoolImpl.primeFactors (2));
        assertEquals (new HashSet<Integer>(Arrays.asList (3)), AThreadPoolImpl.primeFactors (3));
        assertEquals (new HashSet<Integer>(Arrays.asList (2)), AThreadPoolImpl.primeFactors (4));
        assertEquals (new HashSet<Integer>(Arrays.asList (5)), AThreadPoolImpl.primeFactors (5));
        assertEquals (new HashSet<Integer>(Arrays.asList (2,3)), AThreadPoolImpl.primeFactors (6));
        assertEquals (new HashSet<Integer>(Arrays.asList (7)), AThreadPoolImpl.primeFactors (7));
        assertEquals (new HashSet<Integer>(Arrays.asList (2)), AThreadPoolImpl.primeFactors (8));
        assertEquals (new HashSet<Integer>(Arrays.asList (3)), AThreadPoolImpl.primeFactors (9));
        assertEquals (new HashSet<Integer>(Arrays.asList (2,5)), AThreadPoolImpl.primeFactors (10));
    }
}
