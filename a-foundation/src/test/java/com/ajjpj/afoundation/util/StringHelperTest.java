package com.ajjpj.afoundation.util;

import com.ajjpj.afoundation.util.StringHelper;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @author arno
 */
public class StringHelperTest {
    @Test
    public void testBytesToHexString() {
        assertEquals(null, StringHelper.bytesToHexString (null));
        assertEquals("", StringHelper.bytesToHexString(new byte[] {}));
        assertEquals("01020304", StringHelper.bytesToHexString(new byte[] {1, 2, 3, 4}));
        assertEquals("0E0F10", StringHelper.bytesToHexString(new byte[] {14, 15, 16}));
        assertEquals("80FF", StringHelper.bytesToHexString(new byte[] {-128, -1}));
    }

    @Test
    public void testHexStringToByteArray() {
        assertEquals(null, StringHelper.hexStringToByteArray(null));
        assertEq(new byte[]{}, StringHelper.hexStringToByteArray(""));
        assertEq(new byte[]{0, 1, 2}, StringHelper.hexStringToByteArray("000102"));
        assertEq(new byte[]{14, 15, 16}, StringHelper.hexStringToByteArray("0E0F10"));
        assertEq(new byte[]{-128, -1}, StringHelper.hexStringToByteArray("80FF"));

        assertEq(new byte[]{26}, StringHelper.hexStringToByteArray("1a"));
        assertEq(new byte[]{-2}, StringHelper.hexStringToByteArray("fE"));

        try {
            StringHelper.hexStringToByteArray("1");
            fail("exception expected");
        } catch (Exception e) {
            // expected
        }

        try {
            StringHelper.hexStringToByteArray("1X");
            fail("exception expected");
        } catch (Exception e) {
            // expected
        }

        try {
            StringHelper.hexStringToByteArray("X1");
            fail("exception expected");
        } catch (Exception e) {
            // expected
        }
    }
    private void assertEq(byte[] b1, byte[] b2) {
        assertEquals(b1.length, b2.length);

        for(int i=0; i<b1.length; i++) {
            assertEquals(b1[i], b2[i]);
        }
    }
}
