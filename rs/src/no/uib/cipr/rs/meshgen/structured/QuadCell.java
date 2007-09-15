package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

/**
 * A quadrilateral cell in 2D
 */
public class QuadCell {

    private BiFace front, back, left, right;

    private BilinearMap bilinear;

    public QuadCell(Geometry geometry, int[] cp) {
        this(geometry.getPoint(cp[0]), geometry.getPoint(cp[1]), geometry
                .getPoint(cp[2]), geometry.getPoint(cp[3]));
    }

    /**
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     */
    public QuadCell(Point3D p0, Point3D p1, Point3D p2, Point3D p3) {
        bilinear = new BilinearMap(p0, p1, p3, p2);

        front = new BiFace(p0, p1);
        back = new BiFace(p2, p3);
        left = new BiFace(p3, p0);
        right = new BiFace(p1, p2);
    }

    /**
     * Returns the center point of this cell.
     */
    public Point3D getCenterPoint() {
        return bilinear.getPoint(.5, .5);
    }

    public BiFace getFace(Orientation orientation) {
        switch (orientation) {
        case FRONT:
            return front;
        case BACK:
            return back;
        case LEFT:
            return left;
        case RIGHT:
            return right;
        default:
            throw new RuntimeException();
        }
    }

    /**
     * @return the area of this cell
     */
    public double getVolume() {
        return bilinear.area;
    }

    public static class BiFace {

        private Point3D p0, p1;

        private Vector3D n;

        public BiFace(Point3D p0, Point3D p1) {
            this.p0 = p0;
            this.p1 = p1;

            double x = p1.x() - p0.x();
            double y = p1.y() - p0.y();

            n = new Vector3D(y, -x, 0);
            n = n.mult(1 / n.norm2());
        }

        public double getArea() {
            Vector3D a = new Vector3D(p0, p1);
            return a.norm2();
        }

        public Vector3D getNormal() {
            return n;
        }

        public Point3D getCenterPoint() {
            return p0.plus(p1).scale(0.5);
        }

    }

}
