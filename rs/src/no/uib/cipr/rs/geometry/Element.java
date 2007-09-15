package no.uib.cipr.rs.geometry;

import java.io.Serializable;

import no.uib.cipr.rs.rock.Rock;

/**
 * Geometrical element (3D)
 */
public final class Element implements Serializable {

    private static final long serialVersionUID = -3112741817573702303L;

    public final Point3D center;

    public final double volume;

    public final int index;

    public final Rock rock;

    int[] points;

    int[] interfaces;

    int[] associatedNonNeighbourConnections;

    /**
     * Creates an element. The element can not be used before it is populated.
     * 
     * @param index
     *                The linear index of this element
     * @param center
     *                Double array containing the x, y and z coordinates of the
     *                element center
     * @param volume
     *                The element volume
     * @param rock
     *                The rock of this element
     */
    Element(int index, Point3D center, double volume, Rock rock, int[] ep,
            int[] ei, int[] ec) {
        this.center = center;
        this.volume = volume;
        this.rock = rock;
        this.index = index;

        points = ep;
        interfaces = ei;
        associatedNonNeighbourConnections = ec;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("element: ");
        s.append("i = " + index + ",\n");

        s.append("\t x = " + center.x() + ", y = " + center.y() + ", z = "
                + center.z() + ",\n");
        s.append("\t volume = " + volume + ",\n");

        s.append("\n\t points: ");
        for (int p : points)
            s.append(p + ", ");
        s.append("\n");

        s.append("\t interfaces: ");
        for (int intf : interfaces)
            s.append(intf + ", ");
        s.append("\n");

        return s.toString();
    }

}