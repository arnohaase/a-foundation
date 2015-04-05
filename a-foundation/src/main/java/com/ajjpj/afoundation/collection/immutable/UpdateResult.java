package com.ajjpj.afoundation.collection.immutable;

/**
 * @author arno
 */
class UpdateResult {
    final ABTreeMap left;
    final Object separator;
    final ABTreeMap optRight;

    public UpdateResult (ABTreeMap left, Object separator, ABTreeMap optRight) {
        this.left = left;
        this.separator = separator;
        this.optRight = optRight;
    }
}
