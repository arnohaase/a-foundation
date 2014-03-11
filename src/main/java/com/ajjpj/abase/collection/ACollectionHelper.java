package com.ajjpj.abase.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author arno
 */
public class ACollectionHelper {
    public static String mkString(Iterable<?> iterable) {
        return mkString(iterable, ", ");
    }

    public static String mkString(Iterable<?> iterable, String separator) {
        return mkString(iterable, "", separator, "");
    }

    public static String mkString(Iterable<?> iterable, String prefix, String separator, String suffix) {
        final StringBuilder result = new StringBuilder(prefix);

        boolean first = true;
        for(Object o: iterable) {
            if(first) {
                first = false;
            }
            else {
                result.append(separator);
            }
            result.append(o);
        }

        result.append(suffix);
        return result.toString();
    }

    public static <T> Collection<T> asCollection(final Iterable<T> c) {
        return asCollection(c.iterator());
    }

    public static <T> Collection<T> asCollection(Iterator<T> c) {
        final List<T> result = new ArrayList<>();

        while(c.hasNext()) {
            result.add(c.next());
        }

        return result;
    }
}
