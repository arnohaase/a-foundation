package com.ajjpj.abase.collection;


/**
 * This interface represents a strategy for handling equalityForEquals, typically between elements of a collection. It contains
 *  both <code>equals()</code> and <code>hashCode</code> methods because in general collections rely on both, and these
 *  two methods must be consistent with each other.<p>
 *
 * There are two typical strategies that cover the vast majority of cases. They are readily available as constants in
 *  the interface:
 * <ul>
 *     <li>EQUALS: Delegates to <code>Object.equals()</code> and <code>Object.hashCode()</code> of the elements in
 *                 question. This is the default behavior of standard library collections.</li>
 *     <li>IDENTITY: Uses same-ness '==' to decide equalityForEquals, and <code>System.identityHashCode()</code> for hash code
 *                   calculation. This is the behavior exhibited by <code>IdentityHashMap</code>.</li>
 * </ul>
 *
 * While these two implementations are probably sufficient for most contexts, other strategies are possible (e.g. based
 *  on database primary keys).
 *
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
