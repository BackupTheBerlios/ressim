package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Annulus transform.
 */
public class AnnulusTransform extends Transform {

    private Point3D[] points;

    private double theta0, theta1;

    private double r0, r1;

    /**
     * Creates an annulus transform
     * 
     * @param config
     *            Configuration containing data for the transformation
     * @param topology
     *            A structured topology
     * @param points
     *            Logical space corner points
     */
    public AnnulusTransform(Configuration config, CartesianTopology topology,
            Point3D[] points) {
        if (topology.getDimension() != 2)
            throw new IllegalArgumentException(config.trace()
                    + "AnnulusTransformation is only valid for 2D mesh.");

        this.points = points;

        // read physical domain vertices
        r0 = config.getDouble("r0");
        r1 = config.getDouble("r1");

        if (r0 < 0)
            throw new IllegalArgumentException(config.trace()
                    + "Radius r0 must not be less than zero");

        if (r1 <= r0)
            throw new IllegalArgumentException(config.trace()
                    + "Radius r1 must be larger than r0");

        theta0 = config.getDouble("theta0");
        theta1 = config.getDouble("theta1");

        if (theta0 < 0)
            throw new IllegalArgumentException(config.trace()
                    + "Angle theta0 must not be less than zero");

        if (theta1 <= theta0)
            throw new IllegalArgumentException(config.trace()
                    + "Angle theta1 must be larger than theta0");

        if (theta1 > 2 * Math.PI)
            throw new IllegalArgumentException(config.trace()
                    + "Angle theta1 must not be larger than 2pi");
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double r = r0 + (r1 - r0) * p.y();
        double theta = theta1 + (theta0 - theta1) * p.x();

        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);

        return new Point3D(x, y, 0);
    }
}
