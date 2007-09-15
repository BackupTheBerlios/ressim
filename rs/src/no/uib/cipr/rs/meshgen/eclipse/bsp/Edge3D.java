package no.uib.cipr.rs.meshgen.eclipse.bsp;

import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Edge class
 */
public class Edge3D implements Edge {

    private CornerPoint3D begin;

    private CornerPoint3D end;

    private Vector3D normal;

    private boolean normalSet;

    private int index;

    /**
     * Creates a new edge between two points indicating begin and end with the
     * given index.
     */
    public Edge3D(CornerPoint3D begin, CornerPoint3D end, int index) {
        this.begin = begin;
        this.end = end;

        this.index = index;

        normalSet = false;
    }

    /**
     * Creates an edge between two points indicating begin and end. The index is
     * set to 0.
     */
    public Edge3D(CornerPoint3D begin, CornerPoint3D end) {
        this(begin, end, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Edge#getNormal()
     */
    public final Vector3D getNormal() throws IllegalAccessException {
        if (!normalSet)
            throw new IllegalAccessException("Normal not set");
        return normal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Edge#setNormal(bsp.Vector)
     */
    public void setNormal(Vector3D normal) {
        this.normal = normal;
        normalSet = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Edge#getStartPoint()
     */
    public CornerPoint3D getBeginPoint() {
        return begin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Edge#getEndPoint()
     */
    public CornerPoint3D getEndPoint() {
        return end;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Edge#classifyPartition(bsp.Plane)
     */
    public Partition classifyEdge(Plane3D partitionPlane) {

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

    @Override
    public boolean equals(Object p) {

        if (!(p instanceof Edge3D))
            return false;

        Edge3D edge = (Edge3D) p;

        if (this.begin.equals(edge.getBeginPoint())
                && this.end.equals(edge.getEndPoint()))
            return true;

        if (this.begin.equals(edge.getEndPoint())
                && this.end.equals(edge.getBeginPoint()))
            return true;

        return false;
    }

    @Override
    public String toString() {
        return new String(begin.toString() + " - " + end.toString());
    }

    public int getIndex() {
        return index;
    }

}
