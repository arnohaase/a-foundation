package com.ajjpj.abase.collection.immutable;

/**
 * @author arno
 */
class UpdateResult {
    final ABTree left;
    final Object separator;
    final ABTree optRight;

    public UpdateResult (ABTree left, Object separator, ABTree optRight) {
        this.left = left;
        this.separator = separator;
        this.optRight = optRight;
    }
}
