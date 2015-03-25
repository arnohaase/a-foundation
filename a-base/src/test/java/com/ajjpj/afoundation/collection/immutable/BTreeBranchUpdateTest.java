package com.ajjpj.afoundation.collection.immutable;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * This class tests functionality of index nodes with leaf nodes immediately below them ('branches')
 *
 * @author arno
 */
public class BTreeBranchUpdateTest extends AbstractBTreeTest {
    @Test
    public void testBranchGet() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        // verify the setup
        assertArrayEquals (new Integer[] {210, 310, 410, 510}, branch.separators);

        assertEquals (AOption.some (11000), branch.get (110));
        assertEquals (AOption.some (15000), branch.get (150));
        assertEquals (AOption.some (21000), branch.get (210));
        assertEquals (AOption.some (25000), branch.get (250));
        assertEquals (AOption.some (51000), branch.get (510));
        assertEquals (AOption.some (55000), branch.get (550));
    }

    @Test
    public void testBranchReplace() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (230, 999);
        assertNull (result.separator);
        assertNull (result.optRight);

        final IndexNode newNode = (IndexNode) result.left;
        assertArrayEquals (new Integer[] {210, 310, 410, 510}, newNode.separators);

        final LeafNode leafNode = (LeafNode) newNode.children[1];
        assertArrayEquals (new Integer[] {210, 220, 230, 240, 250}, leafNode.keys);
        assertArrayEquals (new Integer[] {21000, 22000, 999, 24000, 25000}, leafNode.values);
    }

    @Test
    public void testBranchInsert() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (231, 999);
        assertNull (result.separator);
        assertNull (result.optRight);

        final IndexNode newNode = (IndexNode) result.left;
        assertArrayEquals (new Integer[] {210, 310, 410, 510}, newNode.separators);

        final LeafNode leafNode = (LeafNode) newNode.children[1];
        assertArrayEquals (new Integer[] {210, 220, 230, 231, 240, 250}, leafNode.keys);
        assertArrayEquals (new Integer[] {21000, 22000, 23000, 999, 24000, 25000}, leafNode.values);
    }

    @Test
    public void testBranchInsertFirstFirst() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (101, 999);
        assertNull (result.separator);
        assertNull (result.optRight);

        final IndexNode newNode = (IndexNode) result.left;
        assertArrayEquals (new Integer[] {210, 310, 410, 510}, newNode.separators);

        final LeafNode leafNode = (LeafNode) newNode.children[0];
        assertArrayEquals (new Integer[] {101, 110, 120, 130, 140, 150}, leafNode.keys);
        assertArrayEquals (new Integer[] {999, 11000, 12000, 13000, 14000, 15000}, leafNode.values);
    }

    @Test
    public void testBranchInsertFirstLast() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (151, 999);
        assertNull (result.separator);
        assertNull (result.optRight);

        final IndexNode newNode = (IndexNode) result.left;
        assertArrayEquals (new Integer[] {210, 310, 410, 510}, newNode.separators);

        final LeafNode leafNode = (LeafNode) newNode.children[0];
        assertArrayEquals (new Integer[] {110, 120, 130, 140, 150, 151}, leafNode.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 14000, 15000, 999}, leafNode.values);
    }

    @Test
    public void testBranchInsertMiddleBetween() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (301, 999);
        assertNull (result.separator);
        assertNull (result.optRight);

        final IndexNode newNode = (IndexNode) result.left;
        assertArrayEquals (new Integer[] {210, 310, 410, 510}, newNode.separators);

        final LeafNode leafNode = (LeafNode) newNode.children[1];
        assertArrayEquals (new Integer[] {210, 220, 230, 240, 250, 301}, leafNode.keys);
        assertArrayEquals (new Integer[] {21000, 22000, 23000, 24000, 25000, 999}, leafNode.values);
    }

    @Test
    public void testBranchInsertLastLast() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (551, 999);
        assertNull (result.separator);
        assertNull (result.optRight);

        final IndexNode newNode = (IndexNode) result.left;
        assertArrayEquals (new Integer[] {210, 310, 410, 510}, newNode.separators);

        final LeafNode leafNode = (LeafNode) newNode.children[4];
        assertArrayEquals (new Integer[] {510, 520, 530, 540, 550, 551}, leafNode.keys);
        assertArrayEquals (new Integer[] {51000, 52000, 53000, 54000, 55000, 999}, leafNode.values);
    }

    //----------------------------- split

    @Test
    public void testSplit() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350, 360, 370, 380),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (341, 999);
        assertEquals (null, result.separator);
        assertEquals (null, result.optRight);

        final IndexNode newBranch = (IndexNode) result.left;

        assertArrayEquals (new Integer[] {210, 310, 350, 410, 510}, newBranch.separators);

        final LeafNode left  = (LeafNode) newBranch.children[2];
        final LeafNode right = (LeafNode) newBranch.children[3];

        assertArrayEquals (new Integer[] {310, 320, 330, 340, 341}, left.keys);
        assertArrayEquals (new Integer[] {31000, 32000, 33000, 34000, 999}, left.values);

        assertArrayEquals (new Integer[] {350, 360, 370, 380}, right.keys);
        assertArrayEquals (new Integer[] {35000, 36000, 37000, 38000}, right.values);
    }

    @Test
    public void testSplitFirst() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150, 160, 170, 180),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550)
        );

        final UpdateResult result = branch._updated (141, 999);
        assertEquals (null, result.separator);
        assertEquals (null, result.optRight);

        final IndexNode newBranch = (IndexNode) result.left;

        assertArrayEquals (new Integer[] {150, 210, 310, 410, 510}, newBranch.separators);

        final LeafNode left  = (LeafNode) newBranch.children[0];
        final LeafNode right = (LeafNode) newBranch.children[1];

        assertArrayEquals (new Integer[] {110, 120, 130, 140, 141}, left.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 14000, 999}, left.values);

        assertArrayEquals (new Integer[] {150, 160, 170, 180}, right.keys);
        assertArrayEquals (new Integer[] {15000, 16000, 17000, 18000}, right.values);
    }

    @Test
    public void testSplitLast() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550, 560, 570, 580)
        );

        final UpdateResult result = branch._updated (541, 999);
        assertEquals (null, result.separator);
        assertEquals (null, result.optRight);

        final IndexNode newBranch = (IndexNode) result.left;

        assertArrayEquals (new Integer[] {210, 310, 410, 510, 550}, newBranch.separators);

        final LeafNode left  = (LeafNode) newBranch.children[4];
        final LeafNode right = (LeafNode) newBranch.children[5];

        assertArrayEquals (new Integer[] {510, 520, 530, 540, 541}, left.keys);
        assertArrayEquals (new Integer[] {51000, 52000, 53000, 54000, 999}, left.values);

        assertArrayEquals (new Integer[] {550, 560, 570, 580}, right.keys);
        assertArrayEquals (new Integer[] {55000, 56000, 57000, 58000}, right.values);
    }

    @Test
    public void testSplitEscalate() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550, 560, 570, 580),
                leaf (610, 620, 630, 640, 650),
                leaf (710, 720, 730, 740, 750),
                leaf (810, 820, 830, 840, 850)
        );

        final UpdateResult result = branch._updated (541, 999);
        assertEquals (510, result.separator);

        final IndexNode leftBranch = (IndexNode) result.left;
        final IndexNode rightBranch = (IndexNode) result.optRight;

        assertArrayEquals (new Integer[] {210, 310, 410}, leftBranch.separators);
        assertEquals (4, leftBranch.children.length);

        assertArrayEquals (new Integer[] {550, 610, 710, 810}, rightBranch.separators);
        assertEquals (5, rightBranch.children.length);

        final LeafNode firstLeaf  = (LeafNode) rightBranch.children[0];
        final LeafNode secondLeaf = (LeafNode) rightBranch.children[1];

        assertArrayEquals (new Integer[] {510, 520, 530, 540, 541}, firstLeaf.keys);
        assertArrayEquals (new Integer[] {51000, 52000, 53000, 54000, 999}, firstLeaf.values);

        assertArrayEquals (new Integer[] {550, 560, 570, 580}, secondLeaf.keys);
        assertArrayEquals (new Integer[] {55000, 56000, 57000, 58000}, secondLeaf.values);
    }

    @Test
    public void testSplitFirstEscalate() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150, 160, 170, 180),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550),
                leaf (610, 620, 630, 640, 650),
                leaf (710, 720, 730, 740, 750),
                leaf (810, 820, 830, 840, 850)
        );

        final UpdateResult result = branch._updated (141, 999);
        assertEquals (510, result.separator);

        final IndexNode leftBranch = (IndexNode) result.left;
        final IndexNode rightBranch = (IndexNode) result.optRight;

        assertArrayEquals (new Integer[] {150, 210, 310, 410}, leftBranch.separators);
        assertEquals (5, leftBranch.children.length);

        assertArrayEquals (new Integer[] {610, 710, 810}, rightBranch.separators);
        assertEquals (4, rightBranch.children.length);

        final LeafNode firstLeaf  = (LeafNode) leftBranch.children[0];
        final LeafNode secondLeaf = (LeafNode) leftBranch.children[1];

        assertArrayEquals (new Integer[] {110, 120, 130, 140, 141}, firstLeaf.keys);
        assertArrayEquals (new Integer[] {11000, 12000, 13000, 14000, 999}, firstLeaf.values);

        assertArrayEquals (new Integer[] {150, 160, 170, 180}, secondLeaf.keys);
        assertArrayEquals (new Integer[] {15000, 16000, 17000, 18000}, secondLeaf.values);
    }

    @Test
    public void testSplitLastEscalate() {
        final IndexNode branch = branch (
                leaf (110, 120, 130, 140, 150),
                leaf (210, 220, 230, 240, 250),
                leaf (310, 320, 330, 340, 350),
                leaf (410, 420, 430, 440, 450),
                leaf (510, 520, 530, 540, 550),
                leaf (610, 620, 630, 640, 650),
                leaf (710, 720, 730, 740, 750),
                leaf (810, 820, 830, 840, 850, 860, 870, 880)
        );

        final UpdateResult result = branch._updated (841, 999);
        assertEquals (510, result.separator);

        final IndexNode leftBranch = (IndexNode) result.left;
        final IndexNode rightBranch = (IndexNode) result.optRight;

        assertArrayEquals (new Integer[] {210, 310, 410}, leftBranch.separators);
        assertEquals (4, leftBranch.children.length);

        assertArrayEquals (new Integer[] {610, 710, 810, 850}, rightBranch.separators);
        assertEquals (5, rightBranch.children.length);

        final LeafNode firstLeaf  = (LeafNode) rightBranch.children[3];
        final LeafNode secondLeaf = (LeafNode) rightBranch.children[4];

        assertArrayEquals (new Integer[] {810, 820, 830, 840, 841}, firstLeaf.keys);
        assertArrayEquals (new Integer[] {81000, 82000, 83000, 84000, 999}, firstLeaf.values);

        assertArrayEquals (new Integer[] {850, 860, 870, 880}, secondLeaf.keys);
        assertArrayEquals (new Integer[] {85000, 86000, 87000, 88000}, secondLeaf.values);
    }
}
