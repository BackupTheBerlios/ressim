package no.uib.cipr.rs.geometry;

import java.io.Serializable;
import java.lang.Comparable; // compareTo
import java.lang.Double; // doubleToLongBits
import java.lang.Math; // signum
import junit.framework.TestCase;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Three-dimensional point. An equivalence relation is defined for the points
 * based on their spatial location.
 */
public class Point3D implements Serializable, Comparable<Point3D> {

    private static final long serialVersionUID = 122063903842907480L;

    /**
     * Coordinates of the point
     */
    protected double x, y, z;

    /**
     * Starting point for the coordinate system; all points may be seen as
     * vectors from this point.
     */
    public static final Point3D ORIGIN = new Point3D(0, 0, 0);

    /**
     * Creates a point in 3D
     */
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a point in 3D located at the end of vector v starting in point p.
     */
    public Point3D(Point3D p, Vector3D v) {
        this(p.x + v.x(), p.y + v.y(), p.z + v.z());
    }
    
    /**
     * Convenience method that let us write vector application as a pipe instead
     * of nested construction statements, i.e. instead of:
     * 
     * q = new Point3D(new Point3D(p, v), w);
     * 
     * which quickly can get messy, we can instead write:
     * 
     * q = t.apply(v).apply(w);
     */
    public Point3D apply(Vector3D v) {
        return new Point3D(this, v);
    }

    /**
     * Adds a point to this point
     */
    public Point3D plus(Point3D p) {
        return new Point3D(x + p.x, y + p.y, z + p.z);
    }
    
    /**
     * Calculate the barycenter of a "cloud" of points.
     * 
     * @param points
     *  Array containing points that should be summed/found the center for.
     *  This array is represented as a varargs list since in most cases we have
     *  a predefined, fixed number of points to add.
     * @return
     *  Center of all the points passed, or null if no points was passed.
     */
    public static Point3D center(Point3D... points) {
        if (points.length == 0) {
            return null;
        }
        else {
            // start out with a point containing "nothing"
            double x = 0, y = 0, z = 0;
            
            // add the mass for each of the points in the array
            for(Point3D p : points) {
                x += p.x;
                y += p.y;
                z += p.z;
            }
            
            // divide the mass on the count, to get the "average"
            x /= points.length;
            y /= points.length;
            z /= points.length;
            
            // return this point to the caller
            return new Point3D(x, y, z);
        }
    }
    
    /**
     * Scales this point
     */
    public Point3D scale(double a) {
        return new Point3D(x * a, y * a, z * a);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }
    
    /**
     * Number of dimensions stored in this point. Usable as upper bound for
     * loops that enumerates all the components in the point.
     */
    public static final int DIMENSIONS = 3;

    /**
     * Retrieve the point information adressed as an array. This method is
     * usable when scaling points in loops.
     * 
     * @param index
     *  Ordinal for the coordinate axis; which component of the point tuple.
     * @return
     *  Value for the coordinate along the specified dimension.
     */
    public double get(int index) {
        switch(index) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
            default:
                throw new IndexOutOfBoundsException(String.format(
                        "Expected index in range 0..2, but got %d", index));
        }
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    /**
     * Identify a point by a shorter code, suitable for fast lookup.
     * 
     * Since the point is immutable, then it is safe to use the hashcode in maps
     * to identify the object.
     * 
     * @return Integer code that represents a congruence of the object; if the
     *         hash code is not equal, then the objects are not equal either.
     */
    @Override
    public int hashCode() {
        // convert each of the members to their bitwise equivalent,
        // similar to reinterpret_cast<> in C++.
        long bitsX = Double.doubleToLongBits(x);
        long bitsY = Double.doubleToLongBits(y);
        long bitsZ = Double.doubleToLongBits(z);

        // combine all components into one bit string, so that the state
        // of the entire object is encapsulated into one bit-string.
        long bits = bitsX ^ bitsY ^ bitsZ;

        // a double has 64 bits precision whereas the hash code should
        // have only half of that. combine the two parts into one using
        // the same operator used to combine the various members, so we
        // get one with the appropriate length
        int lo = (int) (bits);
        int hi = (int) (bits >>> 32);

        // resulting hash code that identifies the current state
        return lo ^ hi;
    }

    /**
     * Partial ordering of point objects. The ordering is based on the spatial
     * location of the point. Only the 'point' aspects are considered,
     * attributes introduced in subclasses are not.
     * 
     * @param theOther
     *            Other object to which we want to compare ourselves.
     * @return Negative if this object is lesser than the other, zero if this
     *         object is considered to be equal and positive if it is greater.
     */
    public int compareTo(Point3D theOther) {
        // compare each member separately; if this is bigger (farther
        // from origin) than the other object, then the difference will
        // be positive. only if we are unable to decide (tie), use the
        // next component for comparison.
        // points that are too close for us to distinguish them will be
        // considered equal by this method.
        double dz = this.z - theOther.z;
        if (Math.abs(dz) > Tolerances.smallEps) {
            return (int) Math.signum(dz);
        }

        double dy = this.y - theOther.y;
        if (Math.abs(dy) > Tolerances.smallEps) {
            return (int) Math.signum(dy);
        }

        double dx = this.x - theOther.x;
        if (Math.abs(dx) > Tolerances.smallEps) {
            return (int) Math.signum(dx);
        }

        // if we have tested all the components without finding any
        // difference, then they are considered interchangable.
        return 0;
    }

    /**
     * Compare two points for equality, i.e. determine if two points are
     * referring to the same spatial location.
     * 
     * @param theOther
     *            Point to which we want to compare this one. Objects of other
     *            types than Point3D are always considered not equal.
     * 
     * @return True if the other object points to the same location (the two
     *         points may be used interchangably), false if not.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object theOther) {
        // existentiality test; since this is an instance method, then
        // we cannot be the null reference. by doing this first we
        // prevent NullReferenceExceptions later.
        if (theOther == null) {
            return false;
        }

        // quick test; the equality is always reflexive
        if (theOther == this) {
            return true;
        }

        // if the other object is of another type, then it cannot be the
        // same object (it will have different behavior). note that we
        // allow ourselves to be equal to points of a subtype (in contrast
        // with usual implementations that compare the getClass() of
        // this and the other object). this is because we want this
        // method to be consistent with compareTo() (we could write in
        // support for heterogenety in compareTo by comparing the hash-
        // code of the classes as well).
        if (!(theOther instanceof Point3D)) {
            return false;
        }

        // use the compare operator to figure out whether two points are
        // really equal or not. this operator should be consistent with
        // the hashCode function. notice that we cast to a subtype of
        // Point3D, which is really OK since everything after Point3D is
        // erased when passed to the compareTo anyway (we only check
        // against fields in compareTo)
        return compareTo((Point3D) theOther) == 0;
    }

    @SuppressWarnings("unused")
    private static class Test extends TestCase {
        public void testEqualsToSelf() {
            Point3D p = new Point3D(1., 1., 1.);
            assertEquals(p, p);
        }
    }
}