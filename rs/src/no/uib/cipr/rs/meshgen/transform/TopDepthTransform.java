package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Function;

/**
 * Top depth transform.
 */
public class TopDepthTransform extends Transform {

    private Function table;

    private Point3D[] points;

    /**
     * Creates a top depth transform. The transformation is as follows from
     * non-transformed z-values to depth-transformed: z -> z - dz, where dz are
     * the given top depths. For any dz > 0 the transformed point is deeper than
     * before transformation.
     * 
     * @param config
     *            Top depth transform
     * @param topology
     *            Not used
     * @param points
     *            Not used
     */
    public TopDepthTransform(Configuration config, @SuppressWarnings("unused")
    CartesianTopology topology, Point3D[] points) {
        this.points = points;

        table = config.getFunction("TopDepth", 2);
    }

    public TopDepthTransform(Configuration config, Point3D[] points) {
        this.points = points;

        table = config.getFunction("TopDepth", 2);
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double x = p.x();
        double y = p.y();
        double z = p.z();

        double dz = table.get(x, y);

        return new Point3D(x, y, z - dz);
    }
}
