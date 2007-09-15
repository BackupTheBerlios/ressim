package no.uib.cipr.rs.meshgen.lgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * An edge that can be a polyline. Begin and end points of the edge are the
 * first and last points of the polyline. The points are all coordinate-sorted.
 * 
 * When this edge is used to build up interfaces or elements, the begin and end
 * points are the only ones contributing in volume and area computations.
 */
class FineEdge {

    /**
     * A list of all the points of this polyline edge.
     */
    private List<IndexedPoint3D> vertices;

    /**
     * Constructs the fine edge between the given two points. The vertices will
     * be sorted in coordinate order.
     */
    public FineEdge(List<IndexedPoint3D> vertices) {
        if (vertices.size() != 2)
            throw new IllegalArgumentException(
                    "Edge must be constructed from two vertices.");
        this.vertices = vertices;
        Collections.sort(this.vertices);
    }

    /**
     * Creates a fine edge from the two given end points. The given points are
     * sorted.
     */
    public FineEdge(IndexedPoint3D p0, IndexedPoint3D p1) {
        vertices = new ArrayList<IndexedPoint3D>(2);
        vertices.add(p0);
        vertices.add(p1);
        Collections.sort(vertices);
    }

    /**
     * Computes the geometrical overlap between this edge and the points along
     * the given coarse edge. The given coarse edge points must include this
     * edge's first and last points.
     * 
     * After successful completion, the set of points for this edge is altered.
     */
    public void computeEdgeOverlap(CoarseEdge coarse) {
        List<IndexedPoint3D> candidatePoints = coarse.getPoints();

        IndexedPoint3D first = vertices.get(0);
        IndexedPoint3D last = vertices.get(vertices.size() - 1);

        if (!candidatePoints.contains(first) || !candidatePoints.contains(last))
            throw new IllegalArgumentException(
                    "Fine edge points must be contained in coarse edge");

        int i = candidatePoints.indexOf(first);
        int j = candidatePoints.lastIndexOf(last);

        List<IndexedPoint3D> p = new ArrayList<IndexedPoint3D>(j - i + 1);

        for (int l = i; l <= j; l++)
            p.add(candidatePoints.get(l));
        vertices = p;

        Collections.sort(vertices);
    }

    /**
     * Returns an iterator over the sorted points of this polyline edge.
     */
    public List<IndexedPoint3D> vertices() {
        return vertices;
    }

    /**
     * Returns the begin point of this edge.
     */
    public IndexedPoint3D getBeginPoint() {
        return vertices.get(0);
    }

    /**
     * Returns the end point of this edge.
     */
    public IndexedPoint3D getEndPoint() {
        return vertices.get(vertices.size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (IndexedPoint3D p : vertices)
            s.append(p.toString() + " ");

        return s.toString();
    }

    /**
     * Adds the given point to this edge. If the point is already in the point
     * list, it is not added.
     * 
     * This method is used to add new points from overlap computations to edges
     * belonging to faces that are connected to, but have different orientation
     * than the faces used in the overlap computation.
     * 
     */
    public void addPoint(IndexedPoint3D p) {
        if (!vertices.contains(p)) {
            vertices.add(p);
            Collections.sort(vertices);
        }

    }
}
