package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;
import no.uib.cipr.rs.util.Tolerances;

/**
 * A bilinear map
 */
public class BilinearMap {

    private Point3D p0, p1, p2, p3;

    double area;

    Vector3D normal;

    /**
     * Creates a bilinear map
     * 
     * @param p0
     *            Physical space point (0,0) ("lower-left")
     * @param p1
     *            Physical space point (1,0) ("lower-right")
     * @param p2
     *            Physical space point (0,1) ("upper-left")
     * @param p3
     *            Physical space point (1,1) ("upper-right")
     */
    public BilinearMap(Point3D p0, Point3D p1, Point3D p2, Point3D p3) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;

        computeAreaNormal();
    }

    public BilinearMap(IndexedPoint3D[] p) {
        this(p[0], p[1], p[2], p[3]);
    }

    /**
     * Returns a point in physical space defined by the map for the given
     * logical space x- and y-coordinates
     */
    public Point3D getPoint(double a, double b) {
        double k0 = (1 - a) * (1 - b);
        double k1 = a * (1 - b);
        double k2 = (1 - a) * b;
        double k3 = a * b;

        double x = k0 * p0.x() + k1 * p1.x() + k2 * p2.x() + k3 * p3.x();
        double y = k0 * p0.y() + k1 * p1.y() + k2 * p2.y() + k3 * p3.y();
        double z = k0 * p0.z() + k1 * p1.z() + k2 * p2.z() + k3 * p3.z();

        return new Point3D(x, y, z);
    }

    public Point3D getCenterPoint() {
        return getPoint(0.5, 0.5);
    }

    /**
     * Returns The area
     */
    public double getArea() {
        return area;
    }

    /**
     * Returns the unit normal vector in barycenter
     */
    public Vector3D getNormal() {
        return normal;
    }

    private void computeAreaNormal() {
        Vector3D x1 = new Vector3D(p0);
        Vector3D x2 = new Vector3D(p1);
        Vector3D x3 = new Vector3D(p2);
        Vector3D x4 = new Vector3D(p3);

        // compute normal with length equal to surface area
        Vector3D a = (x2.minus(x1)).cross(x3.minus(x1));
        Vector3D b = (x2.minus(x1)).cross(x4.minus(x2));
        Vector3D c = (x4.minus(x3)).cross(x3.minus(x1));
        Vector3D d = (x4.minus(x3)).cross(x4.minus(x2));

        Vector3D n = a.plus(b.plus(c.plus(d))).mult(.25);

        double length = n.norm2();

        area = length;

        if (length < Tolerances.smallEps) {
            System.err.println("Warning: zero length normal vector");
            normal = new Vector3D(0, 0, 0);
        } else
            normal = n.mult(1.0 / length);
    }
}