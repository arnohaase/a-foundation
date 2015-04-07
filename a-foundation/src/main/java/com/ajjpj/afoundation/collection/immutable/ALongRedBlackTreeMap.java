package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * This is a {@link ARedBlackTreeMap} specialization for keys of primitive 'long' values.
 *
 * @author arno
 */
public class ALongRedBlackTreeMap<V> extends AbstractAMap<Long,V> {
    final Tree<V> root;

    @SuppressWarnings ("unchecked")
    private static final ALongRedBlackTreeMap EMPTY = new ALongRedBlackTreeMap (null);

    @SuppressWarnings ("unchecked")
    public static <V> ALongRedBlackTreeMap<V> empty () {
        return EMPTY;
    }

    private ALongRedBlackTreeMap (Tree<V> root) {
        this.root = root;
    }

    @Override public int size () {
        return root == null ? 0 : root.count;
    }

    @Override public boolean containsKey (Long key) {
        return containsKey (key.longValue ());
    }
    public boolean containsKey (long key) {
        return lookup (root, key) != null;
    }

    @Override public AOption<V> get (Long key) {
        return get (key.longValue ());
    }
    public AOption<V> get (long key) {
        final Tree<V> raw = lookup (root, key);
        if (raw == null) {
            return AOption.none ();
        }
        return AOption.some (raw.value);
    }

    public V getRequired (long key) {
        return get (key).get ();
    }

    @Override public ASet<Long> keys () {
        return ALongRedBlackTreeSet.create (this);
    }

    @Override public AEquality keyEquality () {
        return AEquality.EQUALS;
    }

    @Override public AMap<Long, V> clear () {
        return empty ();
    }

    @Override public AMap<Long, V> updated (Long key, V value) {
        return updated (key.longValue (), value);
    }
    public ALongRedBlackTreeMap<V> updated (long key, V value) {
        return new ALongRedBlackTreeMap<> (blacken (upd (root, key, value)));
    }

    @Override public AMap<Long, V> removed (Long key) {
        return removed (key.longValue ());
    }
    public ALongRedBlackTreeMap<V> removed (long key) {
        return new ALongRedBlackTreeMap<> (blacken (del (root, key)));
    }

    @Override public Iterator<AMapEntry<Long, V>> iterator () {
        return new TreeIterator<AMapEntry<Long, V>> () {
            @Override AMapEntry<Long, V> nextResult (Tree<V> tree) {
                return tree;
            }
        };
    }

    @SuppressWarnings ("unused")
    static void validate(Tree tree) {
        if (tree == null) {
            return;
        }

        validate (tree.left);
        validate (tree.right);

        // rule 4: every redden node has two blacken children
        if (isRedTree (tree)) {
            if (tree.left != null && isRedTree (tree.left)) {
                throw new IllegalStateException ("tree " + tree.key + " is redden and has a left child that is redden");
            }
            if (tree.right != null && isRedTree (tree.right)) {
                throw new IllegalStateException ("tree " + tree.key + " is redden and has a right child that is redden");
            }
        }

        checkBlackDepth (tree);
    }

    private static int checkBlackDepth (Tree tree) {
        if (tree == null) {
            return 1;
        }

        final int own = isBlackTree (tree) ? 1 : 0;
        final int left  = checkBlackDepth (tree.left);
        final int right = checkBlackDepth (tree.right);

        // rule 5: every path to 'leaf' nodes must have the same number of blacken nodes
        if (left != right) {
            throw new IllegalStateException ("left and right side have paths to leaf nodes with different numbers of blacken nodes: " + tree.key);
        }
        return own + left;
    }


    private abstract class TreeIterator<R> implements Iterator<R> {
        /*
         * According to "Ralf Hinze. Constructing redden-blacken trees" [http://www.cs.ox.ac.uk/ralf.hinze/publications/#P5]
         * the maximum height of a redden-blacken tree is 2*log_2(n + 2) - 2.
         *
         * According to {@see Integer#numberOfLeadingZeros} ceil(log_2(n)) = (32 - Integer.numberOfLeadingZeros(n - 1))
         *
         * We also don't store the deepest nodes in the pathStack so the maximum pathStack length is further reduced by one.
         */
        @SuppressWarnings ("unchecked")
        private final Tree<V>[] pathStack;
        private int stackIndex = 0;
        private Tree<V> next;

        abstract R nextResult (Tree<V> tree); //TODO rename this

        @SuppressWarnings ("unchecked")
        private TreeIterator() {
            // initialize 'next' with the leftmost element
            if (root == null) {
                pathStack = null;
                next = null;
            }
            else {
                pathStack = new Tree [2 * (32 - Integer.numberOfLeadingZeros(root.count + 2 - 1)) - 2 - 1];
                next = root;
                while (next.left != null) {
                    pushPath (next);
                    next = next.left;
                }
            }
        }

        @Override public boolean hasNext() {
            return next != null;
        }

        @Override public R next() {
            if (next == null) {
                throw new NoSuchElementException ();
            }

            final Tree<V> cur = next;
            next = findNext (next.right);
            return nextResult (cur);
        }

        @Override public void remove () {
            throw new UnsupportedOperationException ();
        }

        private Tree<V> findNext (Tree<V> tree) {
            while (true) {
                if (tree == null) {
                    return popPath ();
                }
                if (tree.left == null) {
                    return tree;
                }
                pushPath (tree);
                tree = tree.left;
            }
        }

        private void pushPath (Tree<V> tree) {
            pathStack[stackIndex] = tree;
            stackIndex += 1;
        }

        private Tree<V> popPath() {
            if (stackIndex == 0) {
                // convenience for handling the end of iteration
                return null;
            }
            stackIndex -= 1;
            return pathStack[stackIndex];
        }
    }





    static <V> Tree<V> lookup(Tree<V> tree, long key) {
        while (tree != null) {
            if (key == tree.key) return tree;

            tree = (key < tree.key) ? tree.left : tree.right;
        }
        return null;
    }

    static boolean isRedTree (Tree tree) {
        return tree != null && tree.isRed ();
    }
    static boolean isBlackTree (Tree tree) {
        return tree != null && tree.isBlack ();
    }

    static <V> Tree<V> blacken (Tree<V> tree) {
        if (tree == null) {
            return null;
        }
        return tree.blacken ();
    }

    static <V> Tree<V> balanceLeft (TreeFactory<V> treeFactory, long key, V value, Tree<V> l, Tree<V> d) {
        if (isRedTree (l) && isRedTree (l.left)) {
            return new RedTree<> (l.key, l.value,
                    new BlackTree<> (l.left.key, l.left.value, l.left.left, l.left.right),
                    new BlackTree<> (key, value, l.right, d));
        }
        if (isRedTree (l) && isRedTree (l.right)) {
            return new RedTree<> (l.right.key, l.right.value,
                    new BlackTree<> (l.key, l.value, l.left, l.right.left),
                    new BlackTree<> (key, value, l.right.right, d));
        }
        return treeFactory.create (l, d);
    }

    static <V> Tree<V> balanceRight (TreeFactory<V> treeFactory, long key, V value, Tree<V> a, Tree<V> r) {
        if (isRedTree (r) && isRedTree (r.left)) {
            return new RedTree<> (r.left.key, r.left.value,
                    new BlackTree<> (key, value, a, r.left.left),
                    new BlackTree<> (r.key, r.value, r.left.right, r.right));
        }
        if (isRedTree (r) && isRedTree (r.right)) {
            return new RedTree<> (r.key, r.value,
                    new BlackTree<> (key, value, a, r.left),
                    new BlackTree<> (r.right.key, r.right.value, r.right.left, r.right.right));
        }
        return treeFactory.create (a, r);
    }

    static <V> Tree<V> upd (Tree<V> tree, long key, V value) {
        if (tree == null) {
            return new RedTree<> (key, value, null, null);
        }
        if (key < tree.key) {
            return balanceLeft (tree, tree.key, tree.value, upd (tree.left, key, value), tree.right);
        }
        if (key > tree.key) {
            return balanceRight (tree, tree.key, tree.value, tree.left, upd (tree.right, key, value));
        }
        return tree.withNewValue (key, value);
    }

    static <V> Tree<V> del (Tree<V> tree, long key) {
        if (tree == null) {
            return null;
        }

        if (key < tree.key) {
            // the node that must be deleted is to the left
            return isBlackTree (tree.left) ?
                    balanceLeft (tree.key, tree.value, del (tree.left, key), tree.right) :

                // tree.left is 'redden', so its children are guaranteed to be blacken.
                new RedTree<> (tree.key, tree.value, del (tree.left, key), tree.right);
        }
        else if (key > tree.key) {
            // the node that must be deleted is to the right
            return isBlackTree (tree.right) ?
                    balanceRight (tree.key, tree.value, tree.left, del (tree.right, key)) :
                new RedTree<> (tree.key, tree.value, tree.left, del (tree.right, key));
        }

        // delete this node and we are finished
        return append (tree.left, tree.right);

    }

    static <V> Tree<V> balance (long key, V value, Tree<V> tl, Tree<V> tr) {
        if (isRedTree (tl) && isRedTree (tr)) return new RedTree<> (key, value, tl.blacken (), tr.blacken ());

        if (isRedTree (tl)) {
            // left is redden, right is blacken
            if (isRedTree (tl.left)) return new RedTree<> (tl.key, tl.value, tl.left.blacken (), new BlackTree<> (key, value, tl.right, tr));
            if (isRedTree (tl.right)) {
                return new RedTree<> (tl.right.key, tl.right.value,
                        new BlackTree<> (tl.key, tl.value, tl.left, tl.right.left),
                        new BlackTree<> (key, value, tl.right.right, tr));
            }
            return new BlackTree<> (key, value, tl, tr);
        }

        if (isRedTree (tr)) {
            // left is blacken, right is redden
            if (isRedTree (tr.right)) return new RedTree<> (tr.key, tr.value, new BlackTree<> (key, value, tl, tr.left), tr.right.blacken ());
            if (isRedTree (tr.left))  return new RedTree<> (tr.left.key, tr.left.value, new BlackTree<> (key, value, tl, tr.left.left), new BlackTree<> (tr.key, tr.value, tr.left.right, tr.right));
            return new BlackTree<> (key, value, tl, tr);
        }

        // tl and tr are both blacken
        return new BlackTree<> (key, value, tl, tr);
    }

    private static <V> Tree<V> balanceLeft (long key, V value, Tree<V> tl, Tree<V> tr) { //TODO merge with other 'balanceLeft' method?
        if (isRedTree (tl)) {
            return new RedTree<> (key, value, tl.blacken (), tr);
        }
        if (isBlackTree (tr)) {
            return balance (key, value, tl, tr.redden ());
        }
        if (isRedTree (tr) && isBlackTree (tr.left)) {
            return new RedTree<> (tr.left.key, tr.left.value, new BlackTree<> (key, value, tl, tr.left.left), balance (tr.key, tr.value, tr.left.right, tr.right.blackToRed ()));
        }
        throw new IllegalStateException ("invariant violation");
    }

    static <V> Tree<V> balanceRight (long key, V value, Tree<V> tl, Tree<V> tr) {
        if (isRedTree (tr)) {
            return new RedTree<> (key, value, tl, tr.blacken ());
        }
        if (isBlackTree (tl)) {
            return balance (key, value, tl.redden (), tr);
        }
        if (isRedTree (tl) && isBlackTree (tl.right)) {
            return new RedTree<> (tl.right.key, tl.right.value, balance (tl.key, tl.value, tl.left.blackToRed (), tl.right.left), new BlackTree <> (key, value, tl.right.right, tr));
        }
        throw new IllegalStateException ("invariant violation");
    }

    /**
     * This method combines two separate sub-trees into a single (balanced) tree. It assumes that both subtrees are
     *  balanced and that all elements in 'tl' are smaller than all elements in 'tr'. This situation occurs when a
     *  node is deleted and its child nodes must be combined into a resulting tree.
     */
    private static <V> Tree<V> append (Tree<V> tl, Tree<V> tr) {
        if (tl == null) return tr;
        if (tr == null) return tl;

        if (isRedTree (tl) && isRedTree (tr)) {
            final Tree<V> bc = append (tl.right, tr.left);
            return isRedTree (bc) ?
                    new RedTree<> (bc.key, bc.value, new RedTree<> (tl.key, tl.value, tl.left, bc.left), new RedTree<> (tr.key, tr.value, bc.right, tr.right)) :
                    new RedTree<> (tl.key, tl.value, tl.left, new RedTree<> (tr.key, tr.value, bc, tr.right));
        }
        if (isBlackTree (tl) && isBlackTree (tr)) {
            final Tree<V> bc = append (tl.right, tr.left);
            return isRedTree (bc) ?
                    new RedTree<> (bc.key, bc.value, new BlackTree<> (tl.key, tl.value, tl.left, bc.left), new BlackTree<> (tr.key, tr.value, bc.right, tr.right)) :
                    balanceLeft (tl.key, tl.value, tl.left, new BlackTree<> (tr.key, tr.value, bc, tr.right));
        }
        if (isRedTree (tr)) {
            return new RedTree<> (tr.key, tr.value, append (tl, tr.left), tr.right);
        }
        if (isRedTree (tl)) {
            return new RedTree<> (tl.key, tl.value, tl.left, append (tl.right, tr));
        }
        throw new IllegalStateException ("invariant violation: unmatched tree on append: " + tl + ", " + tr);
    }


    /**
     * encapsulates tree creation for a given colour
     */
    interface TreeFactory<V> {
        Tree<V> create (Tree<V> left, Tree<V> right);
    }

    static abstract class Tree<V> implements TreeFactory<V>, AMapEntry<Long, V> {
        final long key;
        final V value;
        final int count;

        final Tree<V> left;
        final Tree<V> right;

        public Tree (long key, V value, Tree<V> left, Tree<V> right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;

            this.count = 1 +
                    (left == null ? 0 : left.count) +
                    (right == null ? 0 : right.count);
        }

        @Override public Long getKey () {
            return key;
        }
        @Override public V getValue () {
            return value;
        }

        abstract Tree<V> withNewValue (long key, V value);

        abstract Tree<V> blackToRed();

        abstract boolean isRed();
        abstract boolean isBlack();

        abstract Tree<V> redden ();
        abstract Tree<V> blacken ();
    }

    static class BlackTree<V> extends Tree<V> {
        public BlackTree (long key, V value, Tree<V> left, Tree<V> right) {
            super(key, value, left, right);
        }

        @Override Tree<V> withNewValue (long key, V value) {
            return new BlackTree<> (key, value, left, right);
        }
        @Override public Tree<V> create (Tree<V> left, Tree<V> right) {
            return new BlackTree<> (key, value, left, right);
        }

        @Override Tree<V> blackToRed () {
            return redden ();
        }

        @Override boolean isRed () {
            return false;
        }
        @Override boolean isBlack () {
            return true;
        }

        @Override Tree<V> redden () {
            return new RedTree<> (key, value, left, right);
        }
        @Override Tree<V> blacken () {
            return this;
        }
    }

    static class RedTree<V> extends Tree<V> {
        public RedTree (long key, V value, Tree<V> left, Tree<V> right) {
            super (key, value, left, right);
        }

        @Override Tree<V> withNewValue (long key, V value) {
            return new RedTree<> (key, value, left, right);
        }
        @Override public Tree<V> create (Tree<V> left, Tree<V> right) {
            return new RedTree<> (key, value, left, right);
        }

        @Override Tree<V> blackToRed () {
            throw new IllegalStateException ();
        }

        @Override boolean isRed () {
            return true;
        }
        @Override boolean isBlack () {
            return false;
        }

        @Override Tree<V> redden () {
            return this;
        }
        @Override Tree<V> blacken () {
            return new BlackTree<> (key, value, left, right);
        }
    }
}



