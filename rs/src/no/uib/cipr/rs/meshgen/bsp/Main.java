package no.uib.cipr.rs.meshgen.bsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

public class Main {

    /**
     * Testing routine for BSP.
     */
    public static void main(String[] args) {
        List<IndexedPoint3D> p = getPoints();

        Polygon3D p1 = getPolygon1(p);
        Polygon3D p2 = getPolygon2(p);

        Polygon3D p3 = p1.getIntersection(p2);

        System.out.println("Overlap: " + p3.toString());
    }

    private static Polygon3D getPolygon1(List<IndexedPoint3D> p) {
        List<Edge3D> e1 = new ArrayList<Edge3D>();
        e1.add(new Edge3D(p.get(3), p.get(4)));
        e1.add(new Edge3D(p.get(4), p.get(1)));
        e1.add(new Edge3D(p.get(1), p.get(0)));
        e1.add(new Edge3D(p.get(0), p.get(3)));

        return new Polygon3D(e1);
    }

    private static Polygon3D getPolygon2(List<IndexedPoint3D> p) {
        List<Edge3D> e2 = new ArrayList<Edge3D>();
        e2.add(new Edge3D(p.get(4), p.get(5)));
        e2.add(new Edge3D(p.get(5), p.get(2)));
        e2.add(new Edge3D(p.get(2), p.get(1)));
        e2.add(new Edge3D(p.get(1), p.get(4)));

        return new Polygon3D(e2);
    }

    private static List<IndexedPoint3D> getPoints() {
        List<IndexedPoint3D> points = new ArrayList<IndexedPoint3D>(6);
        for (int j = 0; j <= 2; j++) {
            points.add(new IndexedPoint3D(0, j, 0));
            points.add(new IndexedPoint3D(0, j, -1));
        }
        Collections.sort(points);
        return points;
    }

}
