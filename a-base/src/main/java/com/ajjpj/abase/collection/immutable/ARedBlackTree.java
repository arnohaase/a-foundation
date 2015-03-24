package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.tuples.ATuple2;
import com.ajjpj.abase.function.AFunction1;

import java.util.*;


//TODO extract 'ASortedMap' / 'ASortedSet'
//TODO generify ASet creation based on AMap
//TODO map performance comparison benchmarks

/**
 * @author arno
 */
public class ARedBlackTree<K,V> implements AMap<K,V> {
    final Tree<K,V> root;
    private final Comparator<K> comparator;

    transient private Integer cachedHashcode = null; // intentionally not volatile: This class is immutable, so recalculating per thread works

    public static <K,V> ARedBlackTree<K,V> empty (Comparator<K> comparator) {
        return new ARedBlackTree<> (null, comparator);
    }

    private ARedBlackTree (Tree<K, V> root, Comparator<K> comparator) {
        this.root = root;
        this.comparator = comparator;
    }

    @Override public int size () {
        return root == null ? 0 : root.count;
    }

    @Override public boolean isEmpty () {
        return root == null;
    }

    @Override public boolean nonEmpty () {
        return root != null;
    }

    @Override public boolean containsKey (K key) {
        return lookup (root, key, comparator) != null;
    }

    @Override public boolean containsValue (V value) {
        return values ().contains (value);
    }

    @Override public AOption<V> get (K key) {
        final Tree<K,V> raw = lookup (root, key, comparator);
        if (raw == null) {
            return AOption.none ();
        }
        return AOption.some (raw.value);
    }

    @Override public V getRequired (K key) {
        return get (key).get ();
    }

    @Override public Set<K> keys () {
        return new AbstractSet<K> () {
            @SuppressWarnings ("unchecked")
            @Override public boolean contains (Object o) {
                return ARedBlackTree.this.containsKey ((K) o);
            }
            @SuppressWarnings ("NullableProblems")
            @Override public Iterator<K> iterator () {
                return new TreeIterator<K> () {
                    @Override K nextResult (Tree<K, V> tree) {
                        return tree.key;
                    }
                };
            }
            @Override public int size () {
                return ARedBlackTree.this.size ();
            }
        };
    }

    @Override public Collection<V> values () {
        return new AbstractCollection<V> () {
            @SuppressWarnings ("NullableProblems")
            @Override public Iterator<V> iterator () {
                return new TreeIterator<V> () {
                    @Override V nextResult (Tree<K, V> tree) {
                        return tree.value;
                    }
                };
            }
            @Override public int size () {
                return ARedBlackTree.this.size ();
            }
        };
    }

    @Override public AMap<K, V> updated (K key, V value) {
        return new ARedBlackTree<> (blacken (upd (root, key, value, comparator)), comparator);
    }

    @Override public AMap<K, V> removed (K key) {
        return new ARedBlackTree<> (blacken (del (root, key, comparator)), comparator);
    }

    @Override public Iterator<ATuple2<K, V>> iterator () {
        return new TreeIterator<ATuple2<K, V>> () {
            @Override ATuple2<K, V> nextResult (Tree<K, V> tree) {
                return new ATuple2<> (tree.key, tree.value);
            }
        };
    }

    @Override public Map<K, V> asJavaUtilMap () {
        return new JavaUtilMapWrapper<> (this);
    }

    @Override public AMap<K, V> withDefaultValue (V defaultValue) {
        return new AMapWithDefaultValue<> (this, defaultValue);
    }

    @Override public AMap<K, V> withDefault (AFunction1<? super K, ? extends V, ? extends RuntimeException> function) {
        return new AMapWithDefault<> (this, function);
    }

    @Override public boolean equals (Object obj) {
        if (obj == this) {
            return true;
        }
        if (! (obj instanceof ARedBlackTree)) {
            return false;
        }

        final ARedBlackTree other = (ARedBlackTree) obj;
        if (size () != other.size ()) {
            return false;
        }

        final Iterator iter1 = iterator ();
        final Iterator iter2 = other.iterator ();

        while (iter1.hasNext ()) {
            if (! iter2.hasNext ()) {
                return false;
            }
            if (! Objects.equals (iter1.next (), iter2.next ())) {
                return false;
            }
        }

        return true;
    }

    @Override public int hashCode () {
        if(cachedHashcode == null) {
            int result = 0;

            for(ATuple2<K,V> el: this) {
                result = result ^ (31*Objects.hashCode(el._1) + Objects.hashCode(el._2));
            }

            cachedHashcode = result;
        }

        return cachedHashcode;
    }

    public void dump() {
        dump (root, 0);
    }

    static void indent (int level) {
        System.out.print ("                                                                                                                                                                            ".substring (0, 4*level));
    }

    static void dump (Tree tree, int indent) {
        if (tree == null) {
            indent (indent);
            System.out.println ("<>");
        }
        else {
            dump (tree.left, indent+1);

            indent (indent);
            System.out.print (tree instanceof BlackTree ? "+ " : "* ");
            System.out.println (tree.key);

            dump (tree.right, indent+1);
        }
    }

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
        private final Tree<K,V>[] pathStack;
        private int stackIndex = 0;
        private Tree<K,V> next;

        abstract R nextResult (Tree<K,V> tree); //TODO rename this

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

            final Tree<K,V> cur = next;
            next = findNext (next.right);
            return nextResult (cur);
        }


        private Tree<K,V> findNext (Tree<K,V> tree) {
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

        private void pushPath (Tree<K,V> tree) {
            pathStack[stackIndex] = tree;
            stackIndex += 1;
        }

        private Tree<K,V> popPath() {
            if (stackIndex == 0) {
                // convenience for handling the end of iteration
                return null;
            }
            stackIndex -= 1;
            return pathStack[stackIndex];
        }
    }





    static <K,V> Tree<K,V> lookup(Tree<K,V> tree, K key, Comparator<K> comparator) {
        while (tree != null) {
            final int cmp = comparator.compare (key, tree.key);
            if (cmp == 0) return tree;

            tree = (cmp < 0) ? tree.left : tree.right;
        }
        return null;
    }


    static boolean isRedTree (Tree tree) {
        return tree != null && tree.isRed ();
    }
    static boolean isBlackTree (Tree tree) {
        return tree != null && tree.isBlack ();
    }

    static <K,V> Tree<K,V> blacken (Tree<K,V> tree) {
        if (tree == null) {
            return null;
        }
        return tree.blacken ();
    }

    static <K,V> Tree<K,V> balanceLeft (TreeFactory<K,V> treeFactory, K key, V value, Tree<K,V> l, Tree<K,V> d) {
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

    static <K,V> Tree<K,V> balanceRight (TreeFactory<K,V> treeFactory, K key, V value, Tree<K,V> a, Tree<K,V> r) {
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

    static <K,V> Tree<K,V> upd (Tree<K,V> tree, K key, V value, Comparator<K> comparator) {
        if (tree == null) {
            return new RedTree<> (key, value, null, null);
        }
        final int cmp = comparator.compare (key, tree.key);
        if (cmp < 0) {
            return balanceLeft (tree, tree.key, tree.value, upd (tree.left, key, value, comparator), tree.right);
        }
        if (cmp > 0) {
            return balanceRight (tree, tree.key, tree.value, tree.left, upd (tree.right, key, value, comparator));
        }
        return tree.withNewValue (key, value);
    }

    static <K,V> Tree<K,V> del (Tree<K,V> tree, K key, Comparator<K> comparator) {
        if (tree == null) {
            return null;
        }

        final int cmp = comparator.compare(key, tree.key);
        if (cmp < 0) {
            // the node that must be deleted is to the left
            return isBlackTree (tree.left) ?
                    balanceLeft (tree.key, tree.value, del (tree.left, key, comparator), tree.right) :

                // tree.left is 'redden', so its children are guaranteed to be blacken.
                new RedTree<> (tree.key, tree.value, del (tree.left, key, comparator), tree.right);
        }
        else if (cmp > 0) {
            // the node that must be deleted is to the right
            return isBlackTree (tree.right) ?
                    balanceRight (tree.key, tree.value, tree.left, del (tree.right, key, comparator)) :
                new RedTree<> (tree.key, tree.value, tree.left, del (tree.right, key, comparator));
        }

        // delete this node and we are finished
        return append (tree.left, tree.right);

    }

    static <K,V> Tree<K,V> balance (K key, V value, Tree<K, V> tl, Tree<K, V> tr) {
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

    private static <K,V> Tree<K,V> balanceLeft (K key, V value, Tree<K, V> tl, Tree<K, V> tr) { //TODO merge with other 'balanceLeft' method?
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

    static <K,V> Tree<K,V> balanceRight (K key, V value, Tree<K, V> tl, Tree<K, V> tr) {
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
    private static <K,V> Tree<K,V> append (Tree<K,V> tl, Tree<K,V> tr) {
        if (tl == null) return tr;
        if (tr == null) return tl;

        if (isRedTree (tl) && isRedTree (tr)) {
            final Tree<K,V> bc = append (tl.right, tr.left);
            return isRedTree (bc) ?
                    new RedTree<> (bc.key, bc.value, new RedTree<> (tl.key, tl.value, tl.left, bc.left), new RedTree<> (tr.key, tr.value, bc.right, tr.right)) :
                    new RedTree<> (tl.key, tl.value, tl.left, new RedTree<> (tr.key, tr.value, bc, tr.right));
        }
        if (isBlackTree (tl) && isBlackTree (tr)) {
            final Tree<K,V> bc = append (tl.right, tr.left);
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
    interface TreeFactory<K,V> {
        Tree<K,V> create (Tree<K,V> left, Tree<K,V> right);
    }

    static abstract class Tree<K,V> implements TreeFactory<K,V> {
        final K key;
        final V value;
        final int count;

        final Tree<K,V> left;
        final Tree<K,V> right;

        public Tree (K key, V value, Tree<K, V> left, Tree<K, V> right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;

            this.count = 1 +
                    (left == null ? 0 : left.count) +
                    (right == null ? 0 : right.count);
        }

        abstract Tree<K,V> withNewValue (K key, V value);

        abstract Tree<K,V> blackToRed();

        abstract boolean isRed();
        abstract boolean isBlack();

        abstract Tree<K,V> redden ();
        abstract Tree<K,V> blacken ();
    }

    static class BlackTree<K,V> extends Tree<K,V> {
        public BlackTree (K key, V value, Tree<K, V> left, Tree<K, V> right) {
            super(key, value, left, right);
        }

        @Override Tree<K, V> withNewValue (K key, V value) {
            return new BlackTree<> (key, value, left, right);
        }
        @Override public Tree<K, V> create (Tree<K, V> left, Tree<K, V> right) {
            return new BlackTree<> (key, value, left, right);
        }

        @Override Tree<K, V> blackToRed () {
            return redden ();
        }

        @Override boolean isRed () {
            return false;
        }
        @Override boolean isBlack () {
            return true;
        }

        @Override Tree<K, V> redden () {
            return new RedTree<> (key, value, left, right);
        }
        @Override Tree<K, V> blacken () {
            return this;
        }
    }

    static class RedTree<K,V> extends Tree<K,V> {
        public RedTree (K key, V value, Tree<K, V> left, Tree<K, V> right) {
            super (key, value, left, right);
        }

        @Override Tree<K, V> withNewValue (K key, V value) {
            return new RedTree<> (key, value, left, right);
        }
        @Override public Tree<K, V> create (Tree<K, V> left, Tree<K, V> right) {
            return new RedTree<> (key, value, left, right);
        }

        @Override Tree<K, V> blackToRed () {
            throw new IllegalStateException ();
        }

        @Override boolean isRed () {
            return true;
        }
        @Override boolean isBlack () {
            return false;
        }

        @Override Tree<K, V> redden () {
            return this;
        }
        @Override Tree<K, V> blacken () {
            return new BlackTree<> (key, value, left, right);
        }
    }
}



