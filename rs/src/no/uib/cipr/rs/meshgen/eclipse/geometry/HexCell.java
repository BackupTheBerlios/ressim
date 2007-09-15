package no.uib.cipr.rs.meshgen.eclipse.geometry;

import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * Hexahedral cell class. Also responsible for global indexing of faces.
 */
public class HexCell {

    private int index;

    private int[] lgMap;

    private Quadrilateral top, bottom, front, back, left, right;

    private TrilinearMap trilinear;

    private CornerPoint3D[] p;

    /**
     * Creates the cell with (i, j, k) indices and global linear index, ind.
     * 
     * @param index
     * @param lgMap
     * @param points
     */
    public HexCell(int index, CornerPoint3D[] points, int[] lgMap) {
        this.index = index;

        this.lgMap = lgMap;

        if (points.length != 8)
            throw new IllegalArgumentException("Cell needs 8 corner points");

        this.p = points;

        trilinear = new TrilinearMap(points);

        top = new Quadrilateral(p[0], p[1], p[2], p[3], Orientation.TOP, index);

        bottom = new Quadrilateral(p[4], p[5], p[6], p[7], Orientation.BOTTOM,
                index);

        front = new Quadrilateral(p[0], p[1], p[4], p[5], Orientation.FRONT,
                index);
        back = new Quadrilateral(p[2], p[3], p[6], p[7], Orientation.BACK,
                index);

        left = new Quadrilateral(p[0], p[2], p[4], p[6], Orientation.LEFT,
                index);
        right = new Quadrilateral(p[1], p[3], p[5], p[7], Orientation.RIGHT,
                index);

    }

    /**
     * @return Returns the index.
     */
    public final int getIndex() {
        return index;
    }

    /**
     * @param local
     *                local corner point index
     * @return global corner point index
     */
    public int getGlobalCP(int local) {
        return lgMap[local];
    }

    /**
     * @return the top quadrilateral
     */
    public Quadrilateral top() {
        return top;
    }

    /**
     * @return the bottom quadrilateral
     */
    public Quadrilateral bottom() {
        return bottom;
    }

    /**
     * @return the front quadrilateral
     */
    public Quadrilateral front() {
        return front;
    }

    /**
     * @return the back quadrilateral
     */
    public Quadrilateral back() {
        return back;
    }

    /**
     * @return the left quadrilateral
     */
    public Quadrilateral left() {
        return left;
    }

    /**
     * @return the right quadrilateral
     */
    public Quadrilateral right() {
        return right;
    }

    /**
     * @return the cell volume
     */
    public double getVolume() {
        return trilinear.getVolume();
    }

    /**
     * @return the cell center coordinates
     */
    public CornerPoint3D getCenter() {
        return trilinear.getPoint(0.5, 0.5, 0.5);
    }

    /**
     * 
     * @param orient
     * @return Returns the normal of indicated cell face.
     * 
     * TODO implement/check sign reversals to get outward pointing normals.
     */
    public Vector3D getNormal(Orientation orient) {

        if (orient == Orientation.TOP)
            return top.getNormal();

        if (orient == Orientation.BOTTOM)
            return bottom.getNormal();

        if (orient == Orientation.FRONT)
            return front.getNormal();

        if (orient == Orientation.BACK)
            return back.getNormal();

        if (orient == Orientation.LEFT)
            return left.getNormal();

        if (orient == Orientation.RIGHT)
            return right.getNormal();

        return null;

    }

}
