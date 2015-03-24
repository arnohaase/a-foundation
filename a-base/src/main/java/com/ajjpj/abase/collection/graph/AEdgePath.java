package com.ajjpj.abase.collection.graph;

import com.ajjpj.abase.collection.immutable.AHashSet;
import com.ajjpj.abase.collection.immutable.AList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * This class represents a path in a graph, including the actual edge objects it consists of
 *
 * @author arno
 */
public class AEdgePath<N, E extends AEdge<N>> implements Serializable {
    private final AList<E> edges;
    private final AHashSet<N> nodes;
    private final N to;

    @SuppressWarnings ("unchecked")
    public static <N, E extends AEdge<N>> AEdgePath<N,E> create (E edge) {
        return new AEdgePath (AList.create (edge), AHashSet.create (edge.getFrom (), edge.getTo ()), edge.getTo ());
    }

    AEdgePath (AList<E> edges, AHashSet<N> nodes, N to) {
        this.edges = edges;
        this.nodes = nodes;
        this.to = to;
    }

    AEdgePath<N,E> prepend (E edge) {
        return new AEdgePath<> (edges.cons (edge), nodes.added (edge.getFrom ()), to);
    }

    @SuppressWarnings ("unchecked")
    public List<E> getEdges() {
        return (List) Collections.unmodifiableList (Arrays.asList (edges));
    }

    public int size() {
        return edges.size ();
    }

    public N getFrom() {
        return edges.head ().getFrom ();
    }

    public N getTo() {
        return to;
    }

    public boolean hasCycle() {
        return nodes.size () != size ()+1;
    }

    public boolean isMinimalCycle() {
        return getFrom () == getTo () && nodes.size () == size ();
    }

    public boolean hasNonMinimalCycle() {
        return hasCycle () && !isMinimalCycle ();
    }

    @Override public String toString () {
        return "AEdgePath{" +
                "edges=" + edges +
                ", nodes=" + nodes +
                ", to=" + to +
                '}';
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        AEdgePath aEdgePath = (AEdgePath) o;

        return edges.equals (aEdgePath.edges);

    }
    @Override
    public int hashCode () {
        return edges.hashCode ();
    }
}
