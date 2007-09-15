package no.uib.cipr.rs.meshgen.eclipse.bsp;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;

/**
 * This class represents a 3D plane
 */
public class Plane3D {

    // assumed to be unit normal vector
    Vector3D normal;

    CornerPoint3D origin;

    private double a, b, c, d;

    /**
     * Creates a plane with the given normal vector through the specified point.
     */
    public Plane3D(Vector3D normal, CornerPoint3D origin) {
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
        origin = new CornerPoint3D(0, 0, d / c);
    }

    /**
     * Computes the projection of point x onto the plane.
     * 
     * @param point
     *            A Point3D
     * @return The projected point
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

        Point3D projected = new Point3D(x, y, z);

        return projected;
    }

    /**
     * @return Returns the normal.
     */
    public final Vector3D getNormal() {
        return normal;
    }

    /**
     * @return Returns the origin.
     */
    public final Point3D getOrigin() {
        return origin;
    }

    /**
     * @param point
     * @return Returns the signed distance from this plane
     */
    public double getPointDistance(Point3D point) {
        return a * point.x() + b * point.y() + c * point.z() + d;
    }

    @Override
    public String toString() {
        return new String(a + "x + " + b + "y + " + c + "z = " + d);
    }
}
