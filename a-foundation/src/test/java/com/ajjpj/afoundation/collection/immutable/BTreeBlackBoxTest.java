package com.ajjpj.afoundation.collection.immutable;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class BTreeBlackBoxTest {
    @Test
    public void testSplitLeft() {
        ABTreeMap<String, String> tree = ABTreeMap.empty (new BTreeSpec (4, AbstractBTreeTest.naturalOrder));

        tree = tree.updated ("2", "2");
        tree = tree.updated ("4", "4");
        tree = tree.updated ("6", "6");
        tree = tree.updated ("8", "8");

        tree = tree.updated ("1", "1");
        tree = tree.updated ("3", "3");
        tree = tree.updated ("5", "5");
        tree = tree.updated ("7", "7");

        for (int i=1; i<=8; i++) {
            assertEquals ("" + i, tree.get ("" + i).get ());
        }
    }

    @Test
    public void testSplitRight() {
        ABTreeMap<String, String> tree = ABTreeMap.empty (new BTreeSpec (4, AbstractBTreeTest.naturalOrder));

        tree = tree.updated ("2", "2");
        tree = tree.updated ("4", "4");
        tree = tree.updated ("6", "6");
        tree = tree.updated ("8", "8");

        tree = tree.updated ("7", "7");
        tree = tree.updated ("5", "5");
        tree = tree.updated ("3", "3");
        tree = tree.updated ("1", "1");

        for (int i=1; i<=8; i++) {
            assertEquals ("" + i, tree.get ("" + i).get ());
        }
    }

    @Test
    public void testShotgun() {
        final Random random = new Random (12345);

        ABTreeMap<Integer, Integer> tree = ABTreeMap.empty (new BTreeSpec (8, AbstractBTreeTest.naturalOrder));
        final Map<Integer, Integer> reference = new HashMap<> ();

        for (int i=0; i<1_000_000; i++) {
            final boolean add = random.nextInt (10) < 7;
            final int key = random.nextInt (100_000);

            if (add) {
                final int value = random.nextInt (1_000);
                tree = tree.updated (key, value);
                reference.put (key, value);
            }
            else {
                tree = tree.removed (key);
                reference.remove (key);
            }
        }

        for (int i=0; i<100_000; i++) {
            if (reference.containsKey (i)) {
                assertEquals (reference.get (i), tree.get (i).get ());
            }
            else {
                assertEquals (AOption.<Integer>none (), tree.get (i));
            }
        }
    }
}
