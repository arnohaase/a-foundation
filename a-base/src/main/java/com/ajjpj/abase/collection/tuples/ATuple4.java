package com.ajjpj.abase.collection.tuples;


import java.io.Serializable;


/**
 * @author arno
 */
public class ATuple4<T1,T2,T3,T4> implements Serializable {
    public final T1 _1;
    public final T2 _2;
    public final T3 _3;
    public final T4 _4;

    public ATuple4 (T1 _1, T2 _2, T3 _3, T4 _4) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
    }

    @Override public String toString () {
        return "ATuple4{" +
                "_1=" + _1 +
                ", _2=" + _2 +
                ", _3=" + _3 +
                ", _4=" + _4 +
                '}';
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        ATuple4 aTuple4 = (ATuple4) o;

        if (_1 != null ? !_1.equals (aTuple4._1) : aTuple4._1 != null) return false;
        if (_2 != null ? !_2.equals (aTuple4._2) : aTuple4._2 != null) return false;
        if (_3 != null ? !_3.equals (aTuple4._3) : aTuple4._3 != null) return false;
        if (_4 != null ? !_4.equals (aTuple4._4) : aTuple4._4 != null) return false;

        return true;
    }
    @Override
    public int hashCode () {
        int result = _1 != null ? _1.hashCode () : 0;
        result = 31 * result + (_2 != null ? _2.hashCode () : 0);
        result = 31 * result + (_3 != null ? _3.hashCode () : 0);
        result = 31 * result + (_4 != null ? _4.hashCode () : 0);
        return result;
    }
}
