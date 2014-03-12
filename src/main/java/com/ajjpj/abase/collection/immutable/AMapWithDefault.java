package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.function.AFunction1NoThrow;

/**
 * @author arno
 */
class AMapWithDefault<K,V> extends AWrappedMap<K,V> {
    private final AFunction1NoThrow<V,K> defaultFunction;

    AMapWithDefault(AMap<K, V> inner, AFunction1NoThrow<V,K> defaultFunction) {
        super(inner);
        this.defaultFunction = defaultFunction;
    }

    @Override AMap<K, V> wrap(AMap<K, V> inner) {
        return new AMapWithDefault<>(inner, defaultFunction);
    }

    @Override V defaultValue(K key) {
        return defaultFunction.apply(key);
    }
}
