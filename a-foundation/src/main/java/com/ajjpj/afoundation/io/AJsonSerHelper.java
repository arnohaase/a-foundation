package com.ajjpj.afoundation.io;

import com.ajjpj.afoundation.function.AStatement1;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;


/**
 * This class is a collection of helper methods for manually writing JSON to an OutputStream. <p>
 * <p>
 * For details on the JSON spec, see http://json.org
 *
 * @author arno
 */
public class AJsonSerHelper {
    public static final Charset UTF_8 = Charset.forName ("UTF-8");

    private static final int[] TEN_POW = new int[] {1, 10, 100, 1_000, 10_000, 100_000, 1000_000, 10_000_000, 100_000_000, 1_000_000_000};
    private static final String[] PATTERNS = new String[] {"0", "0.0", "0.00", "0.000", "0.0000", "0.00000", "0.000000", "0.0000000", "0.00000000", "0.000000000"};
    private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols (Locale.US);

    protected final Writer out;
    private final Deque<JsonSerState> state = new ArrayDeque<> ();

    public AJsonSerHelper (OutputStream out) {
        this.out = new OutputStreamWriter (out, UTF_8);
        state.push (JsonSerState.initial);
    }

    public void startObject () throws IOException {
        checkAcceptsValueAndPrefixComma ();
        state.push (JsonSerState.startOfObject);
        out.write ("{");
    }

    public void endObject () throws IOException {
        checkInObject ();
        state.pop ();
        out.write ("}");
        afterValueWritten ();
    }

    public void writeKey (String key) throws IOException {
        if (!state ().acceptsKey) {
            throw new IllegalStateException ("state " + state () + " does not accept a key");
        }
        if (state () == JsonSerState.inObject) {
            writeCommaInObject ();
        }
        _writeStringLiteral (key);
        writeColonAfterKey ();
        state.push (JsonSerState.afterKey);
    }

    public void startArray () throws IOException {
        checkAcceptsValueAndPrefixComma ();
        state.push (JsonSerState.startOfArray);
        out.write ("[");
    }

    public void endArray () throws IOException {
        checkInArray ();
        state.pop ();
        out.write ("]");
        afterValueWritten ();
    }

    public void writeStringLiteral (String s) throws IOException {
        if (s == null) {
            writeNullLiteral ();
            return;
        }

        checkAcceptsValueAndPrefixComma ();
        _writeStringLiteral (s);
        afterValueWritten ();
    }

    private void _writeStringLiteral (String s) throws IOException {
        out.write ('"');
        for (int i = 0; i < s.length (); i++) {
            final char ch = s.charAt (i);

            if (ch == '"') {
                out.write ("\\\"");
            }
            else if (ch == '\\') {
                out.write ("\\\\");
            }
            else if (ch < 16) {
                out.write ("\\u000" + Integer.toHexString (ch));
            }
            else if (ch < 32) {
                out.write ("\\u00" + Integer.toHexString (ch));
            }
            else {
                out.write (ch);
            }
        }

        out.write ('"');
    }

    public void writeNumberLiteral (long value, int numFracDigits) throws IOException {
        checkAcceptsValueAndPrefixComma ();

        if (value < 0) {
            out.write ('-');
            value = -value;
        }

        if (numFracDigits == 0) {
            out.write (String.valueOf (value));
        }
        else {
            final long intPart = value / TEN_POW[numFracDigits];
            final String fracPart = String.valueOf (1_000_000_000 + value % TEN_POW[numFracDigits]).substring (10 - numFracDigits, 10);

            out.write (String.valueOf (intPart));
            out.write (".");
            out.write (fracPart);
        }

        afterValueWritten ();
    }

    public void writeNumberLiteral (double value, int numFracDigits) throws IOException {
        checkAcceptsValueAndPrefixComma ();
        out.write (new DecimalFormat (PATTERNS[numFracDigits], DECIMAL_FORMAT_SYMBOLS).format (value));
        afterValueWritten ();
    }

    public void writeBooleanLiteral (boolean value) throws IOException {
        checkAcceptsValueAndPrefixComma ();
        out.write (String.valueOf (value));
        afterValueWritten ();
    }

    public void writeNullLiteral () throws IOException {
        checkAcceptsValueAndPrefixComma ();
        out.write ("null");
        afterValueWritten ();
    }

    //----------------------------------------- convenience API

    public void writeIntLiteral (int value) throws IOException {
        writeNumberLiteral (value, 0);
    }
    public void writeLongLiteral (long value) throws IOException {
        writeNumberLiteral (value, 0);
    }

    public void writeStringArray (Iterable<String> values) throws IOException {
        startArray ();
        for (String el: values) writeStringLiteral (el);
        endArray ();
    }

    public void writeStringField (String key, String value) throws IOException {
        writeKey (key);
        writeStringLiteral (value);
    }
    public void writeBooleanField (String key, Boolean value) throws IOException {
        writeKey (key);
        if (value == null) writeNullLiteral ();
        else writeBooleanLiteral (value);
    }
    public void writeIntField (String key, Integer value) throws IOException {
        writeKey (key);
        if (value == null) writeNullLiteral ();
        else writeIntLiteral (value);
    }
    public void writeLongField (String key, Long value) throws IOException {
        writeKey (key);
        if (value == null) writeNullLiteral ();
        else writeLongLiteral (value);
    }

    /**
     * This is a convenience method for building simple JSON strings. It passes an AJsonSerHelper to a callback and
     *  builds a string based on what the callback does with it.
     */
    public static <E extends Throwable> String buildString (AStatement1<AJsonSerHelper, E> code) throws E {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        code.apply (new AJsonSerHelper (baos));
        return new String (baos.toByteArray (), UTF_8);
    }

    //----------------------------------------- helper methods

    protected void writeCommaInObject () throws IOException {
        out.write (",");
    }

    protected void writeCommaInArray () throws IOException {
        out.write (",");
    }

    protected void writeColonAfterKey () throws IOException {
        out.write (":");
    }

    protected void checkAcceptsValueAndPrefixComma () throws IOException {
        if (!state ().acceptsValue) {
            throw new IllegalStateException ("state " + state () + " does not accept a value");
        }
        if (state () == JsonSerState.inArray) {
            writeCommaInArray ();
        }
    }

    protected void afterValueWritten () throws IOException {
        if (state () == JsonSerState.afterKey) {
            state.pop ();
        }
        switch (state ()) {
            case startOfArray:
                replaceState (JsonSerState.inArray);
                break;
            case startOfObject:
                replaceState (JsonSerState.inObject);
                break;
            case initial:
                replaceState (JsonSerState.finished);
                out.flush ();
                break;
            default:
        }
    }

    private void checkInObject () {
        if (state () != JsonSerState.inObject && state () != JsonSerState.startOfObject) {
            throw new IllegalStateException ("not in an object");
        }
    }

    private void checkInArray () {
        if (state () != JsonSerState.inArray && state () != JsonSerState.startOfArray) {
            throw new IllegalStateException ("not in an array");
        }
    }

    private void replaceState (JsonSerState newState) {
        state.pop ();
        state.push (newState);
    }

    protected JsonSerState state () {
        return state.peek ();
    }
}

