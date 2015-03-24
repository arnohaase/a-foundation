package com.ajjpj.abase.collection.graph;


/**
 * This is the minimal abstraction for an edge in a directed graph: An edge points from one node to another node.
 *
 * @author arno
 */
public interface AEdge<N> {
    N getFrom();
    N getTo();
}
