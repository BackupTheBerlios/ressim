package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.QuadCell.BiFace;
import no.uib.cipr.rs.meshgen.transform.Transform;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.util.Configuration;

/**
 * A 2D structured geometry.
 */
public class StructuredGeometry2D extends StructuredGeometry {

    private static final long serialVersionUID = -6400675233212979132L;

    /**
     * Creates a 2D structured geometry from the given configuration.
     */
    public StructuredGeometry2D(Configuration config) {
        Configuration gConfig = config.getConfiguration("Geometry");

        double x0 = gConfig.getDouble("X0");
        double y0 = gConfig.getDouble("Y0");
        double z0 = 0;

        int[] nx = gConfig.getIntArray("Nx");
        int[] ny = gConfig.getIntArray("Ny");
        int[] nz = new int[] { 0 };

        double[] dx = gConfig.getDoubleArray("Dx");
        double[] dy = gConfig.getDoubleArray("Dy");
        double[] dz = { -1 };

        checkParts(nx, ny, dx, dy, dz);

        mappingType = gConfig.getString("RegionMappingType");

        checkMappingType();

        // find total number of cells in each direction
        int numX = ArrayData.getTotal(nx);
        int numY = ArrayData.getTotal(ny);

        // Create the topology, and initialize geometry structure
        topology = new CartesianTopology2D(gConfig, numX, numY);
        setSizes(topology);

        // expand deltas
        double[] deltaX = ArrayData.getExpanded(nx, dx);
        double[] deltaY = ArrayData.getExpanded(ny, dy);

        // expand z-direction parts
        partZ = ArrayData.getExpanded(nz);

        double[] x = ArrayData.getExpandedPoints(x0, deltaX);
        double[] y = ArrayData.getExpandedPoints(y0, deltaY);
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

            QuadCell cell = new QuadCell(this, cp);

            buildElement(cellIndex, cell.getVolume(), cell.getCenterPoint());

            for (Orientation orientation : Orientation.getOrientations(topology
                    .getDimension())) {
                int faceIndex = topology.getLinearInterface(ijk, orientation);

                BiFace face = cell.getFace(orientation);

                buildInterface(faceIndex, face.getArea(),
                        face.getCenterPoint(), face.getNormal());
            }
        }
    }

    public CartesianTopology2D getTopology() {
        return (CartesianTopology2D) topology;
    }

    /**
     * Checks parts for consistent values.
     * 
     * Number of parts must be equal for nx-dx, ny-dy.
     * 
     * nx and ny must all be positive.
     * 
     * dx and dy must be positive and dz must be negative
     * 
     * @param dz
     * @param dy
     * @param dx
     * @param ny
     * @param nx
     * 
     */
    private void checkParts(int[] nx, int[] ny, double[] dx, double[] dy,
            double[] dz) {
        if (nx.length != dx.length)
            throw new IllegalArgumentException(
                    "the number of nx and dx values are not equal");
        if (ny.length != dy.length)
            throw new IllegalArgumentException(
                    "the number of ny and dy values are not equal");
        if (dz.length != 1)
            throw new IllegalArgumentException(
                    "the number of dz values must be 1 for 2D geometry");

        for (int val : nx)
            if (!(val > 0))
                throw new IllegalArgumentException("nx must be positive: nx = "
                        + val);
        for (int val : ny)
            if (!(val > 0))
                throw new IllegalArgumentException("ny must be positive: ny = "
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
