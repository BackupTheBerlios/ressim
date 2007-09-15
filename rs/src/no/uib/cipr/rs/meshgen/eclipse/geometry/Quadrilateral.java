package no.uib.cipr.rs.meshgen.eclipse.geometry;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.structured.BilinearMap;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * A quadrilateral face of hexahedral cell
 * 
 * TODO currently the order of Point3D list at construction is crucial for
 * correct functionality. make more robust.
 * 
 * TODO move global face index calculation outside this class
 */
public class Quadrilateral {

    private Orientation orient;

    /**
     * containing cell index
     */
    private int cell;

    /**
     * vertex points
     */
    private CornerPoint3D p0, p1, p2, p3;

    private int[] orderedIndices;

    private int index;

    private double area;

    // barycenter normal
    private Vector3D normal;

    private Orientation type;

    private BilinearMap bilinear;

    /**
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     * @param type
     *                the <code>Orientation</code> type of this quadrilateral.
     * @param cell
     *                the linear index of containing cell. this is used to
     *                calculate global face index
     */
    public Quadrilateral(CornerPoint3D p0, CornerPoint3D p1, CornerPoint3D p2,
            CornerPoint3D p3, Orientation type, int cell) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;

        this.bilinear = new BilinearMap(p0, p1, p2, p3);

        this.type = type;

        if (type == Orientation.TOP || type == Orientation.BACK
                || type == Orientation.LEFT) { // type A ordering
            orderedIndices = new int[] { p0.getIndex(), p1.getIndex(),
                    p3.getIndex(), p2.getIndex() };
        } else { // type B ordering, i.e. bottom, front or right
            orderedIndices = new int[] { p1.getIndex(), p0.getIndex(),
                    p2.getIndex(), p3.getIndex() };
        }

        this.cell = cell;

        index = getIndex(cell);

        area = bilinear.getArea();

        normal = bilinear.getNormal();

        // TODO check if this should happen here
        // swap sign if bottom, front, right
        if (type == Orientation.BOTTOM || type == Orientation.FRONT
                || type == Orientation.RIGHT)
            normal = normal.mult(-1.0);
    }

    /**
     * @param i
     *                Cell index
     * @return Returns global face index
     */
    private int getIndex(int i) {
        return 6 * i + type.getLocalIndex();
    }

    /**
     * Check if a quadrilateral overlaps this quadrilateral
     * 
     * @param quad
     *                A candidate overlapping <code>Quadrilateral</code>
     * @return true if overlap
     */
    public boolean overlap(Quadrilateral quad) {

        // check vertical overlap
        if (type == Orientation.TOP || type == Orientation.BOTTOM)
            return (p0.equals(quad.getP0()) && p1.equals(quad.getP1())
                    && p2.equals(quad.getP2()) && p3.equals(quad.getP3()));

        // check lateral overlap
        if (p2.above(quad.getP0()) && p3.above(quad.getP1()))
            return false;

        if (p0.below(quad.getP2()) && p1.below(quad.getP3()))
            return false;

        return true;
    }

    /**
     * @return Point3D
     */
    private CornerPoint3D getP0() {
        return p0;
    }

    /**
     * @return Point3D
     */
    public CornerPoint3D getP1() {
        return p1;
    }

    /**
     * @return Point3D
     */
    public CornerPoint3D getP2() {
        return p2;
    }

    /**
     * @return Point3D
     */
    public CornerPoint3D getP3() {
        return p3;
    }

    /**
     * @return Returns the containing cell index.
     */
    public int getCell() {
        return cell;
    }

    /**
     * @return Returns the <code>Orientation</code> of this face.
     */
    public Orientation getOrientation() {
        return orient;
    }

    /**
     * This function returns an array of vertex point indices that are ordered
     * counter clockwise relative to the orientation type.
     * 
     * @return an ordered array of vertex indices
     */
    public int[] getOrientedPoints() {
        return orderedIndices;
    }

    /**
     * @return index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the coordinate of a point on the bilinear surface defined by this
     * quadrilateral. The input are the coordinates (a, b) in the surface
     * parameterization. The parameterization specifies the following
     * correspondance between local coordinates and corner points: (0,0)->p0
     * (1,0)->p1, (0,1)->p2 and (1,1)->p3.
     * 
     * @param a
     *                Local x-direction coordinate, 0 <= a <= 1.
     * @param b
     *                Local y-direction coordinate, 0 <= b <=1.
     * @return a point on the bilinear surface
     * 
     * @throws IllegalArgumentException
     *                 if (a,b) is not within legal range
     */

    public Point3D getPoint(double a, double b) throws IllegalArgumentException {

        if (a < 0 || a > 1)
            throw new IllegalArgumentException("'a' must be in [0,1]");
        if (b < 0 || b > 1)
            throw new IllegalArgumentException("'b' must be in [0,1]");

        return bilinear.getPoint(a, b);

    }

    /**
     * @return the quadrilateral area
     */
    public double getArea() {
        return area;
    }

    /**
     * Gets the normal vector in barycenter
     * 
     * @return a normal vector
     */
    public Vector3D getNormal() {
        return normal;
    }
}
