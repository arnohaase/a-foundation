package com.ajjpj.afoundation.collection.immutable;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;


/**
 * @author arno
 */
class LeafNode extends ABTreeMap {
    final Object[] keys;
    final Object[] values;

    LeafNode (BTreeSpec spec, Object[] keys, Object[] values) {
        super (spec);
        this.keys = keys;
        this.values = values;
    }

    @SuppressWarnings ("unchecked")
    private LookupResult lookupKey (Object key) {
        if (keys.length == 0) {
            return new LookupResult (LookupResult.REGULAR, 0);
        }

        LookupResult result = lookupKeyInInterval (key, 0, keys.length);
        if (result.index == keys.length) {
            result = new LookupResult (LookupResult.AFTER_LAST, result.index);
        }
        return result;
    }

    /**
     * @param toIdx the first index *after* the end of the search interval
     */
    @SuppressWarnings ("unchecked")
    private LookupResult lookupKeyInInterval (Object key, int fromIdx, int toIdx) {
        if (toIdx - fromIdx == 1) {
            final int comp = spec.comparator.compare (key, keys[fromIdx]);
            if (comp < 0) {
                return new LookupResult (LookupResult.REGULAR, fromIdx);
            }
            if (comp > 0) {
                return new LookupResult (LookupResult.REGULAR, toIdx);
            }
            return new LookupResult (LookupResult.MATCH, fromIdx);
        }
        else { //TODO optimization this implementation may perform the same comparison twice
            final int medianIdx = (toIdx + fromIdx) / 2;
            final int comp = spec.comparator.compare (key, keys[medianIdx]);
            if (comp < 0) {
                return lookupKeyInInterval (key, fromIdx, medianIdx);
            }
            if (comp > 0) {
                return lookupKeyInInterval (key, medianIdx, toIdx);
            }
            return new LookupResult (LookupResult.MATCH, medianIdx);
        }
    }

    @Override public AOption<Object> get (Object key) {
        final LookupResult lookupResult = lookupKey (key);
        if (lookupResult.kind == LookupResult.MATCH) {
            return AOption.some (values[lookupResult.index]);
        }
        return AOption.none ();
    }

    @Override UpdateResult merge (ABTreeMap rightNeighbour, Object separator) {
        final LeafNode rightLeaf = (LeafNode) rightNeighbour;

        if (keys.length + rightLeaf.keys.length <= spec.maxNumEntries) {
            final Object[] newKeys = new Object[keys.length + rightLeaf.keys.length];
            System.arraycopy (keys, 0, newKeys, 0, keys.length);
            System.arraycopy (rightLeaf.keys, 0, newKeys, keys.length, rightLeaf.keys.length);

            final Object[] newValues = new Object[keys.length + rightLeaf.keys.length];
            System.arraycopy (values, 0, newValues, 0, keys.length);
            System.arraycopy (rightLeaf.values, 0, newValues, keys.length, rightLeaf.keys.length);

            return new UpdateResult (new LeafNode (spec, newKeys, newValues), null, null);
        }
        else {
            final int len = keys.length + rightLeaf.keys.length;
            final int splitIndex = len / 2;

            if (splitIndex < keys.length) {
                final LeafNode left = new LeafNode (spec, Arrays.copyOf (keys, splitIndex), Arrays.copyOf (values, splitIndex));

                final Object[] rightKeys = new Object[len - splitIndex];
                System.arraycopy (keys, splitIndex, rightKeys, 0, keys.length - splitIndex);
                System.arraycopy (rightLeaf.keys, 0, rightKeys, keys.length - splitIndex, rightLeaf.keys.length);

                final Object[] rightValues = new Object[len - splitIndex];
                System.arraycopy (values, splitIndex, rightValues, 0, keys.length - splitIndex);
                System.arraycopy (rightLeaf.values, 0, rightValues, keys.length - splitIndex, rightLeaf.keys.length);

                return new UpdateResult (left, rightKeys[0], new LeafNode (spec, rightKeys, rightValues));
            }
            else {
                final Object[] leftKeys = new Object[splitIndex];
                System.arraycopy (keys, 0, leftKeys, 0, keys.length);
                System.arraycopy (rightLeaf.keys, 0, leftKeys, keys.length, splitIndex - keys.length);

                final Object[] leftValues = new Object[splitIndex];
                System.arraycopy (values, 0, leftValues, 0, keys.length);
                System.arraycopy (rightLeaf.values, 0, leftValues, keys.length, splitIndex - keys.length);

                final Object[] rightKeys   = Arrays.copyOfRange (rightLeaf.keys, rightLeaf.keys.length - (len - splitIndex), rightLeaf.keys.length);
                final Object[]     rightValues = Arrays.copyOfRange (rightLeaf.values, rightLeaf.keys.length - (len - splitIndex), rightLeaf.keys.length);

                final LeafNode right = new LeafNode (spec, rightKeys, rightValues);
                return new UpdateResult (new LeafNode (spec, leftKeys, leftValues), right.keys[0], right);
            }
        }
    }

    @Override RemoveResult _removed (Object key, Object leftSeparator) {
        final LookupResult lookupResult = lookupKey (key);

        if (lookupResult.kind != LookupResult.MATCH) {
            return new RemoveResult (this, false, keys[0]);
        }

        if (keys.length == 1) {
            return new RemoveResult (empty (spec), false, null);
        }

        final Object[] newKeys = new Object[keys.length-1];
        System.arraycopy (keys, 0, newKeys, 0, lookupResult.index);
        System.arraycopy (keys, lookupResult.index + 1, newKeys, lookupResult.index, keys.length - lookupResult.index - 1);

        final Object[] newValues = new Object[values.length-1];
        System.arraycopy (values, 0, newValues, 0, lookupResult.index);
        System.arraycopy (values, lookupResult.index + 1, newValues, lookupResult.index, keys.length - lookupResult.index - 1);

        return new RemoveResult (new LeafNode (spec, newKeys, newValues), newKeys.length < spec.minNumEntries, newKeys[0]);
    }

    @Override UpdateResult _updated (Object key, Object value) {
        final LookupResult lookupResult = lookupKey (key);
        final int len = keys.length;
        switch (lookupResult.kind) {
            case LookupResult.MATCH: {
                final Object[] newValues = Arrays.copyOf (values, len);
                newValues[lookupResult.index] = value;
                return new UpdateResult (new LeafNode (spec, keys, newValues), null, null);
            }
            case LookupResult.AFTER_LAST:
                if (len < spec.maxNumEntries) {
                    final Object[] newKeys = Arrays.copyOf (keys, len + 1);
                    final Object[] newValues = Arrays.copyOf (values, len + 1);
                    newKeys[len] = key;
                    newValues[len] = value;
                    return new UpdateResult (new LeafNode (spec, newKeys, newValues), null, null);
                }
                else {
                    final int idxMedian = spec.maxNumEntries / 2;
                    final LeafNode left = new LeafNode (spec, Arrays.copyOf (keys, idxMedian), Arrays.copyOf (values, idxMedian));
                    final Object[] newKeys   = new Object[spec.maxNumEntries - idxMedian + 1];
                    final Object[]     newValues = new Object[spec.maxNumEntries - idxMedian + 1];
                    System.arraycopy (keys, idxMedian, newKeys, 0, spec.maxNumEntries - idxMedian);
                    System.arraycopy (values, idxMedian, newValues, 0, spec.maxNumEntries - idxMedian);
                    newKeys  [spec.maxNumEntries - idxMedian] = key;
                    newValues[spec.maxNumEntries - idxMedian] = value;
                    return new UpdateResult (left, newKeys[0], new LeafNode (spec, newKeys, newValues));
                }
            case LookupResult.REGULAR:
                if (len < spec.maxNumEntries) {
                    final Object[] newKeys   = new Object[len+1];
                    final Object[]     newValues = new Object[len+1];
                    System.arraycopy (keys, 0, newKeys, 0, lookupResult.index);
                    System.arraycopy (values, 0, newValues, 0, lookupResult.index);
                    System.arraycopy (keys, lookupResult.index, newKeys, lookupResult.index + 1, len - lookupResult.index);
                    System.arraycopy (values, lookupResult.index, newValues, lookupResult.index + 1, len - lookupResult.index);
                    newKeys  [lookupResult.index] = key;
                    newValues[lookupResult.index] = value;
                    return new UpdateResult (new LeafNode (spec, newKeys, newValues), null, null);
                }
                else {
                    final Object[] newKeysLeft  = splitAndAddLeft  (Object.class, keys, lookupResult.index, key);
                    final Object[] newKeysRight = splitAndAddRight (Object.class, keys, lookupResult.index, key);

                    final Object[] newValuesLeft  = splitAndAddLeft  (Object.class, values, lookupResult.index, value);
                    final Object[] newValuesRight = splitAndAddRight (Object.class, values, lookupResult.index, value);

                    return new UpdateResult (new LeafNode (spec, newKeysLeft, newValuesLeft), newKeysRight[0], new LeafNode (spec, newKeysRight, newValuesRight));
                }
        }
        throw new IllegalStateException ("unknown node state: " + lookupResult.kind);
    }

    @SuppressWarnings ("unchecked")
    private <T> T[] splitAndAddLeft (Class<T> componentType, T[] orig, int newIdx, T newEl) {
        final int idxMedian = orig.length / 2;
        if (newIdx > idxMedian) {
            return Arrays.copyOf (orig, idxMedian);
        }

        final T[] result = (T[]) Array.newInstance (componentType, idxMedian + 1);
        System.arraycopy (orig, 0, result, 0, newIdx);
        result[newIdx] = newEl;
        System.arraycopy (orig, newIdx, result, newIdx + 1, idxMedian - newIdx);
        return result;
    }

    @SuppressWarnings ("unchecked")
    private <T> T[] splitAndAddRight (Class<T> componentType, T[] orig, int newIdx, T newEl) {
        final int idxMedian = orig.length / 2;
        if (newIdx <= idxMedian) {
            return Arrays.copyOfRange (orig, idxMedian, orig.length);
        }
        final int lenRight = orig.length - idxMedian + 1;
        final int rightIdx = newIdx - idxMedian;

        final T[] result = (T[]) Array.newInstance (componentType, lenRight);
        System.arraycopy (orig, idxMedian, result, 0, rightIdx);
        result[rightIdx] = newEl;

        System.arraycopy (orig, newIdx, result, rightIdx + 1, lenRight - rightIdx - 1);
        return result;
    }

    @Override public int size () {
        return keys.length;
    }
    @Override public boolean isEmpty () {
        return size () == 0;
    }

    @Override public Set keys () {
        return new AbstractSet () {
            @Override public boolean contains (Object o) {
                return LeafNode.this.containsKey (o);
            }
            @Override public Iterator iterator () {
                return Arrays.asList (keys).iterator ();
            }
            @Override public int size () {
                return LeafNode.this.size ();
            }
        };
    }
}
