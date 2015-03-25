package com.ajjpj.afoundation.collection.immutable;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class BTreeLeafRemoveTest extends AbstractBTreeTest {
    @Test
    public void testSimpleRemoveNotFound() {
        final LeafNode leaf = leaf (1, 2, 4, 5);

        final RemoveResult result = leaf._removed (3, 1);

        final LeafNode newLeaf = (LeafNode) result.newNode;
        assertArrayEquals (new Integer[] {1, 2, 4, 5}, newLeaf.keys);
        assertArrayEquals (new Integer[] {100, 200, 400, 500}, newLeaf.values);

        assertEquals (false, result.underflowed);
        assertEquals (1, result.leftSeparator);
    }

    @Test
    public void testSimpleRemoveMiddle() {
        final LeafNode leaf = leaf (1, 2, 3, 4, 5);

        final RemoveResult result = leaf._removed (3, 1);

        final LeafNode newLeaf = (LeafNode) result.newNode;
        assertArrayEquals (new Integer[] {1, 2, 4, 5}, newLeaf.keys);
        assertArrayEquals (new Integer[] {100, 200, 400, 500}, newLeaf.values);

        assertEquals (false, result.underflowed);
        assertEquals (1, result.leftSeparator);
    }

    @Test
    public void testSimpleRemoveBeginning() {
        final LeafNode leaf = leaf (1, 2, 3, 4, 5);

        final RemoveResult result = leaf._removed (1, 1);

        final LeafNode newLeaf = (LeafNode) result.newNode;
        assertArrayEquals (new Integer[] {2, 3, 4, 5}, newLeaf.keys);
        assertArrayEquals (new Integer[] {200, 300, 400, 500}, newLeaf.values);

        assertEquals (false, result.underflowed);
        assertEquals (2, result.leftSeparator);
    }

    @Test
    public void testSimpleRemoveEnd() {
        final LeafNode leaf = leaf (1, 2, 3, 4, 5);

        final RemoveResult result = leaf._removed (5, 1);

        final LeafNode newLeaf = (LeafNode) result.newNode;
        assertArrayEquals (new Integer[] {1, 2, 3, 4}, newLeaf.keys);
        assertArrayEquals (new Integer[] {100, 200, 300, 400}, newLeaf.values);

        assertEquals (false, result.underflowed);
        assertEquals (1, result.leftSeparator);
    }

    @Test
    public void testUnderflowRemoveMiddle() {
        final LeafNode leaf = leaf (1, 2, 3, 4);

        final RemoveResult result = leaf._removed (3, 1);

        final LeafNode newLeaf = (LeafNode) result.newNode;
        assertArrayEquals (new Integer[] {1, 2, 4}, newLeaf.keys);
        assertArrayEquals (new Integer[] {100, 200, 400}, newLeaf.values);

        assertEquals (true, result.underflowed);
        assertEquals (1, result.leftSeparator);
    }

    @Test
    public void testUnderflowRemoveBeginning() {
        final LeafNode leaf = leaf (1, 2, 3, 4);

        final RemoveResult result = leaf._removed (1, 1);

        final LeafNode newLeaf = (LeafNode) result.newNode;
        assertArrayEquals (new Integer[] {2, 3, 4}, newLeaf.keys);
        assertArrayEquals (new Integer[] {200, 300, 400}, newLeaf.values);

        assertEquals (true, result.underflowed);
        assertEquals (2, result.leftSeparator);
    }

    @Test
    public void testUnderflowRemoveEnd() {
        final LeafNode leaf = leaf (1, 2, 3, 4);

        final RemoveResult result = leaf._removed (4, 1);

        final LeafNode newLeaf = (LeafNode) result.newNode;
        assertArrayEquals (new Integer[] {1, 2, 3}, newLeaf.keys);
        assertArrayEquals (new Integer[] {100, 200, 300}, newLeaf.values);

        assertEquals (true, result.underflowed);
        assertEquals (1, result.leftSeparator);
    }

    @Test
    public void testMergeSimple() {
        final UpdateResult result = leaf (1, 2, 3).merge (leaf (5, 6, 7), 5);

        final LeafNode merged = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {1, 2, 3, 5, 6, 7}, merged.keys);
        assertArrayEquals (new Integer[] {100, 200, 300, 500, 600, 700}, merged.values);

        assertNull (result.separator);
        assertNull (result.optRight);
    }

    @Test
    public void testMergeExactFit() {
        final UpdateResult result = leaf (1, 2, 3, 4).merge (leaf (5, 6, 7, 8), 5);

        final LeafNode merged = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {1, 2, 3, 4, 5, 6, 7, 8}, merged.keys);
        assertArrayEquals (new Integer[] {100, 200, 300, 400, 500, 600, 700, 800}, merged.values);

        assertNull (result.separator);
        assertNull (result.optRight);
    }

    @Test
    public void testMergeBigLeftEven() {
        final UpdateResult result = leaf (1, 2, 3, 4, 5, 6, 7).merge (leaf (8, 9, 10), 8);

        final LeafNode left = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {1, 2, 3, 4, 5}, left.keys);
        assertArrayEquals (new Integer[] {100, 200, 300, 400, 500}, left.values);

        assertEquals (6, result.separator);

        final LeafNode right = (LeafNode) result.optRight;
        assertArrayEquals (new Integer[] {6, 7, 8, 9, 10}, right.keys);
        assertArrayEquals (new Integer[] {600, 700, 800, 900, 1000}, right.values);
    }

    @Test
    public void testMergeBigRightEven() {
        final UpdateResult result = leaf (1, 2, 3).merge (leaf (4, 5, 6, 7, 8, 9, 10), 4);

        final LeafNode left = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {1, 2, 3, 4, 5}, left.keys);
        assertArrayEquals (new Integer[] {100, 200, 300, 400, 500}, left.values);

        assertEquals (6, result.separator);

        final LeafNode right = (LeafNode) result.optRight;
        assertArrayEquals (new Integer[] {6, 7, 8, 9, 10}, right.keys);
        assertArrayEquals (new Integer[] {600, 700, 800, 900, 1000}, right.values);
    }

    @Test
    public void testMergeBigLeftOdd() {
        final UpdateResult result = leaf (1, 2, 3, 4, 5, 6, 7).merge (leaf (8, 9), 8);

        final LeafNode left = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {1, 2, 3, 4}, left.keys);
        assertArrayEquals (new Integer[] {100, 200, 300, 400}, left.values);

        assertEquals (5, result.separator);

        final LeafNode right = (LeafNode) result.optRight;
        assertArrayEquals (new Integer[] {5, 6, 7, 8, 9}, right.keys);
        assertArrayEquals (new Integer[] {500, 600, 700, 800, 900}, right.values);
    }

    @Test
    public void testMergeBigRightOdd() {
        final UpdateResult result = leaf (1, 2, 3).merge (leaf (4, 5, 6, 7, 8, 9), 4);

        final LeafNode left = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {1, 2, 3, 4}, left.keys);
        assertArrayEquals (new Integer[] {100, 200, 300, 400}, left.values);

        assertEquals (5, result.separator);

        final LeafNode right = (LeafNode) result.optRight;
        assertArrayEquals (new Integer[] {5, 6, 7, 8, 9}, right.keys);
        assertArrayEquals (new Integer[] {500, 600, 700, 800, 900}, right.values);
    }
}
