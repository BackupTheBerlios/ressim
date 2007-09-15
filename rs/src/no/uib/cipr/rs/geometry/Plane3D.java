package no.uib.cipr.rs.geometry;

import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * This class represents a 3D plane
 */
public class Plane3D {

    // unit normal vector
    Vector3D normal;

    IndexedPoint3D origin;

    private double a, b, c, d;

    /**
     * Creates a plane with the given normal vector through the specified point.
     */
    public Plane3D(Vector3D normal, IndexedPoint3D origin) {
        this.normal = normal;
        this.origin = origin;

        // compute plane equation coefficients
        a = normal.x();
        b = normal.y();
        c = normal.z();
        d = -(a * origin.x() + b * origin.y() + c * origin.z());
    }

    /**
     * Creates a plane with the given plane equation coefficients.
     */
    public Plane3D(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;

        normal = new Vector3D(a, b, c);
        origin = new IndexedPoint3D(0, 0, d / c);
    }

    /**
     * Returns the point resulting from projecting the given point onto this
     * plane.
     */
    public Point3D getProjectedPoint(Point3D point) {
        Vector3D v = new Vector3D(origin, point);

        double s = normal.dot(v);
        double t = normal.dot(normal);

        double x, y, z;

        if (t != 0.0) {
            x = point.x() - (s / t) * normal.x();
            y = point.y() - (s / t) * normal.y();
            z = point.z() - (s / t) * normal.z();
        } else {
            x = point.x();
            y = point.y();
            z = point.z();
        }
        return new Point3D(x, y, z);
    }

    /**
     * Returns the normal of this plane.
     */
    public final Vector3D getNormal() {
        return normal;
    }

    /**
     * Returns the origin of this plane.
     */
    public final Point3D getOrigin() {
        return origin;
    }

    /**
     * Returns the signed distance from the given point to this plane.
     */
    public double getPointDistance(Point3D point) {
        return a * point.x() + b * point.y() + c * point.z() + d;
    }

    @Override
    public String toString() {
        return a + "x + " + b + "y + " + c + "z = " + d;
    }
}
