package com.ajjpj.abase.collection.graph;

import java.io.Serializable;


/**
 * @author arno
 */
public class ASimpleEdge<N> implements AEdge<N>, Serializable {
    private final N from;
    private final N to;

    public ASimpleEdge (N from, N to) {
        this.from = from;
        this.to = to;
    }

    @Override public N getFrom () {
        return from;
    }
    @Override public N getTo () {
        return to;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        ASimpleEdge that = (ASimpleEdge) o;

        if (from != null ? !from.equals (that.from) : that.from != null) return false;
        if (to != null ? !to.equals (that.to) : that.to != null) return false;

        return true;
    }
    @Override
    public int hashCode () {
        int result = from != null ? from.hashCode () : 0;
        result = 31 * result + (to != null ? to.hashCode () : 0);
        return result;
    }

    @Override public String toString () {
        return "ASimpleEdge{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
