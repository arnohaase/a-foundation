package com.ajjpj.abase.util;

/**
 * @author arno
 */
public class StringHelper {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHexString(byte[] bytes) {
        if(bytes == null) {
            return null;
        }

        final char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        if(s == null) {
            return null;
        }

        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            final int hiNibble = Character.digit(s.charAt(i),   16);
            final int loNibble = Character.digit(s.charAt(i+1), 16);

            if(hiNibble < 0 || loNibble < 0) {
                throw new IllegalArgumentException("no valid hex number at offset " + i);
            }

            data[i / 2] = (byte) ((hiNibble << 4) + loNibble);
        }
        return data;
    }

}
