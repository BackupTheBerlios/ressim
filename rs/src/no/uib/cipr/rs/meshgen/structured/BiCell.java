package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

public class BiCell {

    private Point3D p0, p1;

    private MonoFace left, right;

    public BiCell(Geometry geometry, int[] cp) {
        this(geometry.getPoint(cp[0]), geometry.getPoint(cp[1]));
    }

    public BiCell(Point3D p0, Point3D p1) {
        this.p0 = p0;
        this.p1 = p1;

        left = new MonoFace(p0, Orientation.LEFT);
        right = new MonoFace(p1, Orientation.RIGHT);
    }

    public double getVolume() {
        return Math.abs(p1.x() - p0.x());
    }

    public Point3D getCenterPoint() {
        return p0.plus(p1).scale(0.5);
    }

    public MonoFace getFace(Orientation orientation) {
        switch (orientation) {
        case LEFT:
            return left;
        case RIGHT:
            return right;
        default:
            throw new RuntimeException();
        }
    }

    public static class MonoFace {
        private Point3D p;

        private Vector3D n;

        public MonoFace(Point3D p, Orientation orientation) {
            this.p = p;

            switch (orientation) {
            case LEFT:
                n = new Vector3D(-1, 0, 0);
                break;
            case RIGHT:
                n = new Vector3D(1, 0, 0);
                break;
            default:
                throw new RuntimeException();
            }
        }

        public double getArea() {
            return 1;
        }

        public Vector3D getNormal() {
            return n;
        }

        public Point3D getCenterPoint() {
            return p;
        }

    }
}
