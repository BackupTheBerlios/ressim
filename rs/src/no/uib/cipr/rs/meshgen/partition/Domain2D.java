package no.uib.cipr.rs.meshgen.partition;

import java.util.Set;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * A rectangular domain in 2D consisting of an inner region and a border ring.
 */
public class Domain2D {

    // geometrical representation of the whole domain.
    private Region2D domain;

    // geometrical representation of the inner part of this domain
    private Region2D inner;

    // index of this domain
    private int index;

    // the geometrical dimensions of this domain
    private double x0, x1, y0, y1;

    /**
     * Sets up the 2D domain with the four given corner points, border region
     * width in meter and index. It is assumed that the corner points are given
     * in the following order: (i, j, k), (i+1, j, k), (i+1, j+1, k), (i,
     * j+1,k). This is used to compute the dimensions of this domain.
     */
    public Domain2D(Point3D p0, Point3D p1, Point3D p2, Point3D p3, double b,
            int index) {
        this.index = index;

        x0 = p0.x();
        y0 = p0.y();
        x1 = p2.x();
        y1 = p2.y();

        inner = new Region2D(p0, p1, p2, p3);

        domain = new Region2D(p0.plus(new Point3D(-b, -b, 0)), p1
                .plus(new Point3D(b, -b, 0)), p2.plus(new Point3D(b, b, 0)), p3
                .plus(new Point3D(-b, b, 0)));
    }

    /**
     * Returns true if the given point is within this domain
     */
    public boolean pointInDomain(Point3D point) {
        return domain.pointInRegion(point);
    }

    /**
     * Returns true if the given point is within the interior of this domain.
     */
    public boolean pointInInner(Point3D point) {
        return inner.pointInRegion(point);
    }

    /**
     * Returns true if the given point is within the border ring of this domain.
     */
    public boolean pointInBorderRing(Point3D point) {
        return (domain.pointInRegion(point) && !inner.pointInRegion(point));
    }

    /**
     * Returns the index of this domain.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the orientation of the domain boundary that the given point is
     * closest to.
     */
    public Orientation getClosestBoundary(Point3D point) {
        return domain.getClosestSegment(point).getOrientation();
    }

    /**
     * Returns the orientation of the inner/border ring boundary that the given
     * point is closest to.
     */
    public Orientation getClosestInner(Point3D point) {
        return inner.getClosestSegment(point).getOrientation();
    }

    /**
     * Returns an array of the diagonal corner point coordinates (x0, y0, x1,
     * y1) of the inner domain.
     */
    public double[] getDimensions() {
        return new double[] { x0, y0, x1, y1 };
    }

    /**
     * Enumerate the boundaries of the domain.
     * 
     * @param set
     *  Set to which the boundaries are added.
     * @return
     *  The same set that was passed as a parameter.
     */
    Set<Segment3D> boundaries(Set<Segment3D> set) {
        // the boundaries of the domain is the boundaries of the inner
        // region, i.e. the (overlapping) border is not considered a
        // proper part of the domain.
        return inner.boundaries(set);
    }    
}
