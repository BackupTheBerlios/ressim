package no.uib.cipr.rs.meshgen.bsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.rs.geometry.Plane3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;
import no.uib.cipr.rs.util.Tolerances;

/**
 * A polyline representation of an edge. The edge points are stored in insertion
 * order.
 */
public class Edge3D {
    private Vector3D normal;

    private boolean normalSet;

    private List<IndexedPoint3D> vertices;

    /**
     * Creates a new edge between the given begin and end points.
     */
    public Edge3D(IndexedPoint3D begin, IndexedPoint3D end) {
        vertices = new ArrayList<IndexedPoint3D>(2);
        vertices.add(begin);
        vertices.add(end);

        normalSet = false;
    }

    /**
     * Creates a new edge from the given list of points.
     */
    public Edge3D(List<IndexedPoint3D> vertices) {
        this.vertices = new ArrayList<IndexedPoint3D>(vertices);

        normalSet = false;
    }

    /**
     * Returns a unit vector normal to this edge.
     * 
     * @throws IllegalAccessException
     */
    public Vector3D getNormal() throws IllegalAccessException {
        if (!normalSet)
            throw new IllegalAccessException("Normal not set");
        return normal;
    }

    /**
     * Sets the normal of this edge to the given vector
     */
    public void setNormal(Vector3D normal) {
        this.normal = normal;
        normalSet = true;
    }

    /**
     * Returns the start point of this edge.
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

    /**
     * Returns the vertices of this edge not including end point.
     */
    public List<IndexedPoint3D> getPoints() {
        // return vertices;
        return vertices; //.subList(0, vertices.size() - 1);
    }

    /**
     * Reverses the point list for this edge
     */
    public void reverse() {
        Collections.reverse(vertices);
    }

    /**
     * Returns the edge segment of this edge that lies between the given begin
     * and end points. The begin and end points are included in the segment.
     */
    public Edge3D getSegment(IndexedPoint3D begin, IndexedPoint3D end) {
        boolean reverse = isReverse(begin, end);

        List<IndexedPoint3D> temp = new ArrayList<IndexedPoint3D>(
                vertices);

        // do not add point if present
        if (!temp.contains(begin))
            temp.add(begin);
        if (!temp.contains(end))
            temp.add(end);

        if (reverse)
            Collections.sort(temp, Collections.reverseOrder());
        else
            Collections.sort(temp);

        int i = temp.indexOf(begin);
        int j = temp.lastIndexOf(end);

        if (i > j)
            throw new IllegalArgumentException(
                    "Begin and end points are not ordered");

        List<IndexedPoint3D> v = new ArrayList<IndexedPoint3D>();
        for (int l = i; l <= j; l++)
            v.add(temp.get(l));

        return new Edge3D(v);
    }

    /**
     * Returns the spatial relationship between this edge and the given
     * partition plane
     */
    public Partition classifyEdge(Plane3D partitionPlane) {
        IndexedPoint3D begin = getBeginPoint();
        IndexedPoint3D end = getEndPoint();

        double distBegin = partitionPlane.getPointDistance(begin);
        double distEnd = partitionPlane.getPointDistance(end);

        if (distEnd > Tolerances.smallEps) {
            if (distBegin < Tolerances.smallEps) {
                return Partition.INTERSECTING;
            } else if (distBegin > Tolerances.smallEps) {
                return Partition.OUTSIDE;
            } else { // distBegin equal to 0 within tolerance
                return Partition.OUTSIDE;
            }
        }

        else if (distEnd < Tolerances.smallEps) {
            if (distBegin < Tolerances.smallEps) {
                return Partition.INSIDE;
            } else if (distBegin > Tolerances.smallEps) {
                return Partition.INTERSECTING;
            } else { // distBegin equal to 0 within tolerance
                return Partition.INSIDE;
            }

        } else { // distEnd equal to 0 within tolerance

            if (distBegin < Tolerances.smallEps) {
                return Partition.INSIDE;
            } else if (distBegin > Tolerances.smallEps) {
                return Partition.OUTSIDE;
            } else { // distBegin equal to 0 within tolerance
                return Partition.COINCIDENT;
            }
        }
    }

    /**
     * Returns the vertices of this edge including end point.
     */
    public List<IndexedPoint3D> vertices() {
        return vertices;
    }

    /**
     * Returns true if this edge is equal to the given edge. Equality means that
     * the edges share begin and end points within a tolerance
     * <code>Constants.smallEps</code>. The order of begin and end points is
     * arbitrary.
     */
    @Override
    public boolean equals(Object p) {
        if (!(p instanceof Edge3D))
            return false;

        Edge3D edge = (Edge3D) p;

        if (this.getBeginPoint().equals(edge.getBeginPoint())
                && this.getEndPoint().equals(edge.getEndPoint()))
            return true;

        if (this.getBeginPoint().equals(edge.getEndPoint())
                && this.getEndPoint().equals(edge.getBeginPoint()))
            return true;

        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (IndexedPoint3D p : vertices)
            s.append(p.toString() + " ");

        return s.toString();
    }

    /**
     * Returns true if the given points are reversely ordered.
     */
    private boolean isReverse(IndexedPoint3D begin, IndexedPoint3D end) {
        return begin.compareTo(end) != -1 ? true : false;
    }

}
