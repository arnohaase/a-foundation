package com.ajjpj.abase.collection;

/**
 * @author arno
 */
public class AEqualsWrapper<T> { //TODO javadoc, junit
    private final AEquality equality;
    public final T value;

    public AEqualsWrapper(AEquality equality, T value) {
        this.equality = equality;
        this.value = value;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override public boolean equals(Object obj) {
        return equality.equals(value, obj);
    }

    @Override public int hashCode() {
        return equality.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
