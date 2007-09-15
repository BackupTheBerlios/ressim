package no.uib.cipr.rs.util;

import java.lang.Comparable;

/**
 * Abstraction for a pair of two objects. The pair in itself is immutable but
 * its individual components are not. Thus, you cannot change of which objects
 * the pair is constructed, but you can change the objects (if they are not
 * immutable themselves).
 * 
 * Using a pair is often a handy way to return more than one value from a
 * function. (If both objects are of the same type, you can also consider using
 * an array).
 * 
 * A pair is partially ordered if both the generic type arguments are. However,
 * this is not tested by the compiler. Any typing errors will be reported at
 * runtime.
 */
public class Pair<X, Y> implements Comparable<Pair<X, Y>> {
    private X x;

    private Y y;

    /**
     * Construct the pair from each of its components.
     * 
     * @param x
     *                Reference to object to fill in the first
     * @param y
     */
    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor. Create a new pair from an existing pair. Note that the
     * pair
     * 
     * @param p
     *                Pair that should be copied. The elements of the old pair
     *                will be copied so that the same point
     */
    public Pair(Pair<? extends X, ? extends Y> p) {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * Observer methods for each of the fields in the pair. You can create
     * subclasses with more domain-specific names to make the code more
     * readable.
     */
    public X x() {
        return this.x;
    }

    public Y y() {
        return this.y;
    }

    /**
     * Generate a hash code that enables quick narrowing of the search for a
     * specific pair; this method maps from the domain <X,Y>-&gt;Integer. In its
     * nature, more than one input maps to the same output.
     */
    @Override
    public int hashCode() {
        int xHash = this.x.hashCode();
        int yHash = this.y.hashCode();

        // exclusive-or is the sum of two numbers without considering
        // carry from one bit to the other, and is a good general way
        // to combine two arbitrary numbers.
        int hash = xHash ^ yHash;
        return hash;
    }

    /**
     * Compare a pair with another pair. The comparison is deep, i.e. both
     * components must be equivalent to the corresponding components in the
     * other pair.
     * 
     * @param theOther
     *                Pair to which we should be compared. If another object
     *                than a pair is passed, it is considered inequal.
     * @return True if the other pair is considered equal, false if not.
     */
    @Override
    public boolean equals(Object theOther) {
        // quick test; if it is the same reference, then the object must
        // be equal; the relation is always reflexive
        if (this == theOther) {
            return true;
        }

        // existientiality test; since this is an object, the other must
        // also be one -- null is never the same as anything but itself
        if (null == theOther) {
            return false;
        }

        // equivalence relation covers all kind of pairs; we are allowed
        // to subclass the pair in ways that do not affect the relation
        if (!(theOther instanceof Pair)) {
            return false;
        }

        // note that the member is not cast to the generic type when
        // introspecting the other object for its parts.
        boolean xEquals = this.x.equals(((Pair<?, ?>) theOther).x);
        boolean yEquals = this.y.equals(((Pair<?, ?>) theOther).y);

        // we consider the pair to be equal if both parts are.
        return xEquals && yEquals;
    }

    /**
     * Order two partially to eachother. The order should be reflexive,
     * assymmetric and transitive. It is consistent with hashCode() and equals()
     * (if the two generic arguments are).
     * 
     * @param theOther
     *                Another pair to which this one is compared.
     * @return Negative if this object is less-than the other, zero if they are
     *         equivalent and positive if this object is greater-than the other.
     */
    @SuppressWarnings("unchecked")
    public int compareTo(Pair<X, Y> theOther) {
        // compare the first component, and if it is equal, then compare
        // the other. the magnitude of the result is considered irrelevant.
        int dx = ((Comparable<X>) this.x).compareTo(theOther.x);
        if (dx != 0) {
            return dx;
        }

        // do the same comparison for the second component
        int dy = ((Comparable<Y>) this.y).compareTo(theOther.y);
        if (dy != 0) {
            return dy;
        }

        // if both components were equal, then the pair is too
        return 0;
    }

    /**
     * Textual description of the pair using the description of its components.
     * (If the string from one of the members contains a comma, the output may
     * be confusing to read).
     */
    @Override
    public String toString() {
        String sx = x.toString();
        String sy = y.toString();
        String s = String.format("(%s,%s)", sx, sy);
        return s;
    }
}
