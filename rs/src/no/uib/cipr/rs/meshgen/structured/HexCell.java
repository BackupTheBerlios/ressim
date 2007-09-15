package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

/**
 * Hexahedral cell class.
 */
public class HexCell {

    private QuadFace top, bottom, front, back, left, right;

    private TrilinearMap trilinear;

    public HexCell(Geometry geometry, int[] cp) {
        this(geometry.getPoint(cp[0]), geometry.getPoint(cp[1]), geometry
                .getPoint(cp[2]), geometry.getPoint(cp[3]), geometry
                .getPoint(cp[4]), geometry.getPoint(cp[5]), geometry
                .getPoint(cp[6]), geometry.getPoint(cp[7]));
    }

    /**
     * Creates the hex cell. Follows right-hand rule ordering.
     */
    public HexCell(Point3D point0, Point3D point1, Point3D point2,
            Point3D point3, Point3D point4, Point3D point5, Point3D point6,
            Point3D point7) {

        // converting from right-hand rule ordering to volume-area calculation
        // ordering.
        Point3D p0 = point0;
        Point3D p1 = point1;
        Point3D p2 = point3;
        Point3D p3 = point2;
        Point3D p4 = point4;
        Point3D p5 = point5;
        Point3D p6 = point7;
        Point3D p7 = point6;

        trilinear = new TrilinearMap(p0, p1, p2, p3, p4, p5, p6, p7);

        top = new QuadFace(p0, p1, p2, p3, Orientation.TOP);
        bottom = new QuadFace(p4, p5, p6, p7, Orientation.BOTTOM);

        front = new QuadFace(p0, p1, p4, p5, Orientation.FRONT);
        back = new QuadFace(p2, p3, p6, p7, Orientation.BACK);

        left = new QuadFace(p0, p2, p4, p6, Orientation.LEFT);
        right = new QuadFace(p1, p3, p5, p7, Orientation.RIGHT);
    }

    /**
     * Returns the cell volume
     */
    public double getVolume() {
        return trilinear.getVolume();
    }

    /**
     * Returns the normal of the given cell interface
     * 
     * TODO implement/check sign reversals to get outward pointing normals.
     */
    public QuadFace getFace(Orientation orient) {
        if (orient == Orientation.TOP)
            return top;

        if (orient == Orientation.BOTTOM)
            return bottom;

        if (orient == Orientation.FRONT)
            return front;

        if (orient == Orientation.BACK)
            return back;

        if (orient == Orientation.LEFT)
            return left;

        if (orient == Orientation.RIGHT)
            return right;

        return null;
    }

    /**
     * Returns the cell center coordinates
     */
    public Point3D getCenterPoint() {
        return trilinear.getPoint(0.5, 0.5, 0.5);
    }

    public static class QuadFace {
        private BilinearMap bilinear;

        private double scale;

        /**
         * @param p0
         * @param p1
         * @param p2
         * @param p3
         */
        public QuadFace(Point3D p0, Point3D p1, Point3D p2, Point3D p3,
                Orientation orientation) {

            bilinear = new BilinearMap(p0, p1, p2, p3);

            scale = 1.0;

            if (orientation == Orientation.BOTTOM
                    || orientation == Orientation.FRONT
                    || orientation == Orientation.RIGHT)
                scale = -1.0;
        }

        /**
         * Returns the coordinate of a point on the bilinear surface defined by
         * this quadrilateral. The input are the coordinates (a, b) in the
         * surface parameterization. The parameterization specifies the
         * following correspondance between local coordinates and corner points:
         * (0,0)->p0 (1,0)->p1, (0,1)->p2 and (1,1)->p3.
         * 
         * @param a
         *            Local x-direction coordinate, 0 <= a <= 1.
         * @param b
         *            Local y-direction coordinate, 0 <= b <=1.
         * @return a point on the bilinear surface
         * 
         * @throws IllegalArgumentException
         *             if (a,b) is not within legal range
         */
        public Point3D getPoint(double a, double b)
                throws IllegalArgumentException {
            if (a < 0 || a > 1)
                throw new IllegalArgumentException("'a' must be in [0,1]");
            if (b < 0 || b > 1)
                throw new IllegalArgumentException("'b' must be in [0,1]");

            return bilinear.getPoint(a, b);
        }

        /**
         * Returns the center point of this quadrilateral.
         */
        public Point3D getCenterPoint() {
            return getPoint(.5, .5);
        }

        /**
         * Returns the quadrilateral area
         */
        public double getArea() {
            return bilinear.area;
        }

        /**
         * Returns the normal vector in barycenter
         */
        public Vector3D getNormal() {
            return bilinear.normal.mult(scale);
        }

    }

}
