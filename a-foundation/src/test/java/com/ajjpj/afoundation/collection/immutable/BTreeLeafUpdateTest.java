package com.ajjpj.afoundation.collection.immutable;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * This class tests functionality of leaf nodes by themselves
 *
 * @author arno
 */
public class BTreeLeafUpdateTest extends AbstractBTreeTest {
    @Test
    public void testLeafReplace() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150);
        final UpdateResult result = leafNode._updated (130, 999);

        assertNull (result.separator);
        assertNull (result.optRight);

        final LeafNode newLeaf = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {110, 120, 130, 140, 150}, newLeaf.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 999, 14000, 15000}, newLeaf.values);
    }

    @Test
    public void testLeafReplaceFirst() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150);
        final UpdateResult result = leafNode._updated (110, 999);

        assertNull (result.separator);
        assertNull (result.optRight);

        final LeafNode newLeaf = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {110, 120, 130, 140, 150}, newLeaf.keys);
        assertArrayEquals (new Integer[] {999, 12000, 13000, 14000, 15000}, newLeaf.values);
    }

    @Test
    public void testLeafReplaceLast() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150);
        final UpdateResult result = leafNode._updated (150, 999);

        assertNull (result.separator);
        assertNull (result.optRight);

        final LeafNode newLeaf = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {110, 120, 130, 140, 150}, newLeaf.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 14000, 999}, newLeaf.values);
    }

    @Test
    public void testLeafInsertMiddle() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150);
        final UpdateResult result = leafNode._updated (131, 999);

        assertNull (result.separator);
        assertNull (result.optRight);

        final LeafNode newLeaf = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {110, 120, 130, 131, 140, 150}, newLeaf.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 999, 14000, 15000}, newLeaf.values);
    }

    @Test
    public void testLeafInsertFirst() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150);
        final UpdateResult result = leafNode._updated (101, 999);

        assertNull (result.separator);
        assertNull (result.optRight);

        final LeafNode newLeaf = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {101, 110, 120, 130, 140, 150}, newLeaf.keys);
        assertArrayEquals (new Integer[] {999, 11000, 12000, 13000, 14000, 15000}, newLeaf.values);
    }

    @Test
    public void testLeafInsertLast() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150);
        final UpdateResult result = leafNode._updated (151, 999);

        assertNull (result.separator);
        assertNull (result.optRight);

        final LeafNode newLeaf = (LeafNode) result.left;
        assertArrayEquals (new Integer[] {110, 120, 130, 140, 150, 151}, newLeaf.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 14000, 15000, 999}, newLeaf.values);
    }

    @Test
    public void testLeafInsertOverflow() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150, 160, 170, 180);

        final UpdateResult result = leafNode._updated (121, 999);
        assertEquals (150, result.separator);

        final LeafNode left = (LeafNode) result.left;
        final LeafNode right = (LeafNode) result.optRight;

        assertArrayEquals (new Integer[] {110, 120, 121, 130, 140}, left.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 999, 13000, 14000}, left.values);

        assertArrayEquals (new Integer[] {150, 160, 170, 180}, right.keys);
        assertArrayEquals (new Integer[] {15000, 16000, 17000, 18000}, right.values);
    }

    @Test
    public void testLeafInsertOverflowFirst() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150, 160, 170, 180);

        final UpdateResult result = leafNode._updated (101, 999);
        assertEquals (150, result.separator);

        final LeafNode left = (LeafNode) result.left;
        final LeafNode right = (LeafNode) result.optRight;

        assertArrayEquals (new Integer[] {101, 110, 120, 130, 140}, left.keys);
        assertArrayEquals (new Integer[] {999, 11000, 12000, 13000, 14000}, left.values);

        assertArrayEquals (new Integer[] {150, 160, 170, 180}, right.keys);
        assertArrayEquals (new Integer[] {15000, 16000, 17000, 18000}, right.values);
    }

    @Test
    public void testLeafInsertOverflowLast() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150, 160, 170, 180);

        final UpdateResult result = leafNode._updated (181, 999);
        assertEquals (150, result.separator);

        final LeafNode left = (LeafNode) result.left;
        final LeafNode right = (LeafNode) result.optRight;

        assertArrayEquals (new Integer[] {110, 120, 130, 140}, left.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 14000}, left.values);

        assertArrayEquals (new Integer[] {150, 160, 170, 180, 181}, right.keys);
        assertArrayEquals (new Integer[] {15000, 16000, 17000, 18000, 999}, right.values);
    }

    @Test
    public void testLeafInsertOverflowMiddle() {
        final LeafNode leafNode = leaf (110, 120, 130, 140, 150, 160, 170, 180);

        final UpdateResult result = leafNode._updated (141, 999);
        assertEquals (150, result.separator);

        final LeafNode left = (LeafNode) result.left;
        final LeafNode right = (LeafNode) result.optRight;

        assertArrayEquals (new Integer[] {110, 120, 130, 140, 141}, left.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 14000, 999}, left.values);

        assertArrayEquals (new Integer[] {150, 160, 170, 180}, right.keys);
        assertArrayEquals (new Integer[] {15000, 16000, 17000, 18000}, right.values);
    }
}
