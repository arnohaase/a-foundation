package com.ajjpj.abase.collection.graph;

import com.ajjpj.abase.collection.ACollectionHelper;
import com.ajjpj.abase.collection.immutable.AHashMap;
import com.ajjpj.abase.collection.immutable.AList;
import com.ajjpj.abase.collection.immutable.AMap;
import com.ajjpj.abase.function.APredicateNoThrow;

import java.io.Serializable;
import java.util.*;


//TODO javadoc
//TODO junit tests

/**
 * This class represents a directed graph - in the sense of a data structure, <em>not</em> of its visual representation. It is
 *  immutable, i.e. its nodes and edges are fixed on initialization. It is thread safe, i.e. it is safe to access concurrently
 *  from several threads.<p>
 *
 * A graph is represented as a collection of nodes and a second collection of edges. There is no requirement for the graph to be
 *  connected.
 *
 * @author arno
 */
public class ADiGraph<N,E extends AEdge<N>> implements Serializable {
    private final Object LOCK = new Object ();

    private final Object[] nodes;
    private final AEdge[] edges;

    /**
     * This is a convenience factory method that extracts the list of nodes from the edges. It assumes that every node has at least one
     *  edge going from or to it.
     */
    public static <N, E extends AEdge<N>> ADiGraph<N,E> create (Collection<E> edges) {
        final Set<N> result = new HashSet<> ();
        for (E edge: edges) {
            result.add (edge.getFrom ());
            result.add (edge.getTo ());
        }

        return create (result, edges);
    }

    public static <N, E extends AEdge<N>> ADiGraph<N,E> create (Collection<N> nodes, Collection<E> edges) {
        final Object[] nodeArr = new Object[nodes.size ()];
        final AEdge[] edgeArr = new AEdge[edges.size ()];

        int idx = 0;
        for (N node: nodes) {
            nodeArr[idx] = node;
            idx += 1;
        }

        idx = 0;
        for (E edge: edges) {
            edgeArr[idx] = edge;
            idx += 1;
        }

        return new ADiGraph<N,E> (nodeArr, edgeArr);
    }

    private ADiGraph (Object[] nodes, AEdge[] edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Collection<N> nodes () {
        return new ArrayIterable<> (nodes);
    }

    public Collection<E> edges () {
        return new ArrayIterable<> (edges);
    }

    //TODO helper: create list of edges from 'partial order'


    private transient AMap<N,AList<AEdgePath<N,E>>> _incomingPathsInternal;
    private transient AMap<N,AList<AEdgePath<N,E>>> _outgoingPathsInternal;
    private transient AList<AEdgePath<N,E>> _cyclesInternal;

    /**
     * This method does the reachability analysis in a way that is useful for many other methods.
     */
    private void initPathsInternal() {
        synchronized (LOCK) {
            if (_incomingPathsInternal == null) {
                AMap<N, AList<AEdgePath<N, E>>> incomingPaths = AHashMap.empty ();
                //noinspection unchecked
                incomingPaths = incomingPaths.withDefaultValue (AList.nil);

                AMap<N, AList<AEdgePath<N, E>>> outgoingPaths = AHashMap.empty ();
                //noinspection unchecked
                outgoingPaths = outgoingPaths.withDefaultValue (AList.nil);

                AList<AEdgePath<N, E>> cycles = AList.nil ();

                for (N curNode : nodes ()) { // iterate over nodes, treat 'curNode' as a target
                    final Iterable<E> curIncoming = incomingEdges (curNode);
                    List<AEdgePath<N, E>> unfinishedBusiness = new ArrayList<> ();
                    for (E incomingEdge : curIncoming) {
                        unfinishedBusiness.add (AEdgePath.create (incomingEdge));
                    }

                    AList<AEdgePath<N, E>> nonCycles = AList.nil ();

                    while (unfinishedBusiness.size () > 0) {
                        final List<AEdgePath<N, E>> curBusiness = unfinishedBusiness;

                        for (AEdgePath<N, E> p : unfinishedBusiness) {
                            if (!p.hasCycle () || p.isMinimalCycle ()) nonCycles = nonCycles.cons (p);
                            if (p.isMinimalCycle ()) cycles = cycles.cons (p);
                        }

                        unfinishedBusiness = new ArrayList<> ();

                        for (AEdgePath<N, E> curPath : curBusiness) {
                            final Iterable<E> l = incomingEdges (curPath.getFrom ());
                            for (E newEdge : l) {
                                final AEdgePath<N, E> pathCandidate = curPath.prepend (newEdge);
                                if (!pathCandidate.hasNonMinimalCycle ()) {
                                    unfinishedBusiness.add (pathCandidate);
                                }
                            }
                        }

                    }
                    incomingPaths = incomingPaths.updated (curNode, nonCycles);

                    for (AEdgePath<N, E> p : nonCycles) {
                        outgoingPaths = outgoingPaths.updated (p.getFrom (), outgoingPaths.getRequired (p.getFrom ()).cons (p));
                    }
                }
                _incomingPathsInternal = incomingPaths;
                _outgoingPathsInternal = outgoingPaths;
                _cyclesInternal = cycles;
            }
        }
    }

    private transient Map<N,List<E>> _incomingEdges = null;
    private Map<N,List<E>> incomingEdges() {
        if (_incomingEdges == null) {
            _incomingEdges = new HashMap<> ();

            for (E edge: edges ()) {
                List<E> edgeList = _incomingEdges.get (edge.getTo ());
                if (edgeList == null) {
                    edgeList = new ArrayList<> ();
                    _incomingEdges.put (edge.getTo (), edgeList);
                }
                edgeList.add (edge);
            }
        }

        return _incomingEdges;
    }

    public Collection<E> incomingEdges (N node) {
        final List<E> result = incomingEdges ().get (node);
        return result != null ? Collections.unmodifiableList (result) : Collections.<E>emptyList ();
    }

    private transient Map<N,List<E>> _outgoingEdges = null;
    private Map<N,List<E>> outgoingEdges() {
        if (_outgoingEdges == null) {
            _outgoingEdges = new HashMap<> ();

            for (E edge: edges ()) {
                List<E> edgeList = _outgoingEdges.get (edge.getFrom ());
                if (edgeList == null) {
                    edgeList = new ArrayList<> ();
                    _outgoingEdges.put (edge.getFrom (), edgeList);
                }
                edgeList.add (edge);
            }
        }

        return _outgoingEdges;
    }

    public Collection<E> outgoingEdges (N node) {
        final List<E> result = outgoingEdges ().get (node);
        return result != null ? Collections.unmodifiableList (result) : Collections.<E>emptyList ();
    }

    public Collection<AEdgePath<N,E>> outgoingPaths (N node) {
        initPathsInternal ();
        return _outgoingPathsInternal.getRequired (node).asJavaUtilList ();
    }
    public Collection<AEdgePath<N,E>> incomingPaths (N node) {
        initPathsInternal ();
        return _incomingPathsInternal.getRequired (node).asJavaUtilList ();
    }

    public boolean hasEdge (N from, N to) {
        for (E edge: incomingEdges (to)) {
            if (from.equals (edge.getFrom ())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPath (N from, N to) {
        for (AEdgePath<N,E> path: incomingPaths (to)) {
            if (from.equals (path.getFrom ())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return an Iterable containing all nodes, sorted in such a way that a node is guaranteed to come before all nodes that can be reached from it
     */
    public List<N> sortedNodesByReachability() throws ACircularityException {
        if (hasCycles()) {
            throw new ACircularityException ();
        }

        final Object[] result = new Object[nodes.length];
        int nextIdx = 0;

        final Set<N> unprocessed = new HashSet<> ();
        for (Object node: nodes) {
            //noinspection unchecked
            unprocessed.add ((N) node);
        }

        //TODO Map<N,Integer> with 'remaining' incoming edges, decrement when a node is 'processed' --> JMH

        while (! unprocessed.isEmpty ()) {
            final Set<N> nextBatch = ACollectionHelper.filter (unprocessed, new APredicateNoThrow<N> () {
                @Override public boolean apply (N n) {
                    for (E e: incomingEdges (n)) {
                        if (unprocessed.contains (e.getFrom ())) {
                            return false;
                        }
                    }
                    return true;
                }
            });

            unprocessed.removeAll (nextBatch);
            for (N n: nextBatch) {
                result[nextIdx] = n;
                nextIdx += 1;
            }
        }

        return new ArrayIterable<> (result);
    }

    public AList<AEdgePath<N,E>> minimalCycles () {
        initPathsInternal ();
        return _cyclesInternal;
    }

    public boolean hasCycles() {
        return minimalCycles ().nonEmpty ();
    }

    public boolean isAcyclic() {
        return minimalCycles ().isEmpty ();
    }

    public boolean isTree() {
        return isForest () && rootNodes ().size() == 1;
    }

    private Boolean _isForest;
    public boolean isForest() {
        synchronized (LOCK) {
            if (_isForest == null) {
                _isForest = !hasCycles () && ACollectionHelper.forAll (nodes (), new APredicateNoThrow<N> () {
                    @Override public boolean apply (N n) {
                        return incomingEdges (n).size () <= 1; // no node with more than one edge pointing to it
                    }
                });
            }
        }
        return _isForest;
    }

    private transient Collection<N> _rootNodes;
    /** nodes with no edge pointing to them */
    public Collection<N> rootNodes() {
        synchronized (LOCK) {
            if (_rootNodes == null) {
                _rootNodes = ACollectionHelper.filter (nodes (), new APredicateNoThrow<N> () {
                    @Override public boolean apply (N n) {
                        return incomingEdges (n).isEmpty ();
                    }
                });
            }
            return _rootNodes;
        }
    }

    private transient Collection<N> _leafNodes;
    /** nodes with no edge pointing from them */
    public Collection<N> leafNodes() {
        synchronized (this) {
            if (_leafNodes == null) {
                _leafNodes = ACollectionHelper.filter (nodes (), new APredicateNoThrow<N> () {
                    @Override public boolean apply (N n) {
                        return outgoingEdges (n).isEmpty ();
                    }
                });
            }
            return _leafNodes;
        }
    }

    private static class ArrayIterable<T> extends AbstractList<T> {
        private final Object[] data;

        ArrayIterable (Object[] data) {
            this.data = data;
        }

        @SuppressWarnings ("NullableProblems")
        @Override public Iterator<T> iterator () {
            return new ArrayIterator<> (data);
        }

        @SuppressWarnings ("unchecked")
        @Override public T get (int index) {
            return (T) data[index];
        }
        @Override public int size () {
            return data.length;
        }
    }

    private static class ArrayIterator<T> implements Iterator<T> {
        private final Object[] data;
        private int nextIdx = 0;

        public ArrayIterator (Object[] data) {
            this.data = data;
        }

        @Override public boolean hasNext () {
            return nextIdx < data.length;
        }
        @SuppressWarnings ("unchecked")
        @Override public T next () {
            final T result = (T) data[nextIdx];
            nextIdx += 1;
            return result;
        }
        @Override public void remove () {
            throw new UnsupportedOperationException ();
        }
    }
}



