package com.ajjpj.afoundation.collection.immutable;

/**
 * @author arno
 */
class RemoveResult {
    final ABTree newNode;
    final boolean underflowed;
    final Object leftSeparator;

    public RemoveResult (ABTree newNode, boolean underflowed, Object leftSeparator) {
        this.newNode = newNode;
        this.underflowed = underflowed;
        this.leftSeparator = leftSeparator;
    }
}
