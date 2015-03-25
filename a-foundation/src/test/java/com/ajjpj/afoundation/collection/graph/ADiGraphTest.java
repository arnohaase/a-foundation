package com.ajjpj.afoundation.collection.graph;

import com.ajjpj.afoundation.collection.graph.ADiGraph;
import com.ajjpj.afoundation.collection.graph.AEdgePath;
import com.ajjpj.afoundation.collection.graph.AGraphCircularityException;
import com.ajjpj.afoundation.collection.graph.ASimpleEdge;
import com.ajjpj.afoundation.collection.immutable.AHashSet;
import com.ajjpj.afoundation.collection.immutable.AList;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


/**
 * @author arno
 */
@SuppressWarnings ("unchecked")
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

        // test robustness against non-existing nodes
        assertTrue (graph.incomingEdges ("X").isEmpty ());
        assertTrue (graph.incomingPaths ("X").isEmpty ());
        assertTrue (graph.outgoingEdges ("X").isEmpty ());
        assertTrue (graph.outgoingPaths ("X").isEmpty ());

        assertTrue (graph.isAcyclic ());
        assertTrue (graph.isForest ());
        assertTrue (graph.isTree ());

        assertTrue (eqSet (graph.leafNodes (), "c"));
        assertTrue (eqSet (graph.nodes (), "a", "b", "c"));
        assertTrue (eqSet (graph.rootNodes (), "a"));

        assertEquals (Arrays.asList ("a", "b", "c"), graph.sortedNodesByReachability ());
    }

    @Test
    public void testCycle() {
        final ADiGraph<String, ASimpleEdge<String>> graph = ADiGraph.create (Arrays.asList (edge ("a", "b"), edge ("b", "a")));

        assertTrue (eqSet (graph.edges (), edge ("a", "b"), edge ("b", "a")));

        assertTrue (graph.hasCycles ());
        assertTrue (eqSet (graph.minimalCycles (), path ("a", "b", "a"), path ("b", "a", "b")));

        assertTrue (graph.hasEdge ("a", "b"));
        assertTrue (graph.hasEdge ("b", "a"));

        assertTrue (graph.hasPath ("a", "b"));
        assertTrue (graph.hasPath ("b", "a"));
        assertTrue (graph.hasPath ("a", "a"));
        assertTrue (graph.hasPath ("b", "b"));

        assertTrue (eqSet (graph.incomingEdges ("a"), edge ("b", "a")));
        assertTrue (eqSet (graph.incomingEdges ("b"), edge ("a", "b")));

        assertTrue (eqSet (graph.incomingPaths ("a"), path("b", "a"), path ("a", "b", "a")));
        assertTrue (eqSet (graph.incomingPaths ("b"), path("a", "b"), path ("b", "a", "b")));

        assertTrue (eqSet (graph.outgoingEdges ("a"), edge ("a", "b")));
        assertTrue (eqSet (graph.outgoingEdges ("b"), edge ("b", "a")));

        assertTrue (eqSet (graph.outgoingPaths ("a"), path ("a", "b"), path ("a", "b", "a")));
        assertTrue (eqSet (graph.outgoingPaths ("b"), path ("b", "a"), path ("b", "a", "b")));

        assertFalse (graph.isAcyclic ());
        assertFalse (graph.isForest ());
        assertFalse (graph.isTree ());

        assertTrue (graph.leafNodes ().isEmpty ());
        assertTrue (eqSet (graph.nodes (), "a", "b"));
        assertTrue (graph.rootNodes ().isEmpty ());

        try {
            graph.sortedNodesByReachability ();
            fail ("exception expected");
        }
        catch (AGraphCircularityException e) {
            // expected
        }
    }

    @Test
    public void testTree() {
        final ADiGraph<String, ASimpleEdge<String>> graph = ADiGraph.create (Arrays.asList (edge ("a", "b"), edge ("a", "c")));

        assertFalse (graph.hasCycles ());
        assertTrue (graph.isAcyclic ());
        assertTrue (graph.isForest ());
        assertTrue (graph.isTree ());

        assertTrue (eqSet (graph.leafNodes (), "b", "c"));
        assertTrue (eqSet (graph.rootNodes (), "a"));

        assertEquals ("a", graph.sortedNodesByReachability ().get (0));
    }

    @Test
    public void testForest() {
        final ADiGraph<String, ASimpleEdge<String>> graph = ADiGraph.create (Arrays.asList (edge ("a", "b"), edge ("a", "c"), edge ("x", "y")));

        assertFalse (graph.hasCycles ());
        assertTrue (graph.isAcyclic ());
        assertTrue (graph.isForest ());
        assertFalse (graph.isTree ());

        assertTrue (eqSet (graph.leafNodes (), "b", "c", "y"));
        assertTrue (eqSet (graph.rootNodes (), "a", "x"));
    }

    @Test
    public void testAcyclicNonTree() {
        final ADiGraph<String, ASimpleEdge<String>> graph = ADiGraph.create (Arrays.asList (edge ("a", "b"), edge ("a", "c"), edge ("b", "d"), edge ("c", "d")));

        assertFalse (graph.hasCycles ());
        assertTrue (graph.isAcyclic ());
        assertFalse (graph.isForest ());
        assertFalse (graph.isTree ());

        assertTrue (eqSet (graph.leafNodes (), "d"));
        assertTrue (eqSet (graph.rootNodes (), "a"));

        assertEquals ("a", graph.sortedNodesByReachability ().get (0));
        assertEquals ("d", graph.sortedNodesByReachability ().get (3));
    }

    @Test
    public void testNodesWithoutEdges() {
        final ADiGraph<String, ASimpleEdge<String>> graph = ADiGraph.create (Arrays.asList ("a", "b", "c"), Arrays.asList (edge ("a", "b")));

        assertTrue (eqSet (graph.edges (), edge ("a", "b")));

        assertFalse (graph.hasCycles ());
        assertTrue (graph.minimalCycles ().isEmpty ());

        assertTrue (graph.isAcyclic ());
        assertTrue (graph.isForest ());
        assertFalse (graph.isTree ());

        assertTrue (eqSet (graph.leafNodes (), "b", "c"));
        assertTrue (eqSet (graph.nodes (), "a", "b", "c"));
        assertTrue (eqSet (graph.rootNodes (), "a", "c"));

        assertEquals ("b", graph.sortedNodesByReachability ().get (2));
    }

    @Test
    public void testEmpty() {
        final ADiGraph<String, ASimpleEdge<String>> graph = ADiGraph.create (Collections.<ASimpleEdge<String>>emptyList ());

        assertTrue (graph.edges ().isEmpty ());

        assertTrue (graph.minimalCycles ().isEmpty ());
        assertFalse (graph.hasCycles ());

        assertFalse (graph.hasEdge ("a", "b"));

        assertFalse (graph.hasPath ("a", "b"));

        assertTrue (graph.incomingEdges ("a").isEmpty ());
        assertTrue (graph.incomingPaths ("a").isEmpty ());

        assertTrue (graph.outgoingEdges ("a").isEmpty ());
        assertTrue (graph.outgoingPaths ("a").isEmpty ());

        assertTrue (graph.isAcyclic ());
        assertTrue (graph.isForest ());
        assertFalse (graph.isTree ());

        assertTrue (graph.leafNodes ().isEmpty ());
        assertTrue (graph.nodes ().isEmpty ());
        assertTrue (graph.rootNodes ().isEmpty ());

        assertTrue (graph.sortedNodesByReachability ().isEmpty ());
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
