package com.ajjpj.abase.collection;


/**
 * This class wraps arbitrary objects, providing customizable <code>equals()</code> and <code>hashCode()</code> behavior based
 *  on an <code>AEquality</code> instance. Calls to <code>equals()</code> or <code>hashCode()</code> on the wrapper are delegated
 *  to the corresponding calls on the <code>AEquality</code> instance. <p />
 *
 * This is a rather low-level class that is primarily useful for infrstructure code. If you do not understand its usefulness, you
 *  probably do not need it.
 *
 * @author arno
 */
public class AEqualsWrapper<T> { //TODO junit
    private final AEquality equality;
    public final T value;

    public AEqualsWrapper(AEquality equality, T value) {
        this.equality = equality;
        this.value = value;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override public boolean equals(Object obj) {
        return equality.equals(value, ((AEqualsWrapper)obj).value);
    }

    @Override public int hashCode() {
        return equality.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
