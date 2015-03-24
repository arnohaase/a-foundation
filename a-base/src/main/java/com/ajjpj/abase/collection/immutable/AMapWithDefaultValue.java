package com.ajjpj.abase.collection.immutable;

/**
 * @author arno
 */
class AMapWithDefaultValue<K,V> extends AWrappedMap<K,V> {
    private final V defaultValue;

    AMapWithDefaultValue(AMap<K, V> inner, V defaultValue) {
        super(inner);
        this.defaultValue = defaultValue;
    }

    @Override AMap<K, V> wrap(AMap<K, V> inner) {
        return new AMapWithDefaultValue<>(inner, defaultValue);
    }

    @Override V defaultValue(K key) {
        return defaultValue;
    }
}
