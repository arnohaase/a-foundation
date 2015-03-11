package com.ajjpj.abase.collection.immutable;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class BTreeBranchRemoveTest extends AbstractBTreeTest {
    @Test
    public void testSimpleRemove() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14, 15),
                leaf (21, 22, 23, 24, 25),
                leaf (31, 32, 33, 34, 35),
                leaf (41, 42, 43, 44, 45),
                leaf (51, 52, 53, 54, 55)
        );

        final RemoveResult result = node._removed (33, 11);
        assertEquals (11, result.leftSeparator);
        assertFalse (result.underflowed);

        final IndexNode newNode = (IndexNode) result.newNode;
        assertArrayEquals (new Integer[] {21, 31, 41, 51}, newNode.separators);
        final LeafNode leaf = (LeafNode) newNode.children[2];
        assertArrayEquals (new Integer[] {31, 32, 34, 35}, leaf.keys);
        assertArrayEquals (new Integer[] {3100, 3200, 3400, 3500}, leaf.values);
    }

    @Test
    public void testRemoveNotFound() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14, 15),
                leaf (21, 22, 23, 24, 25),
                leaf (31, 32, 33, 34, 35),
                leaf (41, 42, 43, 44, 45),
                leaf (51, 52, 53, 54, 55)
        );

        final RemoveResult resultNotFound = node._removed (12345, 11);
        assertSame (node, resultNotFound.newNode);
        assertEquals (11, resultNotFound.leftSeparator);
        assertFalse (resultNotFound.underflowed);
    }

    @Test
    public void testMergeMiddle() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14, 15),
                leaf (21, 22, 23, 24, 25),
                leaf (31, 32, 33, 34),
                leaf (41, 42, 43, 44, 45),
                leaf (51, 52, 53, 54, 55)
        );

        final RemoveResult result = node._removed (33, 11);

        assertEquals (false, result.underflowed);
        assertEquals (11, result.leftSeparator);

        final IndexNode newNode = (IndexNode) result.newNode;
        assertArrayEquals (new Integer[] {21, 31, 51}, newNode.separators);

        final LeafNode newLeaf = (LeafNode) newNode.children[2];
        assertArrayEquals (new Integer[] {31, 32, 34, 41, 42, 43, 44, 45}, newLeaf.keys);
        assertArrayEquals (new Integer[] {3100, 3200, 3400, 4100, 4200, 4300, 4400, 4500}, newLeaf.values);
    }

    //TODO merge and split --> redistribute

    @Test
    public void testMergeStart() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24, 25),
                leaf (31, 32, 33, 34, 35),
                leaf (41, 42, 43, 44, 45),
                leaf (51, 52, 53, 54, 55)
        );

        final RemoveResult result = node._removed (11, 11);

        assertEquals (false, result.underflowed);
        assertEquals (12, result.leftSeparator);

        final IndexNode newNode = (IndexNode) result.newNode;
        assertArrayEquals (new Integer[] {31, 41, 51}, newNode.separators);

        final LeafNode newLeaf = (LeafNode) newNode.children[0];
        assertArrayEquals (new Integer[] {12, 13, 14, 21, 22, 23, 24, 25}, newLeaf.keys);
        assertArrayEquals (new Integer[] {1200, 1300, 1400, 2100, 2200, 2300, 2400, 2500}, newLeaf.values);
    }

    @Test
    public void testMergeEnd() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14, 15),
                leaf (21, 22, 23, 24, 25),
                leaf (31, 32, 33, 34, 35),
                leaf (41, 42, 43, 44, 45),
                leaf (51, 52, 53, 54)
        );

        final RemoveResult result = node._removed (53, 11);

        assertEquals (false, result.underflowed);
        assertEquals (11, result.leftSeparator);

        final IndexNode newNode = (IndexNode) result.newNode;
        assertArrayEquals (new Integer[] {21, 31, 41}, newNode.separators);

        final LeafNode newLeaf = (LeafNode) newNode.children[3];
        assertArrayEquals (new Integer[] {41, 42, 43, 44, 45, 51, 52, 54}, newLeaf.keys);
        assertArrayEquals (new Integer[] {4100, 4200, 4300, 4400, 4500, 5100, 5200, 5400}, newLeaf.values);
    }

    @Test
    public void testMergeSplitMiddle() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14, 15),
                leaf (21, 22, 23, 24, 25),
                leaf (31, 32, 33, 34),
                leaf (41, 42, 43, 44, 45, 46, 47, 48),
                leaf (51, 52, 53, 54, 55)
        );

        final RemoveResult result = node._removed (33, 11);

        assertEquals (false, result.underflowed);
        assertEquals (11, result.leftSeparator);

        final IndexNode newNode = (IndexNode) result.newNode;
        assertArrayEquals (new Integer[] {21, 31, 43, 51}, newNode.separators);

        final LeafNode left  = (LeafNode) newNode.children[2];
        final LeafNode right = (LeafNode) newNode.children[3];

        assertArrayEquals (new Integer[] {31, 32, 34, 41, 42}, left.keys);
        assertArrayEquals (new Integer[] {3100, 3200, 3400, 4100, 4200}, left.values);
        assertArrayEquals (new Integer[] {43, 44, 45, 46, 47, 48}, right.keys);
        assertArrayEquals (new Integer[] {4300, 4400, 4500, 4600, 4700, 4800}, right.values);
    }

    @Test
    public void testMergeSplitStart() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24, 25, 26, 27, 28),
                leaf (31, 32, 33, 34, 35),
                leaf (41, 42, 43, 44, 45),
                leaf (51, 52, 53, 54, 55)
        );

        final RemoveResult result = node._removed (11, 11);

        assertEquals (false, result.underflowed);
        assertEquals (12, result.leftSeparator);

        final IndexNode newNode = (IndexNode) result.newNode;
        assertArrayEquals (new Integer[] {23, 31, 41, 51}, newNode.separators);

        final LeafNode left  = (LeafNode) newNode.children[0];
        final LeafNode right = (LeafNode) newNode.children[1];

        assertArrayEquals (new Integer[] {12, 13, 14, 21, 22}, left.keys);
        assertArrayEquals (new Integer[] {1200, 1300, 1400, 2100, 2200}, left.values);
        assertArrayEquals (new Integer[] {23, 24, 25, 26, 27, 28}, right.keys);
        assertArrayEquals (new Integer[] {2300, 2400, 2500, 2600, 2700, 2800}, right.values);
    }

    @Test
    public void testMergeSplitEnd() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14, 15),
                leaf (21, 22, 23, 24, 25),
                leaf (31, 32, 33, 34, 35),
                leaf (41, 42, 43, 44, 45, 46, 47, 48),
                leaf (51, 52, 53, 54)
        );

        final RemoveResult result = node._removed (54, 11);

        assertEquals (false, result.underflowed);
        assertEquals (11, result.leftSeparator);

        final IndexNode newNode = (IndexNode) result.newNode;
        assertArrayEquals (new Integer[] {21, 31, 41, 46}, newNode.separators);

        final LeafNode left  = (LeafNode) newNode.children[3];
        final LeafNode right = (LeafNode) newNode.children[4];

        assertArrayEquals (new Integer[] {41, 42, 43, 44, 45}, left.keys);
        assertArrayEquals (new Integer[] {4100, 4200, 4300, 4400, 4500}, left.values);
        assertArrayEquals (new Integer[] {46, 47, 48, 51, 52, 53}, right.keys);
        assertArrayEquals (new Integer[] {4600, 4700, 4800, 5100, 5200, 5300}, right.values);
    }

    @Test
    public void testMergeToSingle() {
        final IndexNode left = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24),
                leaf (31, 32, 33, 34)
        );

        final IndexNode right = branch (
                leaf (51, 52, 53, 54),
                leaf (61, 62, 63, 64),
                leaf (71, 72, 73, 74),
                leaf (81, 82, 83, 84)
        );

        final UpdateResult merged = left.merge (right, 51);
        assertEquals (null, merged.optRight);
        assertEquals (null, merged.separator);

        final IndexNode n = (IndexNode) merged.left;
        assertArrayEquals (new Integer[] {21, 31, 51, 61, 71, 81}, n.separators);
        assertEquals (7, n.children.length);
        for (InMemoryBTree child: n.children) {
            assertNotNull (child);
        }
    }

    @Test
    public void testMergeSplitLeftBigOdd() {
        final IndexNode left = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24),
                leaf (31, 32, 33, 34),
                leaf (41, 42, 43, 44),
                leaf (51, 52, 53, 54),
                leaf (55, 56, 57, 58)
        );

        final IndexNode right = branch (
                leaf (61, 62, 63, 64),
                leaf (71, 72, 73, 74),
                leaf (81, 82, 83, 84)
        );

        final UpdateResult merged = left.merge (right, 61);
        assertEquals (51, merged.separator);

        final IndexNode newLeft = (IndexNode) merged.left;
        final IndexNode newRight = (IndexNode) merged.optRight;

        assertArrayEquals (new Integer[] {21, 31, 41}, newLeft.separators);
        assertEquals (4, newLeft.children.length);
        for (InMemoryBTree child: newLeft.children) {
            assertNotNull (child);
        }

        assertArrayEquals (new Integer[] {55, 61, 71, 81}, newRight.separators);
        assertEquals (5, newRight.children.length);
        for (InMemoryBTree child: newRight.children) {
            assertNotNull (child);
        }
    }

    @Test
    public void testMergeSplitRightBigOdd() {
        final IndexNode left = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24),
                leaf (31, 32, 33, 34)
        );

        final IndexNode right = branch (
                leaf (41, 42, 43, 44),
                leaf (51, 52, 53, 54),
                leaf (55, 56, 57, 58),
                leaf (61, 62, 63, 64),
                leaf (71, 72, 73, 74),
                leaf (81, 82, 83, 84)
        );

        final UpdateResult merged = left.merge (right, 41);
        assertEquals (51, merged.separator);

        final IndexNode newLeft = (IndexNode) merged.left;
        final IndexNode newRight = (IndexNode) merged.optRight;

        assertArrayEquals (new Integer[] {21, 31, 41}, newLeft.separators);
        assertEquals (4, newLeft.children.length);
        for (InMemoryBTree child: newLeft.children) {
            assertNotNull (child);
        }

        assertArrayEquals (new Integer[] {55, 61, 71, 81}, newRight.separators);
        assertEquals (5, newRight.children.length);
        for (InMemoryBTree child: newRight.children) {
            assertNotNull (child);
        }
    }

    @Test
    public void testMergeSplitLeftBigEven() {
        final IndexNode left = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24),
                leaf (31, 32, 33, 34),
                leaf (41, 42, 43, 44),
                leaf (45, 46, 47, 48),
                leaf (51, 52, 53, 54),
                leaf (55, 56, 57, 58)
        );

        final IndexNode right = branch (
                leaf (61, 62, 63, 64),
                leaf (71, 72, 73, 74),
                leaf (81, 82, 83, 84)
        );

        final UpdateResult merged = left.merge (right, 61);
        assertEquals (51, merged.separator);

        final IndexNode newLeft = (IndexNode) merged.left;
        final IndexNode newRight = (IndexNode) merged.optRight;

        assertArrayEquals (new Integer[] {21, 31, 41, 45}, newLeft.separators);
        assertEquals (5, newLeft.children.length);
        for (InMemoryBTree child: newLeft.children) {
            assertNotNull (child);
        }

        assertArrayEquals (new Integer[] {55, 61, 71, 81}, newRight.separators);
        assertEquals (5, newRight.children.length);
        for (InMemoryBTree child: newRight.children) {
            assertNotNull (child);
        }
    }

    @Test
    public void testMergeSplitRightBigEven() {
        final IndexNode left = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24),
                leaf (31, 32, 33, 34)
        );

        final IndexNode right = branch (
                leaf (41, 42, 43, 44),
                leaf (45, 46, 47, 48),
                leaf (51, 52, 53, 54),
                leaf (55, 56, 57, 58),
                leaf (61, 62, 63, 64),
                leaf (71, 72, 73, 74),
                leaf (81, 82, 83, 84)
        );

        final UpdateResult merged = left.merge (right, 41);
        assertEquals (51, merged.separator);

        final IndexNode newLeft = (IndexNode) merged.left;
        final IndexNode newRight = (IndexNode) merged.optRight;

        assertArrayEquals (new Integer[] {21, 31, 41, 45}, newLeft.separators);
        assertEquals (5, newLeft.children.length);
        for (InMemoryBTree child: newLeft.children) {
            assertNotNull (child);
        }

        assertArrayEquals (new Integer[] {55, 61, 71, 81}, newRight.separators);
        assertEquals (5, newRight.children.length);
        for (InMemoryBTree child: newRight.children) {
            assertNotNull (child);
        }
    }

    @Test
    public void testUnderflow() {
        final IndexNode node = branch (
                leaf (11, 12, 13, 14),
                leaf (21, 22, 23, 24),
                leaf (31, 32, 33, 34),
                leaf (41, 42, 43, 44)
        );

        final RemoveResult result = node._removed (32, 11);

        assertEquals (11, result.leftSeparator);
        assertEquals (true, result.underflowed);

        final IndexNode newNode = (IndexNode) result.newNode;

        assertArrayEquals (new Integer[] {21, 31}, newNode.separators);
        assertEquals (3, newNode.children.length);
        final LeafNode newLeaf = (LeafNode) newNode.children[2];
        assertArrayEquals (new Integer[] {31, 33, 34, 41, 42, 43, 44}, newLeaf.keys);
        assertArrayEquals (new Integer[] {3100, 3300, 3400, 4100, 4200, 4300, 4400}, newLeaf.values);
    }

    //TODO underflow escalation
}
