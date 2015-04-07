package com.ajjpj.afoundation.collection;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;


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

    /**
     * Comparison by object identity. This equality strategy is intentionally <em>not</em> serializable since identity
     *  of elements in a collection is bound to be lost during serialization.
     */
    AEquality IDENTITY = new Identity();

    /**
     * This AEquality implementation requires objects to be {@link Comparable} and implements
     *  equality based on the result of {@link Comparable#compareTo(Object)}. Hashing is
     *  <em>not</em> supported.
     */
    AEquality NATURAL_ORDER = new NaturalOrder ();

    class Equals implements AEquality, Serializable {
        private Object readResolve() {
            return EQUALS;
        }

        private Equals () {
        }

        @Override
        public boolean equals(Object o1, Object o2) {
            if (o1 == null) {
                return o2 == null;
            }
            else {
                return o1.equals(o2);
            }
        }

        @Override public int hashCode(Object o) {
            return o != null ? o.hashCode() : 0;
        }
    }

    class Identity implements AEquality, Serializable {
        private Object readResolve () {
            return IDENTITY;
        }

        private Identity() {
        }

        @Override public boolean equals(Object o1, Object o2) {
            return o1 == o2;
        }
        @Override public int hashCode(Object o) {
            return System.identityHashCode(o);
        }
    }

    class NaturalOrder implements AEquality, Serializable {
        private NaturalOrder() {}

        private Object readResolve() {
            return NATURAL_ORDER;
        }

        @SuppressWarnings ("unchecked")
        @Override public boolean equals (Object o1, Object o2) {
            return ((Comparable) o1).compareTo (o2) == 0;
        }
        @Override public int hashCode (Object o) {
            return Objects.hashCode (o);
        }

        @Override public boolean equals (Object obj) {
            return getClass () == obj.getClass ();
        }
    }

    class ComparatorBased implements AEquality, Serializable {
        private final Comparator comparator;

        public ComparatorBased (Comparator comparator) {
            this.comparator = comparator;
        }

        @SuppressWarnings ("unchecked")
        @Override public boolean equals (Object o1, Object o2) {
            return comparator.compare (o1, o2) == 0;
        }
        @Override public int hashCode (Object o) {
            return Objects.hashCode (o);
        }

        @Override public boolean equals (Object obj) {
            //noinspection SimplifiableIfStatement
            if (getClass () != obj.getClass ()) return false;
            return Objects.equals (comparator, ((ComparatorBased) obj).comparator);
        }
    }
}
