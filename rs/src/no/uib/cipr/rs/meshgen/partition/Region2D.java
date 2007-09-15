package no.uib.cipr.rs.meshgen.partition;

import java.util.Arrays;
import java.util.Set;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * A closed rectangular region in 2D defined by oriented line segments.
 */
class Region2D {

    private OrientedSegment3D[] orientedSegments;

    public Region2D(Point3D p0, Point3D p1, Point3D p2, Point3D p3) {
        OrientedSegment3D front = new OrientedSegment3D(p0, p1,
                Orientation.FRONT);
        OrientedSegment3D back = new OrientedSegment3D(p2, p3,
                Orientation.BACK);
        OrientedSegment3D left = new OrientedSegment3D(p3, p0,
                Orientation.LEFT);
        OrientedSegment3D right = new OrientedSegment3D(p1, p2,
                Orientation.RIGHT);

        orientedSegments = new OrientedSegment3D[] { front, right, back,
                left };
    }

    /**
     * Returns true if the given point is within this region.
     */
    public boolean pointInRegion(Point3D point) {
        Point3D v1 = null;
        Point3D v2 = null;

        // winding number
        int wn = 0;

        for (Segment3D segment : orientedSegments) {
            v1 = segment.getFirst();
            v2 = segment.getSecond();

            if (v1.y() <= point.y()) {
                if (v2.y() > point.y())
                    if (isLeft(v1, v2, point) > 0.0)
                        ++wn;
            } else {
                if (v2.y() <= point.y())
                    if (isLeft(v1, v2, point) < 0.0)
                        --wn;
            }
        }

        return (wn == 0 ? false : true);
    }

    /**
     * Returns the segment that is closest to the given point.
     */
    public OrientedSegment3D getClosestSegment(Point3D point) {
        OrientedSegment3D segment = null;

        double distance = Double.MAX_VALUE;

        for (OrientedSegment3D s : orientedSegments) {
            double d = s.distanceToPoint(point);

            if (d < distance) {
                segment = s;
                distance = d;
            }
        }
        return segment;
    }

    /**
     * Returns a value indicating if the given test point is left, on or
     * right of an infinite line defined by the first two given points.
     * 
     * @param p0
     *            first vertex, v1
     * @param p1
     *            second vertex, v2
     * @param p2
     *            test point, p
     * @return zero if p is on the line through v1 and v2, otherwise, the
     *         signed distance indicating left or right is returned.
     */
    private double isLeft(Point3D p0, Point3D p1, Point3D p2) {
        double res = ((p1.x() - p0.x()) * (p2.y() - p0.y()) - (p2.x() - p0
                .x())
                * (p1.y() - p0.y()));

        return res;
    }

    /**
     * Enumerate the boundaries for this region. 
     * 
     * @param set
     *  Set to which the boundaries will be added.
     * @return
     *  The same set that was passed as an input, which enables piping
     *  of the set.
     */
    Set<Segment3D> boundaries(Set<Segment3D> set) {
        set.addAll(Arrays.asList(orientedSegments));
        return set;
    }
}