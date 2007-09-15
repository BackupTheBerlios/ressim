package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Translation and dilation transform.
 */
public class TranslateDilateTransform extends Transform {

    private Point3D[] points;

    private double x0, y0, z0;

    private double dx, dy, dz;

    private double dxt, dyt, dzt;

    private double xt0, yt0, zt0;

    /**
     * Translates and dilates the given set of points to lie within a given
     * global target box.
     * 
     * x_t = dx_t/dx *(x-x_0) + x_t0
     * 
     * dx = x_max - x_min : geometry lengths for given points
     * 
     * dxt = x_tmax - x_tmin : lengths for target points
     */
    public TranslateDilateTransform(Configuration config,
            CartesianTopology topology, Point3D[] points) {
        if (topology.getDimension() != 3)
            throw new IllegalArgumentException(config.trace()
                    + "TranslateDilateTransform is only valid for 3D mesh.");

        this.points = points;

        double[] targetOrigin = config.getDoubleArray("TargetOrigin");
        double[] targetLength = config.getDoubleArray("TargetLengths");

        checkInput(config.trace(), targetOrigin, targetLength);

        setup(targetOrigin, targetLength);
    }

    /**
     * Translates and dilates the given points to lie within the rectangular box
     * with corner point at the given origin and with the given dimensions.
     */
    public TranslateDilateTransform(double[] targetOrigin,
            double[] targetLength, CartesianTopology topology, Point3D[] points) {
        if (topology.getDimension() != 3)
            throw new IllegalArgumentException(
                    "TranslateDilateTransform is only valid for 3D mesh.");

        this.points = points;

        checkInput("", targetOrigin, targetLength);

        setup(targetOrigin, targetLength);
    }

    private void checkInput(String msg, double[] targetOrigin,
            double[] targetLength) {
        if (targetOrigin.length != 3)
            throw new IllegalArgumentException(msg
                    + "Origin must be specified with 3 doubles");
        if (targetLength.length != 3)
            throw new IllegalArgumentException(msg
                    + "Lengths must be specified with 3 doubles");

    }

    private void setup(double[] targetOrigin, double[] targetLength) {
        xt0 = targetOrigin[0];
        yt0 = targetOrigin[1];
        zt0 = targetOrigin[2];

        dxt = targetLength[0];
        dyt = targetLength[1];
        dzt = targetLength[2];

        x0 = points[0].x();
        y0 = points[0].y();
        z0 = points[0].z();

        double xn = points[points.length - 1].x();
        double yn = points[points.length - 1].y();
        double zn = points[points.length - 1].z();

        dx = xn - x0;
        dy = yn - y0;
        dz = zn - z0;

        if (!(dx > 0))
            throw new IllegalArgumentException(
                    "X-direction length must be positive");

        if (!(dy > 0))
            throw new IllegalArgumentException(
                    "Y-direction length must be positive");

        if (!(dz < 0))
            throw new IllegalArgumentException(
                    "Z-direction length must be negative");
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double x = p.x();
        double y = p.y();
        double z = p.z();

        double xt = dxt / dx * (x - x0) + xt0;
        double yt = dyt / dy * (y - y0) + yt0;
        double zt = dzt / dz * (z - z0) + zt0;

        return new Point3D(xt, yt, zt);
    }
}
