package no.uib.cipr.rs.util;

/**
 * Pair of two values organized such that it will be equal to the same pair of
 * the same two values, i.e. it doesn't matter in which order the two component
 * were specified. Use this type of pair to identify edges between two (indexed)
 * vertices.
 * 
 * @author roland.kaufmann@cipr.uib.no
 * @param <T>
 *            Type of the components in the pair
 */
public class OrderedPair<T extends Comparable<T>> extends Pair<T, T> {
    /**
     * Initialize the pair from each its components.
     * 
     * @param t
     *            A value to put in the pair. This may not necessarily end up as
     *            the first component in the pair.
     * @param u
     *            The other of the values that should end up in the pair, though
     *            not necessarily as the second component.
     */
    public OrderedPair(T t, T u) {
        super(sort(t, u));
    }

    /**
     * Copy constructor. Initialize this pair from another existing pair. Even
     * if you already had an ordered pair, the comparator will be invoked once
     * again.
     * 
     * @param p
     *            Pair containing the values to be ordered.
     */
    public OrderedPair(Pair<? extends T, ? extends T> p) {
        this(p.x(), p.y());
    }

    /**
     * Helper method that returns a pair with the arguments sorted according to
     * their partial order.
     */
    private static <T extends Comparable<T>> Pair<T, T> sort(T t, T u) {
        // if they were given in correct order already, then return them
        // as they were, otherwise swap them. If they are considered
        // equal, then noone can distinguish them (according to the
        // equivalence relation anyway), and it doesn't matter in which
        // order they are returned.
        if (t.compareTo(u) <= 0) {
            return new Pair<T, T>(t, u);
        } else {
            return new Pair<T, T>(u, t);
        }
    }
}
