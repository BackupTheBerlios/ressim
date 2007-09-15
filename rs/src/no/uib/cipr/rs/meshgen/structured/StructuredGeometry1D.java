package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.BiCell.MonoFace;
import no.uib.cipr.rs.meshgen.transform.Transform;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.util.Configuration;

/**
 * A 1D structured geometry.
 */
public class StructuredGeometry1D extends StructuredGeometry {

    private static final long serialVersionUID = 1412522003383160684L;

    /**
     * Creates a 1D structured geometry from the configuration
     */
    public StructuredGeometry1D(Configuration config) {
        Configuration gConfig = config.getConfiguration("Geometry");

        double x0 = gConfig.getDouble("X0");
        double y0 = 0;
        double z0 = 0;

        int[] nx = gConfig.getIntArray("Nx");
        int[] nz = new int[] { 0 };

        double[] dx = gConfig.getDoubleArray("Dx");
        double[] dy = { 1.0 };
        double[] dz = { -1.0 };

        checkParts(nx, dx, dy, dz);

        mappingType = gConfig.getString("RegionMappingType");

        checkMappingType();

        // find total number of cells in each direction
        int numX = ArrayData.getTotal(nx);

        // Create the topology, and initialize geometry structure
        topology = new CartesianTopology1D(gConfig, numX);
        setSizes(topology);

        // expand deltas
        double[] deltaX = ArrayData.getExpanded(nx, dx);

        partZ = ArrayData.getExpanded(nz);

        // expand point coordinates
        double[] x = ArrayData.getExpandedPoints(x0, deltaX);
        double[] y = new double[] { y0 };
        double[] z = new double[] { z0 };

        // store non-transformed point coordinates
        Point3D[] nonTransPoints = new Point3D[topology.getNumPoints()];
        for (IJK ijk : topology.getPointsIJK()) {
            nonTransPoints[topology.getLinearPoint(ijk)] = new Point3D(x[ijk
                    .i()], y[ijk.j()], z[ijk.k()]);
        }

        Transform transform = Transform.create(gConfig, topology,
                nonTransPoints);

        for (IJK ijk : topology.getPointsIJK())
            buildPoint(topology.getLinearPoint(ijk), transform
                    .getPoint(topology.getLinearPoint(ijk)));

        for (IJK ijk : topology.getElementsIJK()) {
            int[] cp = topology.getElementPoints(ijk);

            int cellIndex = topology.getLinearElement(ijk);

            BiCell cell = new BiCell(this, cp);

            buildElement(cellIndex, cell.getVolume(), cell.getCenterPoint());

            for (Orientation orientation : Orientation.getOrientations1D()) {
                int faceIndex = topology.getLinearInterface(ijk, orientation);

                MonoFace face = cell.getFace(orientation);

                buildInterface(faceIndex, face.getArea(),
                        face.getCenterPoint(), face.getNormal());
            }
        }

    }

    public CartesianTopology1D getTopology() {
        return (CartesianTopology1D) topology;
    }

    /**
     * Checks mesh parts for consistent values.
     * 
     * Number of parts must be equal for nx-dx.
     * 
     * nx must all be positive.
     * 
     * dx must be positive.
     * 
     * @param nx
     * @param dx
     * @param dy
     * @param dz
     * 
     */
    private void checkParts(int[] nx, double[] dx, double[] dy, double[] dz) {
        if (nx.length != dx.length)
            throw new IllegalArgumentException(
                    "the number of nx and dx values are not equal");
        if (dy.length != 1)
            throw new IllegalArgumentException(
                    "the number of dy values must be 1 for 1D geometry");
        if (dz.length != 1)
            throw new IllegalArgumentException(
                    "the number of dz values must be 1 for 1D geometry");

        for (int val : nx)
            if (!(val > 0))
                throw new IllegalArgumentException("nx must be positive: nx = "
                        + val);

        for (double val : dz)
            if (!(val < 0.0))
                throw new IllegalArgumentException("dz must be�negative: dz = "
                        + val);
        for (double val : dy)
            if (!(val > 0.0))
                throw new IllegalArgumentException("dy must be positive: dy = "
                        + val);
        for (double val : dx)
            if (!(val > 0.0))
                throw new IllegalArgumentException("dx must be positive: dx = "
                        + val);
    }
}
