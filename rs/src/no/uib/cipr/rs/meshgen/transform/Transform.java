package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Tranformation of a point set.
 */
public abstract class Transform {

    /**
     * Returns the point coordinates with the given index.
     */
    public abstract Point3D getPoint(int i);

    /**
     * Creates a transformation without an underlying toplogy. Defaults to an
     * identity transform
     */
    public static Transform create(Configuration config,
            Point3D[] nonTransPoints) {
        return config.getObject("Transform", Transform.class,
                IdentityTransform.class, (Object) nonTransPoints);
    }

    /**
     * Creates a transformation for a topologically structured mesh. Defaults to
     * an identity transform
     */
    public static Transform create(Configuration config,
            CartesianTopology topology, Point3D[] nonTransPoints) {
        return config.getObject("StructuredTransform", Transform.class,
                IdentityTransform.class, topology, (Object) nonTransPoints);
    }
}
