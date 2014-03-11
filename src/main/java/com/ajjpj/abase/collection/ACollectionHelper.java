package com.ajjpj.abase.collection;

/**
 * @author arno
 */
public class ACollectionHelper {
    public String mkString(Iterable<?> iterable) {
        return mkString(iterable, ", ");
    }

    public String mkString(Iterable<?> iterable, String separator) {
        return mkString(iterable, "", separator, "");
    }

    public String mkString(Iterable<?> iterable, String prefix, String separator, String suffix) {
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
}
