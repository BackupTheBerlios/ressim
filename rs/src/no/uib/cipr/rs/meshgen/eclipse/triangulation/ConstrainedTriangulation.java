package no.uib.cipr.rs.meshgen.eclipse.triangulation;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.eclipse.bsp.Polygon;

/**
 * This class generates a constrained triangulation of a closed polygonal region
 * in 2D.
 */
public class ConstrainedTriangulation {

    // store the resulting triangles
    private List<Triangle> triangles;

    /**
     * Constructs the triangulation
     * 
     * @param contour
     *            The polygon defining the region to be triangulated
     */
    public ConstrainedTriangulation(Polygon contour) {

        triangles = new ArrayList<Triangle>();

        generate(contour);

    }

    private void generate(Polygon contour) {
        List<Segment> segmentList = new ArrayList<Segment>();

        int i = 1; // TODO check if 0 is more appropriate
        int first, last;

        List<Point3D> vertices = contour.vertices();

        int npoints = vertices.size();

        first = i;
        last = first + npoints - 1;

        for (int j = 0; j < npoints; j++, i++) {
            // Point3D p = vertices.get(i);

            Segment si = new Segment();

            if (i == last) {
                // TODO
            }
            segmentList.add(si);
        }
    }

    /**
     * Gets the resulting triangles.
     */
    public List<Triangle> getTriangulation() {
        return triangles;
    }

    /**
     * A segment
     */
    private class Segment {
        @SuppressWarnings("unused")
        private Point3D v0, v1;

        @SuppressWarnings("unused")
        private boolean isInserted;

        int root0, root1;

        int next, prev;
    }

}
