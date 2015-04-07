package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.function.AFunction1;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


/**
 * @author arno
 */
abstract class AbstractAMap<K,V> implements AMap<K,V> {
    transient private Integer cachedHashcode = null; // intentionally not volatile: This class is immutable, so recalculating per thread works

    @Override public boolean isEmpty () {
        return size () == 0;
    }
    @Override public boolean nonEmpty () {
        return size () != 0;
    }

    @Override public V getRequired (K key) {
        return get (key).get ();
    }

    @Override public boolean containsKey (K key) {
        return get (key).isDefined ();
    }

    @Override public ACollection<V> values () {
        return new MapValueCollection<> (this);
    }
    @Override public boolean containsValue (V value) {
        return values ().contains (value);
    }

    @Override public Map<K, V> asJavaUtilMap () {
        return new JavaUtilMapWrapper<> (this);
    }
    @Override public AMap<K, V> withDefaultValue (V defaultValue) {
        return new AMapWithDefaultValue<> (this, defaultValue);
    }
    @Override public AMap<K, V> withDefault (AFunction1<? super K, ? extends V, ? extends RuntimeException> function) {
        return new AMapWithDefault<> (this, function);
    }


    @SuppressWarnings ("unchecked")
    @Override public boolean equals (Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || ! (obj instanceof AMap)) {
            return false;
        }

        final AMap<K,V> other = (AMap<K,V>) obj;
        if (size () != other.size ()) {
            return false;
        }

        for (AMapEntry<K,V> e: this) {
            final AOption<V> o = other.get (e.getKey ());
            if (o.isEmpty () || ! Objects.equals (e.getValue (), o.get ())) return false;
        }

        return true;
    }

    @Override public int hashCode () {
        if(cachedHashcode == null) {
            int result = 0;

            for(AMapEntry<K,V> el: this) {
                result = result ^ (31*keyEquality ().hashCode (el.getKey ()) + Objects.hashCode(el.getValue ()));
            }

            cachedHashcode = result;
        }

        return cachedHashcode;
    }

    @Override public String toString () {
        final StringBuilder result = new StringBuilder ("{");

        boolean first = true;
        for (AMapEntry<K,V> e: this) {
            if (first) first = false;
            else result.append (", ");

            result.append (e.getKey ()).append ("->").append (e.getValue ());
        }

        result.append ("}");
        return result.toString ();
    }
}
