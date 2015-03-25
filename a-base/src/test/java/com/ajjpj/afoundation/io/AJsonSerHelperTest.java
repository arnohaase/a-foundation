package com.ajjpj.afoundation.io;

import com.ajjpj.afoundation.io.AJsonSerHelper;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AJsonSerHelperTest {
    @Test
    public void testNumber() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(12345, 0);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("12345", result);
    }

    @Test
    public void testNumberWithFrac1() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(12345, 1);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("1234.5", result);
    }

    @Test
    public void testNumberWithFrac9() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(1234567890, 9);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("1.234567890", result);
    }

    @Test
    public void testDoubleWithFrac0() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(1.23, 0);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("1", result);
    }

    @Test
    public void testDoubleWithFrac1() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(1.23, 1);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("1.2", result);
    }

    @Test
    public void testDoubleWithFrac9() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(1.23, 9);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("1.230000000", result);
    }

    @Test
    public void testNegativeNumber1() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(-1234567890, 1);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("-123456789.0", result);
    }

    @Test
    public void testNegativeNumber9() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(-1234567890, 9);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("-1.234567890", result);
    }

    @Test
    public void testNegativeNumberInArray() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startArray ();

        ser.writeNumberLiteral (-1, 0);
        ser.writeNumberLiteral (-2, 0);

        ser.endArray ();

        assertEquals ("[-1,-2]", new String (baos.toByteArray ()));
    }

    @Test
    public void testNegativeDouble() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNumberLiteral(-1.23, 9);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("-1.230000000", result);
    }

    @Test
    public void testNull() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeNullLiteral();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("null", result);
    }

    @Test
    public void testTrue() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeBooleanLiteral(true);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("true", result);
    }

    @Test
    public void testFalse() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeBooleanLiteral(false);

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("false", result);
    }

    @Test
    public void testString() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.writeStringLiteral("abcäöü\r\n\t \\\"{}[]");

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("\"abcäöü\\u000d\\u000a\\u0009 \\\\\\\"{}[]\"", result);
    }

    @Test
    public void testNullStringInArray() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startArray ();
        ser.writeStringLiteral ("a");
        ser.writeStringLiteral (null);
        ser.endArray ();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("[\"a\",null]", result);
    }

    @Test
    public void testArray0() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startArray();
        ser.endArray();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("[]", result);
    }

    @Test
    public void testArray1() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startArray();
        ser.writeBooleanLiteral(true);
        ser.endArray();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("[true]", result);
    }

    @Test
    public void testArray3() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startArray();
        ser.writeBooleanLiteral(true);
        ser.writeNumberLiteral(1, 0);
        ser.writeStringLiteral("asdf");
        ser.endArray();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("[true,1,\"asdf\"]", result);
    }

    @Test
    public void testObject0() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startObject();
        ser.endObject();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("{}", result);
    }

    @Test
    public void testObject1() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startObject();
        ser.writeKey("a");
        ser.writeBooleanLiteral(true);
        ser.endObject();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("{\"a\":true}", result);
    }

    @Test
    public void testObject3() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startObject();
        ser.writeKey("a");
        ser.writeBooleanLiteral(true);
        ser.writeKey("b");
        ser.writeNumberLiteral(1, 0);
        ser.writeKey("c");
        ser.writeStringLiteral("asdf");
        ser.endObject();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("{\"a\":true,\"b\":1,\"c\":\"asdf\"}", result);
    }

    @Test
    public void testComplex() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final AJsonSerHelper ser = new AJsonSerHelper(baos);

        ser.startObject();

        ser.writeKey("a");
        ser.writeNumberLiteral(1, 0);

        ser.writeKey("b");
        ser.startArray();
        ser.writeNumberLiteral(2, 0);
        ser.startObject();
        ser.writeKey("x");
        ser.writeNumberLiteral(3, 0);
        ser.endObject();
        ser.startArray();
        ser.writeNumberLiteral(4, 0);
        ser.endArray();
        ser.endArray();

        ser.writeKey("c");
        ser.startObject();
        ser.writeKey("r");
        ser.writeNumberLiteral(5, 0);
        ser.writeKey("s");
        ser.startObject();
        ser.writeKey("x");
        ser.writeNumberLiteral(6, 0);
        ser.endObject();
        ser.writeKey("t");
        ser.startArray();
        ser.writeNumberLiteral(7,0);
        ser.endArray();
        ser.endObject();

        ser.endObject();

        final String result = new String(baos.toByteArray(), "utf-8");
        assertEquals("{\"a\":1,\"b\":[2,{\"x\":3},[4]],\"c\":{\"r\":5,\"s\":{\"x\":6},\"t\":[7]}}", result);
    }

    @Test
    public void testNoWriteKeyInArray() throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(new ByteArrayOutputStream());
        ser.startArray();
        try {
            ser.writeKey("a");
            fail("exception expected");
        }
        catch(IllegalStateException exc) {/**/}
    }

    @Test
    public void testNoEndObjectInArray() throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(new ByteArrayOutputStream());
        ser.startArray();
        try {
            ser.endObject();
            fail("exception expected");
        }
        catch(IllegalStateException exc) {/**/}
    }

    @Test
    public void testNoValueInObject() throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(new ByteArrayOutputStream());
        ser.startObject();
        try {
            ser.writeBooleanLiteral(true);
            fail("exception expected");
        }
        catch(IllegalStateException exc) {/**/}
    }

    @Test
    public void testNoEndObjectAfterKey() throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(new ByteArrayOutputStream());
        ser.startObject();
        ser.writeKey("a");
        try {
            ser.endObject();
            fail("exception expected");
        }
        catch(IllegalStateException exc) {/**/}
    }

    @Test
    public void testNoEndArrayInObject() throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(new ByteArrayOutputStream());
        ser.startObject();
        try {
            ser.endArray();
            fail("exception expected");
        }
        catch(IllegalStateException exc) {/**/}
    }

    @Test
    public void testNoValueWhenFinishedObject() throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(new ByteArrayOutputStream());
        ser.startObject();
        ser.endObject();
        try {
            ser.writeBooleanLiteral(true);
            fail("exception expected");
        }
        catch(IllegalStateException exc) {/**/}
    }

    @Test
    public void testNoValueWhenFinishedArray() throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(new ByteArrayOutputStream());
        ser.startArray();
        ser.endArray();
        try {
            ser.writeBooleanLiteral(true);
            fail("exception expected");
        }
        catch(IllegalStateException exc) {/**/}
    }
}
