package com.ajjpj.abase.collection.immutable;

/**
 * @author arno
 */
class UpdateResult {
    final InMemoryBTree left;
    final Object separator;
    final InMemoryBTree optRight;

    public UpdateResult (InMemoryBTree left, Object separator, InMemoryBTree optRight) {
        this.left = left;
        this.separator = separator;
        this.optRight = optRight;
    }
}
