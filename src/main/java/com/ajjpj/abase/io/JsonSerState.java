package com.ajjpj.abase.io;

/**
 * @author arno
 */
enum JsonSerState {
    initial(false, true), startOfObject(true, false), inObject(true, false), afterKey(false, true), startOfArray(false, true), inArray(false, true), finished(false, false);

    public final boolean acceptsKey;
    public final boolean acceptsValue;

    JsonSerState(boolean acceptsKey, boolean acceptsValue) {
        this.acceptsKey = acceptsKey;
        this.acceptsValue = acceptsValue;
    }
}
