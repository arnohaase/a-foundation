package com.ajjpj.afoundation.collection.immutable;

import java.util.Arrays;


/**
 * @author arno
 */
public class ALongHashSet extends MapAsSetWrapper<Long, ALongHashSet> {
    private static final ALongHashSet EMPTY = new ALongHashSet (ALongHashMap.empty ());

    public static ALongHashSet empty () {
        return EMPTY;
    }

    public static ALongHashSet create (long... elements) {
        ALongHashSet result = empty ();

        for (long el: elements) {
            result = result.added (el); //TODO specialize this
        }
        return result;
    }

    public static ALongHashSet create (Long... elements) {
        return create (Arrays.asList (elements));
    }

    public static ALongHashSet create (Iterable<Long> elements) {
        ALongHashSet result = empty ();

        for (Long el: elements) {
            result = result.added (el);
        }
        return result;
    }

    public static ALongHashSet create (ALongHashMap<?> inner) {
        return new ALongHashSet (inner);
    }


    public ALongHashSet (AMap<Long, ?> inner) {
        super (inner);
    }

    @Override protected ALongHashSet wrapAsSet (AMap<Long, ?> inner) {
        return new ALongHashSet (inner);
    }


}
