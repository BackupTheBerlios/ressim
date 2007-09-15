package no.uib.cipr.rs.meshgen.eclipse.bsp;

import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;

/**
 * @author Erlend
 */
public interface Edge {

    /**
     * Sets the normal of this edge to the given vector
     */
    void setNormal(Vector3D vector3D);

    /**
     * Returns a unit vector normal to this edge.
     * 
     * @throws IllegalAccessException
     */
    Vector3D getNormal() throws IllegalAccessException;

    /**
     * Returns the start point of this edge.
     */
    CornerPoint3D getBeginPoint();

    /**
     * Returns the end point of this edge.
     */
    CornerPoint3D getEndPoint();

    /**
     * Returns the spatial relationship between this edge and the given
     * partition plane
     */
    Partition classifyEdge(Plane3D partitionPlane);

    /**
     * Returns true if this edge is equal to the given edge. Equality means that
     * the edges share begin and end points within a tolerance
     * <code>Constants.smallEps</code>. The order of begin and end points is
     * arbitrary.
     */
    public boolean equals(Object p);

    /**
     * Returns the index of this edge
     */
    int getIndex();

}
