package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Function;

/**
 * Top-bottom transform.
 */
public class TopBottomTransform extends Transform {

    private Function top, bottom;

    private Point3D[] points;

    private double dz;

    private double ztop;

    public TopBottomTransform(Configuration config, @SuppressWarnings("unused")
    CartesianTopology topology, Point3D[] points) {
        this(config, points);
    }

    /**
     * Creates a top and bottom surface conforming transform. The transformation
     * is as follows from non-transformed, z, to transformed, Z, depths:
     * 
     * Z(x,y) = dZ(x,y)/dz(x,y) * (z(x,y)- ztop(x,y)) + Ztop(x,y),
     * 
     * where
     * 
     * dZ(x,y) = Zbottom(x,y)-Ztop(x,y),
     * 
     * dz(x,y) = zbottom(x,y)-ztop(x,y).
     * 
     * The top surface Ztop(x,y) and the bottom surface Zbottom(x,y) must be
     * given such that Ztop(x,y) > Zbottom(x,y) for all x,y.
     * 
     * @param config
     *            Top-bottom transform
     * @param points
     *            Not used
     */
    public TopBottomTransform(Configuration config, Point3D[] points) {
        this.points = points;

        dz = config.getDouble("Dz");
        if (!(dz < 0.0))
            throw new IllegalArgumentException("Dz must be negative");

        ztop = config.getDouble("ZTop");

        top = config.getFunction("TopSurface", 2);
        bottom = config.getFunction("BottomSurface", 2);
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double x = p.x();
        double y = p.y();
        double z = p.z();

        double Ztop = -top.get(x, y);
        double dZ = -bottom.get(x, y) - Ztop;

        double Z = dZ / dz * (z - ztop) + Ztop;

        return new Point3D(x, y, Z);
    }
}
