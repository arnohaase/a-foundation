package com.ajjpj.afoundation.collection.immutable;

/**
 * @author arno
 */
class LookupResult {
    public static final int MATCH = 0;
    public static final int REGULAR = 1; // 'index' is the index *after* the position at which the key belongs
    public static final int AFTER_LAST = 2;

    final int kind;
    final int index;

    LookupResult (int kind, int index) {
        this.kind = kind;
        this.index = index;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        LookupResult that = (LookupResult) o;

        if (index != that.index) return false;
        if (kind != that.kind) return false;

        return true;
    }
    @Override
    public int hashCode () {
        int result = kind;
        result = 31 * result + index;
        return result;
    }

    @Override public String toString () {
        return "LookupResult{kind=" + kind + ", index=" + index + '}';
    }
}
