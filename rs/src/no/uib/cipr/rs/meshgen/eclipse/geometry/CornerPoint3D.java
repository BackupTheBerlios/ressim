package no.uib.cipr.rs.meshgen.eclipse.geometry;

import java.util.Locale;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * This class stores indexed and comparable points of the corner point mesh.
 */
public class CornerPoint3D extends IndexedPoint3D {

    private static final long serialVersionUID = -8784384173143829027L;

    // true if this point is an edge-edge intersection point.
    private boolean edgeIntersection;

    // index pair of edge indices. This is set if the point is an intersection
    // point. To be used
    // in overlap computations.
    private IJ ij;

    /**
     * Creates a point with the specified coordinates and index
     * 
     * @param x
     * @param y
     * @param z
     * @param i
     *            global (duplicate) corner point index
     */
    public CornerPoint3D(double x, double y, double z, int i) {
        super(x, y, z, i);
        edgeIntersection = false;
    }

    /**
     * Creates a point with the same coordinates as the given point. The index
     * is set to 0.
     * 
     * @param point
     *            A <code>Point3D</code>
     */
    public CornerPoint3D(CornerPoint3D point) {
        super(point.x(), point.y(), point.z(), IndexedPoint3D.UNKNOWN);
        edgeIntersection = false;
    }

    /**
     * Creates a point at the end point of the given input vector starting at
     * the given input point.
     * 
     * @param p
     *            A starting point
     * @param v
     *            A direction vector
     */
    public CornerPoint3D(CornerPoint3D p, Vector3D v) {
        super(p.x() + v.x(), p.y() + v.y(), p.z() + v.z(), IndexedPoint3D.UNKNOWN);
        edgeIntersection = false;
    }

    /**
     * Creates a point with the given coordinates. The index is set to 0.
     * 
     * @param x
     *            x-coordinate
     * @param y
     *            y-coordinate
     * @param z
     *            z-coordinate
     */
    public CornerPoint3D(double x, double y, double z) {
        this(x, y, z, IndexedPoint3D.UNKNOWN);
    }

    /**
     * 
     * @param p
     *            Input point
     * @param i
     *            Point index
     */
    public CornerPoint3D(Point3D p, int i) {
        this(p.x(), p.y(), p.z(), i);
    }

    /**
     * Creates a new point with the given edge-edge intersection index
     * 
     * @param a
     * @param v
     * @param ij
     *            edge-edge intersection index
     */
    public CornerPoint3D(CornerPoint3D a, Vector3D v, IJ ij) {
        this(a, v);

        edgeIntersection = true;

        this.ij = ij;
    }

    /**
     * Checks if this point is vertically above other point
     * 
     * @param p
     * @return true if this.z >= p.z
     * 
     * TODO check if tolerance must be given
     */
    public final boolean above(CornerPoint3D p) {
        return (z >= p.z());
    }

    /**
     * Checks if this point is vertically below other point
     * 
     * @param p
     * @return true if this.z <= p.z
     * 
     * TODO check if tolerance must be given
     */
    public final boolean below(CornerPoint3D p) {
        return (z <= p.z());
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%.4f, %.4f, %.4f]", x, y, z);
    }

    public boolean isEdgeIntersection() {
        return edgeIntersection;
    }

    public IJ getIJ() {
        return ij;
    }
}