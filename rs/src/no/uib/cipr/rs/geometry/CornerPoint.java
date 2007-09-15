package no.uib.cipr.rs.geometry;

import java.io.Serializable;

/**
 * Geometrical cornerpoint (1D)
 */
public final class CornerPoint implements Serializable {

    private static final long serialVersionUID = -6915130726826411942L;

    public final int index;

    int[] interfaces;

    int[] elements;

    public final Point3D coordinate;

    /**
     * Creates a point. The point can not be used before it is populated.
     * 
     * @param index
     *                Global point index
     * @param point
     *                Point coordinates
     */
    CornerPoint(int index, Point3D point, int[] interfaces, int[] elements) {
        this.index = index;
        this.coordinate = point;
        this.interfaces = interfaces;
        this.elements = elements;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("point: ");
        s.append("i = " + index + ",\n");

        s.append("\t x = " + coordinate.x() + ", y = " + coordinate.y()
                + ", z = " + coordinate.z() + ",\n");

        s.append("\n\t interfaces: ");
        for (int intf : interfaces)
            s.append(intf + ", ");
        s.append("\n");

        s.append("\t elements: ");
        for (int el : elements)
            s.append(el + ", ");
        s.append("\n");

        return s.toString();
    }

}
