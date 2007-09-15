package no.uib.cipr.rs.meshgen.eclipse.bsp;

import java.util.List;

import no.uib.cipr.rs.geometry.Point3D;

/**
 * Interface for storing Polygon functionality
 */
public interface Polygon {

    /**
     * Returns the area of this polygon.
     */
    public double getArea();

    /**
     * Returns the <code>Polygon</code> that forms the intersection of this
     * polygon with the given input polygon. Null is returned if the
     * intersection contains less than three edges. Polygon to be intersected
     * with.
     */
    public Polygon getIntersection(Polygon q);

    /**
     * Returns the segment of the given edge that is inside this polygon. If no
     * part of the edge is inside, null is returned.
     */
    public Edge getInsideSegment(Edge edge);

    /**
     * Returns an iterable object of the edges of this polygon.
     */
    public Iterable<Edge> getEdges();

    /**
     * Gets the (unique) vertices of this polygon.
     */
    public List<Point3D> vertices();
}
