package com.ajjpj.abase.collection.immutable;

/**
 * @author arno
 */
class RemoveResult {
    final InMemoryBTree newNode;
    final boolean underflowed;
    final Object leftSeparator;

    public RemoveResult (InMemoryBTree newNode, boolean underflowed, Object leftSeparator) {
        this.newNode = newNode;
        this.underflowed = underflowed;
        this.leftSeparator = leftSeparator;
    }
}
