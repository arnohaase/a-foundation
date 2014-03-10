package com.ajjpj.abase.collection;

/**
 * @author arno
 */
public interface AEquality {
    boolean equals(Object o1, Object o2);
    int hashCode(Object o);

    AEquality EQUALS = new Equals();
    AEquality IDENTITY = new Identity();

    class Equals implements AEquality {
        @Override
        public boolean equals(Object o1, Object o2) {
            if (o1 == null) {
                return o2 == null;
            }
            else {
                return o1.equals(o2);
            }
        }

        @Override
        public int hashCode(Object o) {
            return o != null ? o.hashCode() : 0;
        }
    }

    class Identity implements AEquality {
        @Override
        public boolean equals(Object o1, Object o2) {
            return o1 == o2;
        }

        @Override
        public int hashCode(Object o) {
            return System.identityHashCode(o);
        }
    }
}
