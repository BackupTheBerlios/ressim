package no.uib.cipr.rs.meshgen.dfn;

import java.util.List;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

public class DFNGeometry extends Geometry {

    private static final long serialVersionUID = -328451879096006367L;

    private double[] connectionTrans;

    public DFNGeometry(Parser fileData) {

        /*
         * Setup the points
         */

        setNumPoints(fileData.getNumNodes());

        Point3D[] points = fileData.getPoints();
        for (int i = 0; i < fileData.getNumNodes(); ++i)
            buildPoint(i, points[i]);

        /*
         * Setup the elements
         */

        setNumElements(fileData.getNumActiveCVs());

        for (int i = 0; i < fileData.getNumActiveCVs(); ++i)
            buildElement(i, fileData.getVolume(i), fileData.getElementCenter(i));

        /*
         * Setup the connections and transmissibilities
         */

        setNumConnections(0, fileData.getNumConnections());
        connectionTrans = new double[fileData.getNumConnections()];
        for (int i = 0; i < connectionTrans.length; ++i)
            connectionTrans[i] = fileData.getTransmissibility(i);

        /*
         * Setup the interfaces
         */

        buildInterfaceGeometry(fileData);
    }

    /**
     * Computes interface centers, normals and areas.
     */
    private void buildInterfaceGeometry(Parser fileData) {
        setNumInterfaces(fileData.getNumInterfaces());

        List<CV> active = fileData.getActiveCVs();

        for (int elem = 0, interf = 0; elem < active.size(); elem++) {

            CV cv = active.get(elem);

            if (cv instanceof Node)
                throw new IllegalArgumentException("Node CVs not implemented");
            else if (cv instanceof Segment) {
                Segment e = (Segment) cv;

                double b = fileData.getCorrection(e.getCode());

                int[] l = e.getPointIndices();

                Point3D ni = fileData.getPoint(l[0]);
                Point3D nj = fileData.getPoint(l[1]);

                Vector3D vij = new Vector3D(ni, nj);
                Vector3D vji = new Vector3D(nj, ni);

                // intf 1
                buildInterface(interf++, getArea(ni, nj), getCenter(ni, nj),
                        getUnitNormal(ni, nj));

                // intf 2
                buildInterface(interf++, b, nj, vij.mult(1 / vij.norm2()));

                // intf 3
                buildInterface(interf++, getArea(nj, ni), getCenter(nj, ni),
                        getUnitNormal(nj, ni));

                // intf 4
                buildInterface(interf++, b, ni, vji.mult(1 / vji.norm2()));

            } else if (cv instanceof Polygon) {
                Polygon e = (Polygon) cv;

                int[] segments = e.getSegmentIndices();

                for (int s : segments) {
                    Segment seg = fileData.getSegment(s);

                    Point3D ni = fileData.getPoint(seg.getFirst());
                    Point3D nj = fileData.getPoint(seg.getSecond());

                    buildInterface(interf++, getArea(ni, nj),
                            getCenter(ni, nj), getOutwardNormal(ni, nj,
                                    fileData.getElementCenter(elem)));
                }

            } else if (cv instanceof Polyhedron)
                throw new IllegalArgumentException(
                        "Polyhedron CVs not implemented");
            else
                throw new IllegalArgumentException("Unknown CV type");
        }

    }

    private Vector3D getOutwardNormal(Point3D ni, Point3D nj, Point3D c) {
        Vector3D inward = new Vector3D(getCenter(ni, nj), c);

        Vector3D normal = getUnitNormal(ni, nj);

        if (normal.dot(inward) >= 0.0)
            normal = normal.mult(-1);

        return normal;
    }

    private double getArea(Point3D ni, Point3D nj) {
        Vector3D v = new Vector3D(ni, nj);
        double dz = 1.0;

        return v.norm2() * dz;
    }

    private Point3D getCenter(Point3D ni, Point3D nj) {
        return ni.plus(nj).scale(0.5);
    }

    private Vector3D getUnitNormal(Point3D ni, Point3D nj) {
        Vector3D n = new Vector3D(nj.y() - ni.y(), -(nj.x() - ni.x()), 0);
        return n.mult(1 / n.norm2());
    }

    public double getConnectionTrans(int i) {
        return connectionTrans[i];
    }

}
