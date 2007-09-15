package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Bipolar transform.
 */
public class BipolarTransform extends Transform {

    private Point3D[] points;

    private double a;

    /**
     * Creates an bipolar transform
     * 
     * @param config
     *            Configuration containing data for the transformation
     * @param topology
     *            A structured topology
     * @param points
     *            Logical space corner points
     */
    public BipolarTransform(Configuration config, CartesianTopology topology,
            Point3D[] points) {
        if (topology.getDimension() != 2)
            throw new IllegalArgumentException(config.trace()
                    + "BipolarTransformation is only valid for 2D mesh.");

        this.points = points;

        // read physical domain vertices
        a = config.getDouble("a");

        if (!(a > 0))
            throw new IllegalArgumentException(config.trace()
                    + "Parameter 'a' must be larger than zero.");
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double r = p.x();
        double s = Math.PI * (p.y() - 0.5);

        double x = a * Math.sinh(r) / (Math.cosh(r) + Math.cos(s));
        double y = a * Math.sin(s) / (Math.cosh(r) + Math.cos(s));

        return new Point3D(x, y, 0);
    }
}
