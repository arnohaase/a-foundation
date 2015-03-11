package com.ajjpj.abase.collection;

import com.ajjpj.abase.collection.immutable.*;
import com.ajjpj.abase.collection.tuples.ATuple2;
import com.ajjpj.abase.function.AFunction1NoThrow;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class SerializableTest implements Serializable {
    @Test
    public void testListSerializable() throws IOException, ClassNotFoundException {
        checkSerSame (AList.nil ());
        checkSerEq (AList.create (1, 2, 3));
    }

    @Test
    public void testOptionSerializable() throws IOException, ClassNotFoundException {
        checkSerSame (AOption.none ());
        checkSerEq (AOption.some (1));
        checkSerEq (AOption.some ("abc"));
    }

    @Test
    public void testHashSetSerializable() throws IOException, ClassNotFoundException {
        checkSerSame (AHashSet.empty ());
        assertEquals (MyEquality.INSTANCE, JUnitTestSupport.equality (serDeser (AHashSet.empty (MyEquality.INSTANCE))));

        checkSerEq (AHashSet.create (1, 2, 3, 4, 5));
        checkSerEq (AHashSet.create ("a", "b", "c", "d", "e"));
    }

    static class MyEquality implements AEquality, Serializable {
        static final MyEquality INSTANCE = new MyEquality ();

        private Object readResolve () {
            return INSTANCE;
        }

        @Override public boolean equals (Object o1, Object o2) {
            return Objects.equals (o1, o2);
        }
        @Override public int hashCode (Object o) {
            return Objects.hashCode (o);
        }
    }

    @Test
    public void testHashMapSerializable() throws IOException, ClassNotFoundException {
        checkSerSame (AHashMap.empty ());
        assertEquals (MyEquality.INSTANCE, JUnitTestSupport.equality (serDeser (AHashMap.empty (MyEquality.INSTANCE))));

        checkSerEq (AHashMap.fromKeysAndValues (Arrays.asList (1, 2, 3, 4, 5), Arrays.asList ("a", "b", "c", "d", "e")));
    }

    @Test
    public void testListMapSerializable() throws IOException, ClassNotFoundException {
        checkSerEq (AListMap.empty ());
        assertEquals (MyEquality.INSTANCE, JUnitTestSupport.equality (serDeser (AListMap.empty (MyEquality.INSTANCE))));

        checkSerEq (AListMap.fromKeysAndValues (Arrays.asList (1, 2, 3, 4, 5), Arrays.asList ("a", "b", "c", "d", "e")));
    }

    @Test
    public void testMapWithDefaultSerializable() throws IOException, ClassNotFoundException {
        final AFunction1NoThrow<Integer, String> fun = new AFunction1NoThrow<Integer, String> () {
            @Override public String apply (Integer param) {
                return String.valueOf (param);
            }
        };

        final AMap<Integer, String> map = AHashMap
                .fromKeysAndValues (Arrays.asList (1, 2, 3, 4, 5), Arrays.asList ("a", "b", "c", "d", "e"))
                .withDefault (fun);
        final AMap<Integer, String> ser = serDeser (map);

        assertEquals (map, ser);
        assertEquals ("a", ser.getRequired (1));
        assertEquals ("0", ser.getRequired (0));
    }

    @Test
    public void testMapWithDefaultValueSerializable() throws IOException, ClassNotFoundException {
        final AMap<Integer, String> map = AHashMap
                .fromKeysAndValues (Arrays.asList (1, 2, 3, 4, 5), Arrays.asList ("a", "b", "c", "d", "e"))
                .withDefaultValue ("?");
        final AMap<Integer, String> ser = serDeser (map);

        assertEquals (map, ser);
        assertEquals ("a", ser.getRequired (1));
        assertEquals ("?", ser.getRequired (0));
    }

    @Test
    public void testPairSerializable() throws IOException, ClassNotFoundException {
        checkSerEq (new ATuple2<> (1, "a"));
        checkSerEq (new ATuple2<> (2, "b"));
    }

    @Test
    public void testEqualsWrapperSerializable() throws IOException, ClassNotFoundException {
        assertEquals (AEquality.EQUALS, serDeser (new AEqualsWrapper<> (AEquality.EQUALS, 1)).equality);
        assertEquals (Integer.valueOf (1), serDeser (new AEqualsWrapper<> (AEquality.EQUALS, 1)).value);
        assertEquals (Integer.valueOf (2), serDeser (new AEqualsWrapper<> (AEquality.EQUALS, 2)).value);
    }

    @Test
    public void testEqualitySerializable() throws IOException, ClassNotFoundException {
        assertSame (AEquality.EQUALS, serDeser (AEquality.EQUALS));

        try {
            serDeser (AEquality.IDENTITY);
        }
        catch (NotSerializableException exc) {}
    }

    private void checkSerSame (Object o) throws IOException, ClassNotFoundException {
        assertSame (o, serDeser (o));
    }

    private void checkSerEq (Object o) throws IOException, ClassNotFoundException {
        assertEquals (o, serDeser (o));
        assertNotSame (o, serDeser (o));
    }

    @SuppressWarnings ("unchecked")
    private <T> T serDeser (T o) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();

        try (ObjectOutputStream oos = new ObjectOutputStream (baos)) {
            oos.writeObject (o);
        }

        try (ObjectInputStream ois = new ObjectInputStream (new ByteArrayInputStream (baos.toByteArray ()))) {
            return (T) ois.readObject ();
        }
    }
}
