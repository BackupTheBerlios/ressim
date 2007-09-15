package no.uib.cipr.rs.meshgen.lgr;

import static no.uib.cipr.rs.meshgen.structured.Orientation.BACK;
import static no.uib.cipr.rs.meshgen.structured.Orientation.BOTTOM;
import static no.uib.cipr.rs.meshgen.structured.Orientation.RIGHT;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.meshgen.bsp.Edge3D;
import no.uib.cipr.rs.meshgen.bsp.Polygon3D;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * A face backed by a polygon
 */
class Face {

    /**
     * The polygon representing this face
     */
    private Polygon3D polygon;

    /**
     * Global index of this face.
     */
    private int i;

    /**
     * Flags that this face contains subfaces
     */
    private boolean isRefined;

    /**
     * Stores subfaces if this face is refined.
     */
    private List<Face> subfaces;

    private Orientation orientation;

    /**
     * Stores the global point indices of this face
     */
    private List<Integer> pointIndices;

    /**
     * Creates a face with a given orientation from the given list of edges. The
     * edges must be given in a global counter-clockwise order.
     */
    public Face(List<Edge3D> edges, Orientation orientation)
            throws IllegalArgumentException {
        this.orientation = orientation;
        polygon = new Polygon3D(edges);
        isRefined = false;
    }

    /**
     * Creates a face from the given polygon.
     */
    public Face(Polygon3D polygon, Orientation orientation) {
        this.orientation = orientation;
        this.polygon = polygon;
        isRefined = false;
    }

    /**
     * Sets the index of this face to the given value.
     */
    public void setIndex(int i) {
        this.i = i;
    }

    /**
     * Adds the given polygon to the list of subfaces for this face.
     */
    public void addSubface(Face subface) {
        if (!isRefined)
            subfaces = new ArrayList<Face>();

        subfaces.add(subface);
        isRefined = true;
    }

    /**
     * Returns the global index of this face.
     * 
     * NOTE that the index is not set at construction but during global
     * interface topology.
     */
    public int getIndex() {
        return i;
    }

    /**
     * Returns true if this face is refined
     */
    public boolean isRefined() {
        return isRefined;
    }

    /**
     * Returns the subfaces of this face.
     */
    public List<Face> getSubfaces() {
        return subfaces;
    }

    /**
     * Returns a list of the points of this face.
     */
    public List<IndexedPoint3D> getPoints() {
        return polygon.getPoints();
    }

    public void setPointIndices() {
        if (isRefined)
            throw new IllegalArgumentException(
                    "Faces containing subfaces should not set point indices");

        if (orientation == BOTTOM || orientation == BACK
                || orientation == RIGHT)
            if (!polygon.isReversed())
                polygon.reverse();

        List<IndexedPoint3D> pts = polygon.getPoints();

        pointIndices = new ArrayList<Integer>();

        for (IndexedPoint3D p : pts) {
            int pIndex = p.getIndex();
            if (!pointIndices.contains(pIndex))
                pointIndices.add(pIndex);
        }
    }

    /**
     * Returns a list containing the indices of all the points of this face. For
     * BOTTOM, BACK and RIGHT faces, the order of the points are reversed.
     * 
     * NOTE: this method is used to set interface-points topology.
     */
    public int[] getPointIndices() {
        if (isRefined)
            throw new IllegalArgumentException(
                    "Faces containing subfaces should not return point indices");
        return ArrayData.integerListToArray(pointIndices);
    }

    /**
     * Returns a list of the indices of the primary corner points of this face.
     * For BOTTOM, BACK and RIGHT faces, the order of the points is reversed.
     * 
     * NOTE: This method is used for area, normal and center point computations.
     */

    public List<Integer> getCornerIndices() {
        if (isRefined)
            throw new IllegalArgumentException(
                    "Faces containing subfaces should not return corner indices");

        if (orientation == BOTTOM || orientation == BACK
                || orientation == RIGHT)
            if (!polygon.isReversed())
                polygon.reverse();

        List<Integer> indices = new ArrayList<Integer>(4);

        for (IndexedPoint3D p : polygon.getBeginPoints())
            indices.add(p.getIndex());

        return indices;
    }

    /**
     * Returns the resulting face from intersecting this face with the given
     * face. If there is no overlap, null is returned.
     */
    public Face getIntersection(Face there) {
        Polygon3D overlap = polygon.getIntersection(there.polygon);

        if (overlap == null)
            return null;

        return new Face(overlap, orientation);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (Edge3D e : polygon.getEdges())
            s.append("\n\t\t\t" + e.toString());

        return s.toString();
    }

    /**
     * Updates the edges of this face. Recreates the backing polygon with the
     * given list of edges. This only works for non-refined faces.
     */
    public void updateEdges(List<Edge3D> edges) {
        if (isRefined)
            throw new IllegalArgumentException(
                    "Face with refined faces cannot have its edges updated");

        polygon = new Polygon3D(edges);
    }
}
