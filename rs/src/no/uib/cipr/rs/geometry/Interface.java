package no.uib.cipr.rs.geometry;

import java.io.Serializable;

/**
 * Element interface (2D)
 */
public final class Interface implements Serializable {

    private static final long serialVersionUID = 8198463754751432293L;

    public final int index;

    public final double area;

    public final Vector3D normal;

    public final Point3D center;

    public final boolean boundary;

    int[] points;

    int element;

    int neighbourConnection;

    /**
     * Creates an internal interface. The interface can not be used before it is
     * populated.
     * 
     * @param index
     *                Global index of this interface
     * @param normal
     *                Normal vector of this interface
     * @param area
     *                Interface area
     * @param center
     *                Array of x-, y-, and z-coordinates for the interface's
     *                center point
     * @param boundary
     *                True for a boundary interface
     */
    Interface(int index, Vector3D normal, double area, Point3D center,
            boolean boundary, int[] ip, int ie, int ic) {
        this.boundary = boundary;
        this.center = center;
        this.index = index;
        this.area = area;
        this.normal = normal;
        points = ip;
        element = ie;

        neighbourConnection = ic;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("interface: ");
        s.append("i = " + index + ",\n");

        s.append("\t x = " + center.x() + ", y = " + center.y() + ", z = "
                + center.z() + ",\n");

        s.append("\n\t points: ");
        for (int p : points)
            s.append(p + ", ");
        s.append("\n");

        s.append("\t element: " + element);
        s.append("\n");

        return s.toString();
    }

}