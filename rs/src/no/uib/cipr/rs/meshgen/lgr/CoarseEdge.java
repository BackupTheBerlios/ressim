package no.uib.cipr.rs.meshgen.lgr;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * Stores topological and geometrical (coarse) edge information.
 */
class CoarseEdge {

    /**
     * Coordinate sorted list of points along this edge.
     */
    private List<IndexedPoint3D> vertices;

    /**
     * Map from the containing element indices to the location of this edge in
     * the corresponding elements.
     */
    private EdgeMap elements;

    public CoarseEdge(EdgeMap elements) {
        this.elements = elements;
    }

    /**
     * Sets the points along this edge to the given points.
     */
    public void setPoints(SortedSet<IndexedPoint3D> vertices) {
        this.vertices = new ArrayList<IndexedPoint3D>(vertices);
    }

    /**
     * Returns the points along this edge.
     */
    public List<IndexedPoint3D> getPoints() {
        return vertices;
    }

    /**
     * Returns the elements of this edge.
     */
    public EdgeMap getElements() {
        return elements;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(elements.entrySet().toString());
        s.append("\n");
        for (IndexedPoint3D p : vertices)
            s.append(p.toString() + "\n");

        return s.toString();
    }
}
