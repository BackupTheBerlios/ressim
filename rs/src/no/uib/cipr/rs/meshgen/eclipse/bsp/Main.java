package no.uib.cipr.rs.meshgen.eclipse.bsp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;

/**
 * Driver class for BSP-implementation.
 */
public class Main {

    /**
     * Holds all log-info
     */
    private final static Logger log = Logger.getLogger(Main.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {

        log.info("BinarySpacePartition driver");

        // 3D plane quadrilaterals (se Matlab/bsp-testing.mat)

        // pillars
        Point3D c1 = new Point3D(0, 0, 0);
        Point3D c2 = new Point3D(0, 0, 1);
        Point3D c11 = new Point3D(0, 1, 0);
        Point3D c22 = new Point3D(0, 1, 1);

        Pillar p1 = new Pillar(c1, c2);
        Pillar p2 = new Pillar(c11, c22);

        // left surface corner points
        CornerPoint3D[] cp1 = new CornerPoint3D[] {
                new CornerPoint3D(p1.getPoint(0), 0),
                new CornerPoint3D(p1.getPoint(0.4), 1),
                new CornerPoint3D(p2.getPoint(0.6), 2),
                new CornerPoint3D(p2.getPoint(0), 3) };

        // top right surface corner points
        CornerPoint3D[] cp2 = new CornerPoint3D[] {
                new CornerPoint3D(p1.getPoint(0.5), 4),
                new CornerPoint3D(p1.getPoint(1), 5),
                new CornerPoint3D(p2.getPoint(1), 6),
                new CornerPoint3D(p2.getPoint(0.3), 7) };

        // bottom right surface corner points
        CornerPoint3D[] cp3 = new CornerPoint3D[] {
                new CornerPoint3D(p1.getPoint(0), 8),
                new CornerPoint3D(p1.getPoint(0.5), 9),
                new CornerPoint3D(p2.getPoint(0.3), 10),
                new CornerPoint3D(p2.getPoint(0), 11) };

        int edgeIndex = 0;

        List<Edge> e1 = new ArrayList<Edge>();
        for (int i = 0; i < cp1.length; i++) {
            e1.add(new Edge3D(cp1[i], cp1[(i + 1) % cp1.length], edgeIndex++));
        }
        List<Edge> e2 = new ArrayList<Edge>();
        for (int i = 0; i < cp2.length; i++) {
            e2.add(new Edge3D(cp2[i], cp2[(i + 1) % cp2.length], edgeIndex++));
        }
        List<Edge> e3 = new ArrayList<Edge>();
        for (int i = 0; i < cp3.length; i++) {
            e3.add(new Edge3D(cp3[i], cp3[(i + 1) % cp3.length], edgeIndex++));
        }

        Polygon q1 = new Polygon3D(e1);
        Polygon q2 = new Polygon3D(e2);
        Polygon q3 = new Polygon3D(e3);

        Polygon q12 = q1.getIntersection(q2);
        reportIntersection(q12);

        Polygon q13 = q1.getIntersection(q3);
        reportIntersection(q13);

        // check computation of common plane

        log.info("Best fit plane computation");

        List<Point3D> points = new ArrayList<Point3D>();
        points.add(new Point3D(0, 0, 0));
        points.add(new Point3D(1, 0, 0));
        points.add(new Point3D(0, 1, 0));
        points.add(new Point3D(1, 1, 0.1));

        double a = 0;
        double b = 0;
        double c = 1;

        Vector3D normal = new Vector3D(a, b, c);

        double d = getBestFit(normal, points);

        Plane3D plane = new Plane3D(a, b, c, d);

        log.info("Best fit plane: " + plane.toString());

    }

    private static double getBestFit(Vector3D normal, List<Point3D> points) {

        double a = normal.x();
        double b = normal.y();
        double c = normal.z();

        double m = points.size();

        double d = 0;

        for (int i = 0; i < m; i++) {
            Point3D p = points.get(i);
            d += (a * p.x() + b * p.y() + c * p.z());
        }
        d = d / m;
        return d;
    }

    private static void reportIntersection(Polygon polygon) {

        if (polygon == null)
            log.severe("no intersection found");
        else {
            log.info("");
            log.info("found intersection " + polygon.toString());

            for (Edge e : polygon.getEdges()) {
                CornerPoint3D begin = e.getBeginPoint();
                CornerPoint3D end = e.getEndPoint();

                if (begin.isEdgeIntersection()) {
                    log.info("intersection point: " + begin.toString()
                            + " edges: " + begin.getIJ().toString());
                }
                if (end.isEdgeIntersection()) {
                    log.info("intersection point: " + end.toString()
                            + " edges: " + end.getIJ().toString());
                }

            }
        }

    }
}
