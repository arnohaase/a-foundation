package com.ajjpj.abase.collection;

/**
 * @author arno
 */
public class APair<T1,T2> {
    public final T1 _1;
    public final T2 _2;

    public APair(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    @Override
    public String toString() {
        return "APair{" +
                "_1=" + _1 +
                ", _2=" + _2 +
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final APair aPair = (APair) o;

        if (_1 != null ? !_1.equals(aPair._1) : aPair._1 != null) return false;
        if (_2 != null ? !_2.equals(aPair._2) : aPair._2 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _1 != null ? _1.hashCode() : 0;
        result = 31 * result + (_2 != null ? _2.hashCode() : 0);
        return result;
    }
}
