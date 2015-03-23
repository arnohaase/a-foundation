package com.ajjpj.abase.collection.graph;

import com.ajjpj.abase.collection.immutable.AHashSet;
import com.ajjpj.abase.collection.immutable.AList;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class ADiGraphTest {

    @Test
    public void testSimple() throws Exception {
        final ADiGraph<String, ASimpleEdge<String>> graph = ADiGraph.create (Arrays.asList (edge ("b", "c"), edge ("a", "b")));

        assertTrue (eqSet (graph.edges (), edge ("b", "c"), edge ("a", "b")));

        assertTrue (graph.minimalCycles ().isEmpty ());
        assertFalse (graph.hasCycles ());

        assertTrue (graph.hasEdge ("a", "b"));
        assertTrue (graph.hasEdge ("b", "c"));
        assertFalse (graph.hasEdge ("a", "c"));
        assertFalse (graph.hasEdge ("b", "a"));
        assertFalse (graph.hasEdge ("a", "d"));
        assertFalse (graph.hasEdge ("x", "a"));

        assertTrue (graph.hasPath ("a", "b"));
        assertTrue (graph.hasPath ("b", "c"));
        assertTrue (graph.hasPath ("a", "c"));
        assertFalse (graph.hasPath ("b", "a"));
        assertFalse (graph.hasPath ("a", "d"));
        assertFalse (graph.hasPath ("x", "a"));

        assertTrue (graph.incomingEdges ("a").isEmpty ());
        assertTrue (eqSet (graph.incomingEdges ("b"), edge ("a", "b")));
        assertTrue (eqSet (graph.incomingEdges ("c"), edge ("b", "c")));

        assertTrue (graph.incomingPaths ("a").isEmpty ());
        assertTrue (eqSet (graph.incomingPaths ("b"), path ("a", "b")));
        assertTrue (eqSet (graph.incomingPaths ("c"), path ("a", "b", "c"), path ("b", "c")));

        assertTrue (eqSet (graph.outgoingEdges ("a"), edge ("a", "b")));
        assertTrue (eqSet (graph.outgoingEdges ("b"), edge ("b", "c")));
        assertTrue (graph.outgoingEdges ("c").isEmpty ());

        assertTrue (eqSet (graph.outgoingPaths ("a"), path ("a", "b"), path ("a", "b", "c")));
        assertTrue (eqSet (graph.outgoingPaths ("b"), path ("b", "c")));
        assertTrue (graph.outgoingPaths ("c").isEmpty ());

        assertTrue (graph.isAcyclic ());
        assertTrue (graph.isForest ());
        assertTrue (graph.isTree ());

        assertTrue (eqSet (graph.leafNodes (), "c"));
        assertTrue (eqSet (graph.nodes (), "a", "b", "c"));
        assertTrue (eqSet (graph.rootNodes (), "a"));

        assertEquals (Arrays.asList ("a", "b", "c"), graph.sortedNodesByReachability ());
    }

    //TODO testCycle
    //TODO testTree
    //TODO testForest
    //TODO test acyclic non-tree

    //TODO test inverse

    @Test
    public void testEmpty() {
        fail ("TODO");
    }

    private static ASimpleEdge<String> edge (String from, String to) {
        return new ASimpleEdge<> (from, to);
    }

    private static AEdgePath<String, ASimpleEdge<String>> path (String first, String... others) {
        final List<ASimpleEdge<String>> edges = new ArrayList<> ();
        AHashSet<String> nodeSet = AHashSet.create (first);

        String from, to = first;
        for (String target: others) {
            nodeSet = nodeSet.added (target);
            from = to;
            to = target;
            edges.add (edge (from, to));
        }

        return new AEdgePath<> (AList.create (edges), nodeSet, to);
    }

    private <T> boolean eqSet (Iterable<T> coll1, T... values) {
        return eqSet (coll1, Arrays.asList (values));
    }

    private <T> boolean eqSet (Iterable<T> coll1, Iterable<T> coll2) {
        final Set<T> set1 = new HashSet<> ();
        final Set<T> set2 = new HashSet<> ();

        for (T o: coll1) {
            set1.add (o);
        }

        for (T o: coll2) {
            set2.add (o);
        }

        return set1.equals (set2);
    }

}
