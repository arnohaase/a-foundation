package com.ajjpj.abase.collection.immutable;

import com.ajjpj.abase.collection.tuples.ATuple2;
import com.ajjpj.abase.function.AFunction1;

import java.util.*;


/**
 * @author arno
 */
public class ARedBlackTree<K,V> implements AMap<K,V> {
    private final Tree<K,V> root;
    private final Comparator<K> comparator;

    transient private Integer cachedHashcode = null; // intentionally not volatile: This class is immutable, so recalculating per thread works

    public static <K,V> ARedBlackTree<K,V> empty (Comparator<K> comparator) {
        return new ARedBlackTree<> (null, comparator);
    }

    private ARedBlackTree (Tree<K, V> root, Comparator<K> comparator) {
        validate (root); //TODO remove this

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

        // rule 4: every red node has two black children
        if (isRedTree (tree)) {
            if (tree.left != null && isRedTree (tree.left)) {
                throw new IllegalStateException ("tree " + tree.key + " is red and has a left child that is red");
            }
            if (tree.right != null && isRedTree (tree.right)) {
                throw new IllegalStateException ("tree " + tree.key + " is red and has a right child that is red");
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

        // rule 5: every path to 'leaf' nodes must have the same number of black nodes
        if (left != right) {
            throw new IllegalStateException ("left and right side have paths to leaf nodes with different numbers of black nodes: " + tree.key);
        }
        return own + left;
    }


    private abstract class TreeIterator<R> implements Iterator<R> { //TODO special treatment for empty sets
        /*
         * According to "Ralf Hinze. Constructing red-black trees" [http://www.cs.ox.ac.uk/ralf.hinze/publications/#P5]
         * the maximum height of a red-black tree is 2*log_2(n + 2) - 2.
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
        if (tree == null) {
            return null;
        }
        final int cmp = comparator.compare (key, tree.key);
        if (cmp < 0) {
            return lookup (tree.left, key, comparator);
        }
        else if (cmp > 0) {
            return lookup (tree.right, key, comparator);
        }
        else {
            return tree;
        }
    }


    static boolean isRedTree (Tree tree) {
        return tree instanceof RedTree;
    }
    static boolean isBlackTree (Tree tree) {
        if (tree == null) {
            System.out.println ("***");
            return true;
        }

        return tree instanceof BlackTree;
    }

    static <K,V> Tree<K,V> blacken (Tree<K,V> tree) {
        if (tree == null) {
            return null;
        }
        return tree.black();
    }

    static <K,V> Tree<K,V> mkTree (boolean isBlack, K key, V value, Tree<K,V> left, Tree<K,V> right) {
        if (isBlack) {
            return new BlackTree<> (key, value, left, right);
        }
        else {
            return new RedTree<> (key, value, left, right);
        }
    }

    static <K,V> Tree<K,V> balanceLeft (boolean isBlack, K key, V value, Tree<K,V> l, Tree<K,V> d) {
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
        return mkTree (isBlack, key, value, l, d);
    }

    static <K,V> Tree<K,V> balanceRight (boolean isBlack, K key, V value, Tree<K,V> a, Tree<K,V> r) {
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
        return mkTree (isBlack, key, value, a, r);
    }

    static <K,V> Tree<K,V> upd (Tree<K,V> tree, K key, V value, Comparator<K> comparator) {
        if (tree == null) {
            return new RedTree<> (key, value, null, null);
        }
        final int cmp = comparator.compare (key, tree.key);
        if (cmp < 0) {
            return balanceLeft (isBlackTree (tree), tree.key, tree.value, upd (tree.left, key, value, comparator), tree.right);
        }
        if (cmp > 0) {
            return balanceRight (isBlackTree (tree), tree.key, tree.value, tree.left, upd (tree.right, key, value, comparator));
        }
        return mkTree (isBlackTree (tree), key, value, tree.left, tree.right); //TODO tree.withNewValue(value)
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

                // tree.left is 'red', so its children are guaranteed to be black.
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
        if (isRedTree (tl) && isRedTree (tr)) return new RedTree<> (key, value, tl.black(), tr.black());

        if (isRedTree (tl)) {
            // left is red, right is black
            if (isRedTree (tl.left)) return new RedTree<> (tl.key, tl.value, tl.left.black(), new BlackTree<> (key, value, tl.right, tr));
            if (isRedTree (tl.right)) {
                return new RedTree<> (tl.right.key, tl.right.value,
                        new BlackTree<> (tl.key, tl.value, tl.left, tl.right.left),
                        new BlackTree<> (key, value, tl.right.right, tr));
            }
            return new BlackTree<> (key, value, tl, tr);
        }

        if (isRedTree (tr)) {
            // left is black, right is red
            if (isRedTree (tr.right)) return new RedTree<> (tr.key, tr.value, new BlackTree<> (key, value, tl, tr.left), tr.right.black());
            if (isRedTree (tr.left))  return new RedTree<> (tr.left.key, tr.left.value, new BlackTree<> (key, value, tl, tr.left.left), new BlackTree<> (tr.key, tr.value, tr.left.right, tr.right));
            return new BlackTree<> (key, value, tl, tr);
        }

        // tl and tr are both black
        return new BlackTree<> (key, value, tl, tr);
    }

    static <K,V> Tree<K,V> delSubl(Tree<K,V> t) { //TODO Tree.blackToRed()
        if (t instanceof BlackTree) return t.red();
        throw new IllegalStateException ("invariant violation: expected black, got " + t);
    }

    /**
     * Creates a new Tree and balances it. This method assumes that the left sub-tree 'tl' is TODO
     */
    private static <K,V> Tree<K,V> balanceLeft (K key, V value, Tree<K, V> tl, Tree<K, V> tr) {
        if (isRedTree (tl)) {
            return new RedTree<> (key, value, tl.black(), tr);
        }
        if (isBlackTree (tr)) {
            return balance (key, value, tl, tr.red ());
        }
        if (isRedTree (tr) && isBlackTree (tl)) {
            return new RedTree<> (tr.left.key, tr.left.value, new BlackTree<> (key, value, tl, tr.left.left), balance (tr.key, tr.value, tr.left.right, delSubl (tr.right)));
        }
        throw new IllegalStateException ("invariant violation");
    }

    static <K,V> Tree<K,V> balanceRight (K key, V value, Tree<K, V> tl, Tree<K, V> tr) {
        if (isRedTree (tr)) {
            return new RedTree<> (key, value, tl, tr.black());
        }
        if (isBlackTree (tl)) {
            return balance (key, value, tl.red (), tr);
        }
        if (isRedTree (tl) && isBlackTree (tr)) {
            return new RedTree<> (tl.right.key, tl.right.value, balance (tl.key, tl.value, delSubl (tl.left), tl.right.left), new BlackTree <> (key, value, tl.right.right, tr));
        }
        throw new IllegalStateException ("invariant violation");
    }

    static <K,V> Tree<K,V> delRight (Tree<K,V> tree, K key, Comparator<K> comparator) {
        if (isBlackTree (tree.right)) {
            return balanceRight (tree.key, tree.value, tree.left, del (tree.right, key, comparator));
        }
        else {
            return new RedTree<> (tree.key, tree.value, tree.left, del (tree.right, key, comparator));
        }
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



    static abstract class Tree<K,V> {
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

        abstract Tree<K,V> red();
        abstract Tree<K,V> black();
    }

    static class BlackTree<K,V> extends Tree<K,V> {
        public BlackTree (K key, V value, Tree<K, V> left, Tree<K, V> right) {
            super(key, value, left, right);
        }

        @Override Tree<K, V> red () {
            return new RedTree<> (key, value, left, right);
        }
        @Override Tree<K, V> black () {
            return this;
        }
    }

    static class RedTree<K,V> extends Tree<K,V> {
        public RedTree (K key, V value, Tree<K, V> left, Tree<K, V> right) {
            super (key, value, left, right);
        }

        @Override Tree<K, V> red () {
            return this;
        }

        @Override Tree<K, V> black () {
            return new BlackTree<> (key, value, left, right);
        }
    }
}



