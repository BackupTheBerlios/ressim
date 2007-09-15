package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.HexCell.QuadFace;
import no.uib.cipr.rs.meshgen.transform.Transform;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.util.Configuration;

/**
 * A 3D structured geometry.
 */
public class StructuredGeometry3D extends StructuredGeometry {

    private static final long serialVersionUID = 2414702812513102945L;

    /**
     * Creates a 3D structured geometry from the given configuration.
     */
    public StructuredGeometry3D(Configuration config) {
        Configuration gConfig = config.getConfiguration("Geometry");

        double x0 = gConfig.getDouble("X0");
        double y0 = gConfig.getDouble("Y0");
        double z0 = gConfig.getDouble("Z0");

        int[] nx = gConfig.getIntArray("Nx");
        int[] ny = gConfig.getIntArray("Ny");
        int[] nz = gConfig.getIntArray("Nz");

        double[] dx = gConfig.getDoubleArray("Dx");
        double[] dy = gConfig.getDoubleArray("Dy");
        double[] dz = gConfig.getDoubleArray("Dz");

        checkParts(nx, ny, nz, dx, dy, dz);

        mappingType = gConfig.getString("RegionMappingType");

        checkMappingType();

        // find total number of cells in each direction
        int numX = ArrayData.getTotal(nx);
        int numY = ArrayData.getTotal(ny);
        int numZ = ArrayData.getTotal(nz);

        // Create the topology, and initialize geometry structure
        topology = new CartesianTopology3D(gConfig, numX, numY, numZ);
        setSizes(topology);

        // expand deltas
        double[] deltaX = ArrayData.getExpanded(nx, dx);
        double[] deltaY = ArrayData.getExpanded(ny, dy);
        double[] deltaZ = ArrayData.getExpanded(nz, dz);

        // expand z-direction parts
        partZ = ArrayData.getExpanded(nz);

        // expand point coordinates
        double[] x = ArrayData.getExpandedPoints(x0, deltaX);
        double[] y = ArrayData.getExpandedPoints(y0, deltaY);
        double[] z = ArrayData.getExpandedPoints(z0, deltaZ);

        // store non-transformed point coordinates
        Point3D[] nonTransPoints = new Point3D[topology.getNumPoints()];
        for (IJK ijk : topology.getPointsIJK())
            nonTransPoints[topology.getLinearPoint(ijk)] = new Point3D(x[ijk
                    .i()], y[ijk.j()], z[ijk.k()]);

        Transform transform = Transform.create(gConfig, topology,
                nonTransPoints);

        for (IJK ijk : topology.getPointsIJK())
            buildPoint(topology.getLinearPoint(ijk), transform
                    .getPoint(topology.getLinearPoint(ijk)));

        for (IJK ijk : topology.getElementsIJK()) {
            int[] cp = topology.getElementPoints(ijk);

            int cellIndex = topology.getLinearElement(ijk);

            HexCell cell = new HexCell(this, cp);

            buildElement(cellIndex, cell.getVolume(), cell.getCenterPoint());

            for (Orientation orientation : Orientation.getOrientations(topology
                    .getDimension())) {
                int faceIndex = topology.getLinearInterface(ijk, orientation);

                QuadFace face = cell.getFace(orientation);

                buildInterface(faceIndex, face.getArea(),
                        face.getCenterPoint(), face.getNormal());
            }
        }
    }

    /**
     * Creates a structured geometry, for use by the SPE10 importer
     */
    public StructuredGeometry3D(CartesianTopology3D topology, Box box,
            double dx, double dy, double dz) {
        this.topology = topology;

        int[] nx = new int[] { box.getNumI() };
        int[] ny = new int[] { box.getNumJ() };
        int[] nz = new int[] { box.getNumK() };

        // Initialize geometry structure
        setSizes(topology);

        // expand deltas
        double[] deltaX = ArrayData.getExpanded(nx, new double[] { dx });
        double[] deltaY = ArrayData.getExpanded(ny, new double[] { dy });
        double[] deltaZ = ArrayData.getExpanded(nz, new double[] { dz });

        // expand point coordinates
        double[] x = ArrayData.getExpandedPoints(0, deltaX);
        double[] y = ArrayData.getExpandedPoints(0, deltaY);
        double[] z = ArrayData.getExpandedPoints(0, deltaZ);

        // store point coordinates
        for (IJK ijk : topology.getPointsIJK())
            buildPoint(topology.getLinearPoint(ijk), new Point3D(x[ijk.i()],
                    y[ijk.j()], z[ijk.k()]));

        for (IJK ijk : topology.getElementsIJK()) {
            int[] cp = topology.getElementPoints(ijk);

            int cellIndex = topology.getLinearElement(ijk);

            HexCell cell = new HexCell(this, cp);

            buildElement(cellIndex, cell.getVolume(), cell.getCenterPoint());

            for (Orientation orientation : Orientation.getOrientations(topology
                    .getDimension())) {
                int faceIndex = topology.getLinearInterface(ijk, orientation);

                QuadFace face = cell.getFace(orientation);

                buildInterface(faceIndex, face.getArea(),
                        face.getCenterPoint(), face.getNormal());
            }
        }
    }

    public CartesianTopology3D getTopology() {
        return (CartesianTopology3D) topology;
    }

    /**
     * Checks parts for consistent values.
     * 
     * Number of parts must be equal for nx-dx, ny-dy and nz-dz. nx, ny and nz
     * must all be positive. dx and dy must be positive and dz must be negative
     */
    private void checkParts(int[] nx, int[] ny, int[] nz, double[] dx,
            double[] dy, double[] dz) {
        if (nx.length != dx.length)
            throw new IllegalArgumentException(
                    "the number of nx and dx values are not equal");
        if (ny.length != dy.length)
            throw new IllegalArgumentException(
                    "the number of ny and dy values are not equal");
        if (nz.length != dz.length)
            throw new IllegalArgumentException(
                    "the number of nz and dz values are not equal");

        for (int val : nx)
            if (!(val > 0))
                throw new IllegalArgumentException("nx must be positive: nx = "
                        + val);
        for (int val : ny)
            if (!(val > 0))
                throw new IllegalArgumentException("ny must be positive: ny = "
                        + val);
        for (int val : nz)
            if (!(val > 0))
                throw new IllegalArgumentException("nz must be positive: nz = "
                        + val);

        for (double val : dz)
            if (!(val < 0.0))
                throw new IllegalArgumentException("dz must be negative: dz = "
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
