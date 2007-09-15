package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Rotates the mesh about the y-axis an angle specified in radians and
 * translates along the z-axis a specified distance. Angle and translation both
 * defaults to zero.
 */
public class RotateYTransZTransform extends Transform {

    // translation distance
    private double dz;

    private Point3D[] points;

    // rotation coefficients
    private double rx;

    private double rz;

    public RotateYTransZTransform(Configuration config,
            @SuppressWarnings("unused")
            CartesianTopology topology, Point3D[] points) {
        this(config, points);
    }

    /**
     * Creates a rotate-y transform.
     */
    public RotateYTransZTransform(Configuration config, Point3D[] points) {
        this.points = points;

        // read the y-axis rotation angle, defaults to zero
        double theta = config.getDouble("Theta", 0.0);

        // read the z-axis translation, defaults to zero
        dz = config.getDouble("Dz", 0.0);

        // compute rotation coefficients
        rx = Math.cos(theta);
        rz = Math.sin(theta);
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double x = p.x();
        double y = p.y();
        double z = p.z();

        double X = x * rx - z * rz;
        double Z = x * rz + z * rx + dz;

        return new Point3D(X, y, Z);
    }

}
