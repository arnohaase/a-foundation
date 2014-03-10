package com.ajjpj.abase.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AUUIDTest {
    @Test
    public void testAll() {
        final AUUID uuid = AUUID.createRandom();
        final String s = uuid.toString();
        final AUUID fromString = AUUID.fromString(s);

        assertEquals(uuid, fromString);

        assertTrue(Arrays.equals(uuid.getData(), fromString.getData()));
    }
}
