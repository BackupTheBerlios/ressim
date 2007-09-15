package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.BilinearMap;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Bilinear transform.
 */
public class BilinearTransform extends Transform {

    private Point3D[] points;

    private BilinearMap bilinear;

    /**
     * Creates a bilinear transform of the given input points.
     * 
     * @param config
     *            Configuration
     * @param topology
     *            The topology
     * @param points
     *            Logical space corner points
     */
    public BilinearTransform(Configuration config, @SuppressWarnings("unused")
    CartesianTopology topology, Point3D[] points) {
        this.points = points;

        // read physical domain vertices
        double[] x = config.getDoubleArray("x");
        double[] y = config.getDoubleArray("y");

        Point3D x00 = new Point3D(x[0], y[0], 0);
        Point3D x10 = new Point3D(x[1], y[1], 0);
        Point3D x01 = new Point3D(x[2], y[2], 0);
        Point3D x11 = new Point3D(x[3], y[3], 0);

        bilinear = new BilinearMap(x00, x10, x01, x11);

    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        Point3D bp = bilinear.getPoint(p.x(), p.y());

        return new Point3D(bp.x(), bp.y(), p.z());
    }
}
