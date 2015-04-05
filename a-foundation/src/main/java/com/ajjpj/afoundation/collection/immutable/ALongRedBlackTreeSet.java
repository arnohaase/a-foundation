package com.ajjpj.afoundation.collection.immutable;

import java.util.Arrays;


/**
 * @author arno
 */
public class ALongRedBlackTreeSet extends MapAsSetWrapper<Long, ALongRedBlackTreeSet> {
    public static ALongRedBlackTreeSet empty () {
        return new ALongRedBlackTreeSet (ALongRedBlackTreeMap.<Boolean> empty ());
    }

    public static ALongRedBlackTreeSet create (long... elements) {
        ALongRedBlackTreeSet result = empty ();

        for (long el: elements) {
            result = result.added (el); //TODO specialize this
        }
        return result;
    }

    public static ALongRedBlackTreeSet create (Long... elements) {
        return create (Arrays.asList (elements));
    }

    public static ALongRedBlackTreeSet create (Iterable<Long> elements) {
        ALongRedBlackTreeSet result = empty ();

        for (Long el: elements) {
            result = result.added (el);
        }
        return result;
    }

    public static ALongRedBlackTreeSet create (ALongRedBlackTreeMap<?> inner) {
        return new ALongRedBlackTreeSet (inner);
    }

    //TODO ASortedMap, ASortedSet


    private ALongRedBlackTreeSet (AMap<Long, ?> inner) {
        super (inner);
    }

    @Override protected ALongRedBlackTreeSet wrapAsSet (AMap<Long, ?> inner) {
        return new ALongRedBlackTreeSet (inner);
    }
}
