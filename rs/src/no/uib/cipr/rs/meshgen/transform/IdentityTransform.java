package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Identity transform.
 */
public class IdentityTransform extends Transform {

    private Point3D[] points;

    /**
     * Creates an identity transform
     * 
     * @param config
     *            Not used
     * @param topology
     *            Not used
     * @param points
     *            Not used
     */
    public IdentityTransform(@SuppressWarnings("unused")
    Configuration config, @SuppressWarnings("unused")
    CartesianTopology topology, Point3D[] points) {
        this.points = points;
    }

    public IdentityTransform(@SuppressWarnings("unused")
    Configuration config, Point3D[] points) {
        this.points = points;
    }

    @Override
    public Point3D getPoint(int i) {
        return points[i];
    }
}
