package no.uib.cipr.rs.meshgen.lgr;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.meshgen.bsp.Edge3D;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * Test class for face overlap computations
 */
public class Main {

    public static void main(String[] args) {
        System.out
                .println("Testing face overlap for faces with non-parallel normals");

        // face 1
        List<IndexedPoint3D> points1 = new ArrayList<IndexedPoint3D>();
        points1.add(new IndexedPoint3D(0, 0.5, 0, 0));
        points1.add(new IndexedPoint3D(1, 0.5, 0, 1));
        points1.add(new IndexedPoint3D(1, 0.5, 1, 2));
        points1.add(new IndexedPoint3D(0, 0.5, 1, 3));

        List<Edge3D> edges1 = new ArrayList<Edge3D>();
        edges1.add(new Edge3D(points1.get(0), points1.get(1)));
        edges1.add(new Edge3D(points1.get(1), points1.get(2)));
        edges1.add(new Edge3D(points1.get(2), points1.get(3)));
        edges1.add(new Edge3D(points1.get(3), points1.get(0)));

        Face face1 = new Face(edges1, Orientation.FRONT);

        // face 2
        List<IndexedPoint3D> points2 = new ArrayList<IndexedPoint3D>();
        points2.add(new IndexedPoint3D(1, 0, 0.5, 4));
        points2.add(new IndexedPoint3D(2, 0, 0.5, 5));
        points2.add(new IndexedPoint3D(2, 1, 0.5, 6));
        points2.add(new IndexedPoint3D(1, 1, 0.5, 7));

        List<Edge3D> edges2 = new ArrayList<Edge3D>();
        edges2.add(new Edge3D(points2.get(0), points2.get(1)));
        edges2.add(new Edge3D(points2.get(1), points2.get(2)));
        edges2.add(new Edge3D(points2.get(2), points2.get(3)));
        edges2.add(new Edge3D(points2.get(3), points2.get(0)));

        Face face2 = new Face(edges2, Orientation.BOTTOM);

        Face overlap12 = face1.getIntersection(face2);
        if (overlap12 == null)
            System.out.println("No overlap");
        else
            System.out.println("Overlap: " + overlap12.toString());

        // face 3
        List<IndexedPoint3D> points3 = new ArrayList<IndexedPoint3D>();
        points3.add(new IndexedPoint3D(1, 0, 0.5, 8));
        points3.add(new IndexedPoint3D(1, 0.0, 1, 9));
        points3.add(new IndexedPoint3D(1, 0.5, 1, 10));
        points3.add(new IndexedPoint3D(1, 0.5, 0.5, 11));

        List<Edge3D> edges3 = new ArrayList<Edge3D>();
        edges3.add(new Edge3D(points3.get(0), points3.get(1)));
        edges3.add(new Edge3D(points3.get(1), points3.get(2)));
        edges3.add(new Edge3D(points3.get(2), points3.get(3)));
        edges3.add(new Edge3D(points3.get(3), points3.get(0)));

        Face face3 = new Face(edges3, Orientation.BOTTOM);

        Face overlap13 = face1.getIntersection(face3);
        if (overlap13 == null)
            System.out.println("No overlap");
        else
            System.out.println("Overlap: " + overlap13.toString());

    }
}
