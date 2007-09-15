package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.util.Configuration;

/**
 * Substitute transform.
 */
public class SubstituteTransform extends Transform {

    private Point3D[] points;

    /**
     * Creates a substitute transform.
     * 
     * @param config
     *            Substitute transform
     * @param topology
     *            Not used
     * @param points
     *            Not used
     */
    public SubstituteTransform(Configuration config,
            CartesianTopology topology, @SuppressWarnings("unused")
            Point3D[] points) {
        if (topology.getDimension() != 2)
            throw new IllegalArgumentException(config.trace()
                    + "SubstituteTransformation is only valid for 2D mesh.");
        // topology of input data
        int nx = config.getInt("NumXPoints");
        int ny = config.getInt("NumYPoints");

        if (nx != topology.getNumPointsI())
            throw new IllegalArgumentException(config.trace()
                    + "Illegal number of points in x-direction.");
        if (ny != topology.getNumPointsJ())
            throw new IllegalArgumentException(config.trace()
                    + "Illegal number of points in y-direction.");

        double[] xc = config.getDoubleArray("XCoordinates");
        double[] yc = config.getDoubleArray("YCoordinates");

        int np = nx * ny;
        if (xc.length != np)
            throw new IllegalArgumentException(config.trace() + "Expected "
                    + np + " x-coordinate values");

        if (yc.length != np)
            throw new IllegalArgumentException(config.trace() + "Expected "
                    + np + " number of y-coordinate values");

        this.points = new Point3D[topology.getNumPoints()];

        // linear index into xc and yc
        int l = 0;

        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                IJK ijk = new IJK(i, j, 0);
                double x = xc[l];
                double y = yc[l];
                l++;
                this.points[topology.getLinearPoint(ijk)] = new Point3D(x, y, 0);
            }
        }
    }

    @Override
    public Point3D getPoint(int i) {
        return this.points[i];
    }

}
