package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Parallelogram transform. Shifting the coordinates of a given direction
 * according to
 * 
 * \vec{x} <- P\vec{x}
 * 
 * where _ _ | 1 a1 b1 | P = | a2 1 b2 | |_a3 b3 1_|
 * 
 * Non-specified constants defaults to zero
 * 
 */
public class ParallelogramTransform extends Transform {

    private final double a1, a2, a3, b1, b2, b3;

    private Point3D[] points;

    /**
     * Creates a parallelogram transform
     */
    public ParallelogramTransform(Configuration config, Point3D[] points) {
        a1 = config.getDouble("a1", 0);
        a2 = config.getDouble("a2", 0);
        a3 = config.getDouble("a3", 0);
        b1 = config.getDouble("b1", 0);
        b2 = config.getDouble("b2", 0);
        b3 = config.getDouble("b3", 0);

        this.points = points;
    }

    /**
     * Creates a parallelogram transform.
     */
    public ParallelogramTransform(Configuration config,
            CartesianTopology topology, Point3D[] points) {
        this(config, points);

        if (topology.getDimension() != 3)
            throw new IllegalArgumentException(config.trace()
                    + "ParallelogramTransform is only valid for 3D mesh.");
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double x = p.x();
        double y = p.y();
        double z = p.z();

        double xx = x + a1 * y + b1 * z;
        double yy = y + a2 * x + b2 * z;
        double zz = z + a3 * x + b3 * y;

        return new Point3D(xx, yy, zz);
    }
}
