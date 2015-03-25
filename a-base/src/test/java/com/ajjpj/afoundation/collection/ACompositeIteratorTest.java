package com.ajjpj.afoundation.collection;

import com.ajjpj.afoundation.collection.ACompositeIterator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Roman
 */
public class ACompositeIteratorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none ();

    @Test
    public void testHasNextPlusNext() {
        final ACompositeIterator<Integer> iter = new ACompositeIterator<> (Arrays.asList (
                Arrays.asList (1,2,3,4).iterator (),
                Arrays.asList (5,6,7,8).iterator (),
                Arrays.asList (9,10).iterator ()
        ));

        int expected = 1;
        while (expected <= 10) {
            assertTrue (iter.hasNext ());
            int elem = iter.next ();
            assertEquals (expected, elem);
            expected++;
        }
        assertFalse (iter.hasNext ());
        expectedException.expect (NoSuchElementException.class);
        iter.next ();
    }

    @Test
    public void testPureNext() {
        final ACompositeIterator<Integer> iter = new ACompositeIterator<> (Arrays.asList (
                Arrays.asList (1,2,3,4).iterator (),
                Arrays.asList (5,6,7,8).iterator (),
                Arrays.asList (9,10).iterator ()
        ));

        int expected = 1;
        while (expected <= 10) {
            int elem = iter.next ();
            assertEquals (expected, elem);
            expected++;
        }

        expectedException.expect (NoSuchElementException.class);
        iter.next ();
    }
}
