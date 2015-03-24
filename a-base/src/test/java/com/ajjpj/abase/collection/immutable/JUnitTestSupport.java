package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.AEquality;


/**
 * @author arno
 */
public class JUnitTestSupport {
    public static AEquality equality (AHashMap map) {
        return map.equality;
    }

    public static AEquality equality (AListMap map) {
        return map.equality;
    }

    public static AEquality equality (AHashSet set) {
        return set.equalityForEquals ();
    }
}
