package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.util.Configuration;

/**
 * Skew transform.
 */
public class SkewTransform extends Transform {

    private Point3D[] points;

    /**
     * Creates a skew transform
     * 
     * @param config
     *            Configuration containing data for the transformation
     * @param topology
     *            A structured topology
     * @param points
     *            Logical space corner points
     */
    public SkewTransform(Configuration config, CartesianTopology topology,
            Point3D[] points) {
        if (topology.getDimension() != 2)
            throw new IllegalArgumentException(config.trace()
                    + "SkewTransformation is only valid for 2D mesh.");

        double[] tx = config.getDoubleArray("TopX");
        double[] bx = config.getDoubleArray("BottomX");

        checkConsistency(config, tx, bx);

        int nt = topology.getNumElementI();

        if (nt % 3 != 0)
            throw new IllegalArgumentException(
                    config.trace()
                            + "Number of elements in x-direction must be dividable by 3");

        // number of elements per 3-domain
        int nd = nt / 3;

        if (nd % 2 != 0)
            throw new IllegalArgumentException(
                    config.trace()
                            + "Number of elements in x-direction for center domain must be dividable by 2");

        // number of elements per 2-domain (center of 3-domain)
        int nc = nd / 2;

        // array of dx for top and bottom indexable by ijk.i()
        double[] topx = getXPoints(tx, nt, nd, nc);
        double[] bottomx = getXPoints(bx, nt, nd, nc);

        double yt = points[topology.getNumPoints() - 1].y();
        double yb = points[0].y();

        double dy = yt - yb;

        this.points = new Point3D[topology.getNumPoints()];

        for (IJK ijk : topology.getPointsIJK()) {
            double xt = topx[ijk.i()];
            double xb = bottomx[ijk.i()];
            double dx = xt - xb;

            int i = topology.getLinearPoint(ijk);

            double z = points[i].z();
            double y = points[i].y();
            double x = (dx / dy) * (y - yt) + xt;

            this.points[i] = new Point3D(x, y, z);
        }

    }

    private double[] getXPoints(double[] p, int nt, int nd, int nc) {
        double[] x = new double[nt + 1];

        double dx1 = (p[1] - p[0]) / nd;
        double dx2 = (p[2] - p[1]) / nc;
        double dx3 = (p[3] - p[2]) / nc;
        double dx4 = (p[4] - p[3]) / nd;

        // global counter
        int index = 0;

        // first domain
        for (int i = 0; i <= nd; i++) {
            x[index++] = p[0] + i * dx1;
        }
        // second domain
        for (int i = 1; i <= nc; i++) {
            x[index++] = p[1] + i * dx2;
        }
        // third domain
        for (int i = 1; i <= nc; i++) {
            x[index++] = p[2] + i * dx3;
        }
        // fourth domain
        for (int i = 1; i <= nd; i++) {
            x[index++] = p[3] + i * dx4;
        }

        return x;
    }

    private void checkConsistency(Configuration config, double[] tx, double[] bx) {
        if (tx.length != 5)
            throw new IllegalArgumentException(config.trace()
                    + "'TopX' must have 5 values");

        if (bx.length != 5)
            throw new IllegalArgumentException(config.trace()
                    + "'BottomX' must have 5 values");

        if (tx[0] != bx[0])
            throw new IllegalArgumentException(config.trace()
                    + "First top and bottom coordinates must be equal");
        if (tx[1] != bx[1])
            throw new IllegalArgumentException(config.trace()
                    + "Second top and bottom coordinates must be equal");
        if (tx[3] != bx[3])
            throw new IllegalArgumentException(config.trace()
                    + "Fourth top and bottom coordinates must be equal");
        if (tx[4] != bx[4])
            throw new IllegalArgumentException(config.trace()
                    + "Fifth top and bottom coordinates must be equal");

        if (!((tx[0] < tx[1]) && (tx[1] < tx[2]) && (tx[2] < tx[3]) && (tx[3] < tx[4])))
            throw new IllegalArgumentException(config.trace()
                    + "'TopX' coordinates must be increasing");

        if (!((bx[0] < bx[1]) && (bx[1] < bx[2]) && (bx[2] < bx[3]) && (bx[3] < bx[4])))
            throw new IllegalArgumentException(config.trace()
                    + "'BottomX' coordinates must be increasing");
    }

    @Override
    public Point3D getPoint(int i) {
        return points[i];
    }
}
