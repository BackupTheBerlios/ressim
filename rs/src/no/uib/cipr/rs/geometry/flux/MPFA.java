package no.uib.cipr.rs.geometry.flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Vector3D;

/**
 * Baseclass for MPFA transmissibility calculations. Subclasses must create an
 * interaction region and decide upon how to store the transmissibilities
 */
abstract class MPFA extends TransmissibilityComputer {

    /**
     * Retrives the connections associated with a grid point
     */
    protected Collection<NeighbourConnection> connections(CornerPoint p) {
        Set<NeighbourConnection> connections = new HashSet<NeighbourConnection>();

        for (Interface intf : mesh.interfaces(p))
            if (!intf.boundary)
                connections.add(mesh.connection(intf));

        return Collections.unmodifiableCollection(connections);
    }

    /**
     * Calculates T = C * (A \ B) - D, and checks for singularities
     */
    protected Matrix calculateTmatrix(Matrix A, Matrix B, Matrix C, Matrix D) {
        Matrix AiB = B.copy();

        try {
            AiB = A.solve(B, AiB);
        } catch (MatrixSingularException e) {
            throw new IllegalArgumentException(
                    "Conductivity tensor does not appear to be symmetrical, positive definite");
        }

        return C.multAdd(AiB, D.copy().scale(-1));
    }

    /**
     * Computes the distance between the centers of the given element and
     * interface
     * 
     * @return The Euclidian distance (2-norm)
     */
    protected double distance(Element el, Interface intf) {
        Point3D elp = el.center, intp = intf.center;

        Vector3D difference = new Vector3D(elp, intp);

        return difference.norm2();
    }

    /**
     * Gets the neighbouring interface of the given interface. Not for boundary
     * interfaces
     */
    protected Interface getNeighbour(Interface intf) {
        NeighbourConnection c = mesh.connection(intf);

        if (mesh.hereInterface(c) == intf)
            return mesh.thereInterface(c);
        else
            return mesh.hereInterface(c);
    }

    /**
     * Checks if the interface is on the "here" side of the connection
     */
    protected boolean isHere(Interface intf) {
        if (intf.boundary)
            return true;
        else
            return intf.index == mesh.connection(intf).hereInterface;
    }

    /**
     * Returns true if the given point is located on the domain boundary. This
     * occurs if at least one of the interfaces sharing the given points in
     * located on the boundary;
     */
    protected boolean pointOnBoundary(CornerPoint p) {
        for (Interface intf : mesh.interfaces(p))
            if (intf.boundary)
                return true;

        return false;
    }

    /**
     * Calculates -A*(K*n) on each of the interfaces
     */
    protected Vector3D[] calculateLocalConductivityDirections(Tensor3D K,
            CornerPoint p, List<Interface> interIntf) {
        Vector3D[] Kn = new Vector3D[interIntf.size()];

        for (int i = 0; i < interIntf.size(); i++) {
            Interface intf = interIntf.get(i);

            Kn[i] = K.mult(calculateNormalArea(intf, p));
        }

        return Kn;
    }

    /**
     * Computes the subface area located in the given interface centered around
     * the given point.
     * 
     * It is assumed that the points of the interface are circularly ordered.
     * 
     * Please note: This method uses faceting, and is not accurate for bilinear
     * surfaces. Try to use calculateNormalArea instead
     */
    protected double computeSubFaceArea(Interface intf, CornerPoint p) {
        // get half-edge points
        List<Point3D> pts = getHalfEdgePoints(p, intf);

        if (pts.size() != 2)
            throw new IllegalArgumentException(
                    "Two half-edge intersection points should exist");

        Point3D a = intf.center;
        Point3D b = p.coordinate;
        Point3D c = pts.get(0);
        Point3D d = pts.get(1);

        Vector3D ab = new Vector3D(a, b);
        Vector3D ac = new Vector3D(a, c);
        Vector3D ad = new Vector3D(a, d);

        double A1 = 0.5 * ab.cross(ac).norm2();
        double A2 = 0.5 * ab.cross(ad).norm2();

        return A1 + A2;
    }

    /**
     * Returns the two points of intersection between the half-face edges and
     * the edges of the given interface. The half-edge starts at the given
     * point.
     * 
     * This method relies on the interface points being circularly (clockwise)
     * ordered.
     * 
     * @return List of two points: the half-edge point after p1 along intf, and
     *         the half-edge point before p1 along intf
     */
    protected List<Point3D> getHalfEdgePoints(CornerPoint p1, Interface intf) {
        List<CornerPoint> points = new ArrayList<CornerPoint>();
        for (CornerPoint p : mesh.points(intf))
            points.add(p);

        int n = points.size();

        int i = points.indexOf(p1);

        // next point (in circular list)
        int j = (i + 1) % n;
        CornerPoint p2 = points.get(j);

        // previous point (in circular list)
        int k;
        if (i == 0)
            k = n - 1;
        else
            k = i - 1;
        CornerPoint p3 = points.get(k);

        // intersection point coordinates
        Point3D c1 = p1.coordinate;
        Point3D c2 = p2.coordinate;
        Point3D c3 = p3.coordinate;

        Point3D cp1 = c1.plus(c2).scale(0.5);

        Point3D cp2 = c1.plus(c3).scale(0.5);

        return Arrays.asList(cp1, cp2);
    }

    /**
     * Represents an interaction region tying together elements for the
     * computation of a continuous flux and pressure field by a multi-point
     * method
     */
    interface InteractionRegion {

        /**
         * The elements of the interaction region
         */
        Iterable<Element> elements();

        /**
         * Ordered list of the interfaces associated with each element
         */
        List<Interface> interfaces(Element el);

        /**
         * Local index of an element
         */
        int index(Element el);

        /**
         * Local index of an interface
         */
        int index(Interface intf);

        /**
         * Number of elements in the interaction region
         */
        int numElements();

        /**
         * Number of connections in the interaction region
         */
        int numConnections();

        /**
         * Calculates the matrix of transmissibilities based on the given
         * conductivity tensor
         */
        Matrix calculateT(Conductivity K);
    }

    /**
     * Tests for reproduability of uniform flow. Uses \nabla p = (xx,yy,zz), and
     * p = xx * x + yy * y + zz * z. This is only for K=I.
     */
    protected void testUniformFlow(List<List<Transmissibility>> M) {
        double xx = Math.random(), yy = Math.random(), zz = Math.random();
        Vector3D nablap = new Vector3D(xx, yy, zz);

        for (NeighbourConnection c : mesh.neighbourConnections()) {

            // No boundary 'attached' connection
            if (connectedToBoundary(c))
                continue;

            double f = 0;
            for (Transmissibility t : M.get(c.index)) {
                int k = mesh.element(t).index;
                double tk = t.k;

                Point3D ep = mesh.elements().get(k).center;
                double x = ep.x();
                double y = ep.y();
                double z = ep.z();

                double p = xx * x + yy * y + zz * z;

                f += tk * p;
            }

            Interface intf = mesh.hereInterface(c);
            double true_flux = -nablap.dot(intf.normal) * intf.area;

            double relativeDifference = Math.abs(true_flux - f)
                    / Math.abs(true_flux);
            if (relativeDifference > 1e-6)
                System.err
                        .printf(
                                "Non-uniform flow across connection %5d (%5d : %5d): %3.6e\n",
                                c.index + 1, mesh.here(c).index + 1, mesh
                                        .here(c).index + 1, relativeDifference);
        }
    }

    /**
     * Checks if the given connection is somehow attached to the boundary
     */
    private boolean connectedToBoundary(NeighbourConnection c) {
        Interface here = mesh.hereInterface(c);
        Interface there = mesh.thereInterface(c);

        return connectedToBoundary(here) || connectedToBoundary(there);
    }

    /**
     * Checks if the given interface has a neighboring interface which is a part
     * of the boundary
     */
    private boolean connectedToBoundary(Interface intf) {
        for (CornerPoint p : mesh.points(intf))
            for (Interface i : mesh.interfaces(p))
                if (i.boundary)
                    return true;

        return false;
    }

    /**
     * Calculates an outward normal vector on the given interface, where the
     * interface has four cornerpoints. As the interface curves, this vector is
     * in principle spatially varying. The vector returned is integrated over a
     * quarter of the interface, and hence is scaled by the area.
     */
    protected Vector3D calculateNormalArea(Interface intf, CornerPoint p) {
        List<CornerPoint> points = new ArrayList<CornerPoint>();
        for (CornerPoint p1 : mesh.points(intf))
            points.add(p1);

        // If not four points, do something similar ...
        if (points.size() == 2)
            // 2D case
            return intf.normal.mult(-intf.area / 2);
        else if (points.size() == 3)
            // 3D, exact planar interface
            return intf.normal.mult(-intf.area / 3);
        else if (points.size() > 4)
            // 3D, general polygon
            return intf.normal.mult(-computeSubFaceArea(intf, p));

        // Find the start-point
        int i = 0;
        for (CornerPoint point : points)
            if (point == p)
                break;
            else
                i++;

        Point3D x1 = points.get(i).coordinate;
        Point3D x2 = points.get(i == 0 ? 3 : (i - 1) % 4).coordinate;
        Point3D x3 = points.get((i + 1) % 4).coordinate;
        Point3D x4 = points.get((i + 2) % 4).coordinate;

        Vector3D v21 = new Vector3D(x1, x2); // x2 - x1
        Vector3D v31 = new Vector3D(x1, x3); // x3 - x1
        Vector3D v42 = new Vector3D(x2, x4); // x4 - x2
        Vector3D v43 = new Vector3D(x3, x4); // x4 - x3

        Vector3D n1 = v21.cross(v31).mult(9);
        Vector3D n2 = v21.cross(v42).mult(3);
        Vector3D n3 = v43.cross(v31).mult(3);
        Vector3D n4 = v43.cross(v42);

        return n1.plus(n2).plus(n3).plus(n4).mult(1. / 64.);
    }
}
