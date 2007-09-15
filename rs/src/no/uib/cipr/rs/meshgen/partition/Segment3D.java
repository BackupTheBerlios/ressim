package no.uib.cipr.rs.meshgen.partition;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

/**
 * A line segment in 3D defined by two points.
 */
public class Segment3D {

    protected Point3D p0;

    protected Point3D p1;

    /**
     * Creates a line segment in 3D from the given start- and end-points
     * 
     * @param p0
     *            Start-point of segment
     * @param p1
     *            End-point of segment
     */
    public Segment3D(Point3D p0, Point3D p1) {
        this.p0 = p0;
        this.p1 = p1;
    }

    /**
     * Returns the start-point of this segment.
     */
    public Point3D getFirst() {
        return p0;
    }

    /**
     * Returns the end-point of this segment.
     */
    public Point3D getSecond() {
        return p1;
    }

    /**
     * Returns the Euclidian distance between this segment and the given point.
     */
    public double distanceToPoint(Point3D point) {
        Vector3D v = new Vector3D(p0, p1); // p1 - p0
        Vector3D w = new Vector3D(p0, point); // p - p0

        double c1 = v.dot(w);

        if (c1 <= 0.0)
            return Math.sqrt(w.dot(w));

        double c2 = v.dot(v);
        if (c2 <= c1) {
            Vector3D tmp = new Vector3D(p1, point);
            return Math.sqrt(tmp.dot(tmp));
        }

        double b = c1 / c2;
        Point3D pb = new Point3D(p0, v.mult(b));

        Vector3D tmp = new Vector3D(pb, point);
        return Math.sqrt(tmp.dot(tmp));
    }

    /**
     * Map a segment into the domain of natural numbers in such a way
     * that the likelyhood of getting unique numbers are high.
     */
    @Override
    public int hashCode() {
        // get the hash code for each of the components of the segment
        int h0 = p0.hashCode();
        int h1 = p1.hashCode();
        
        // combine the hash codes; note that a feature of this method
        // is that two segments with just opposite direction will have
        // the same hash code (since the order of the operands does not
        // matter for the XOR operator)
        int h  = h0 ^ h1;
        
        // return the hash code for the composite
        return h;
    }
    
    /**
     * Test two segments for equality. The test only considers if the
     * two segments covers the same line between two points, not the
     * orientation of the line. (This coincidentiality is used when
     * enumerating the border of regions).
     * 
     * See the section titled "instanceof versus getClass in equals Methods"
     * at http://www.artima.com/intv/blochP.html for a discussion.
     */
    @Override
    public boolean equals(Object o) {
        // existenciality test
        if (this == null) {
            return false;
        }
        
        // quick test for reflexitivity
        if (this == o) {
            return true;
        }
        
        // if we are comparing against some other type disjoint from
        // our, then we cannot be equal. we can however be equal to
        // another object that is something more than just a segment,
        // if we consider just the segment properties (note that this
        // may cause x.equals(y) while not y.equals(x) if we are
        // comparing different subclasses)
        if (!(o instanceof Segment3D)) {
            return false;
        }
        
        // the test above makes this cast safe
        Segment3D that = (Segment3D) o;
        
        // test each of the individual parts, either correspondingly (c)
        // or diagonally (d)
        boolean c0 = this.p0.equals(that.p0);
        boolean c1 = this.p1.equals(that.p1);

        boolean d0 = this.p0.equals(that.p1);
        boolean d1 = this.p1.equals(that.p0);
        
        // combine the results; both parts must be equal, either in the
        // same or in the opposite direction
        boolean e  = ( c0 && c1 ) || ( d0 && d1 );       
        return e;
    }
    
    /**
     * String representation of the segment for debugging purposes.
     */
    @Override
    public String toString() {
        return String.format("(%s-%s)", p0.toString(), p1.toString());
    }
}