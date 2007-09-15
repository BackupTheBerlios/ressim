package no.uib.cipr.rs.meshgen.transform;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.util.Configuration;

/**
 * Perturbation transform.
 */
public class PerturbationTransform extends Transform {

    private double alpha;

    private Point3D[] points;

    /**
     * Creates a perturbation transform.
     * 
     * @param config
     *            Perturbation transform
     * @param topology
     *            Not used
     * @param points
     *            Not used
     */
    public PerturbationTransform(Configuration config,
            CartesianTopology topology, Point3D[] points) {
        if (topology.getDimension() == 1)
            throw new IllegalArgumentException(config.trace()
                    + "PerturbationTransformation is not valid for 1D mesh.");

        alpha = config.getDouble("alpha");

        this.points = new Point3D[topology.getNumPoints()];

        // recompute all points
        int ni = topology.getNumPointsI();
        int nj = topology.getNumPointsJ();

        for (IJK ijk : topology.getPointsIJK()) {
            int i = ijk.i();
            int j = ijk.j();
            int k = ijk.k();

            // skip if boundary cell
            if (i < 2 || i > ni - 3) {
                int index = topology.getLinearPoint(ijk);
                this.points[index] = points[index];
                continue;
            }

            if (j < 2 || j > nj - 3) {
                int index = topology.getLinearPoint(ijk);
                this.points[index] = points[index];
                continue;
            }

            IJK right = new IJK(i + 1, j, k);
            IJK left = new IJK(i - 1, j, k);

            IJK back = new IJK(i, j + 1, k);
            IJK front = new IJK(i, j - 1, k);

            Point3D p = points[topology.getLinearPoint(ijk)];

            Point3D pl = points[topology.getLinearPoint(left)];
            Point3D pr = points[topology.getLinearPoint(right)];
            Point3D pb = points[topology.getLinearPoint(back)];
            Point3D pf = points[topology.getLinearPoint(front)];

            double x = p.x() + alpha * (Math.random() - 0.5)
                    * Math.min(pr.x() - p.x(), p.x() - pl.x());
            double y = p.y() + alpha * (Math.random() - 0.5)
                    * Math.min(pb.y() - p.y(), p.y() - pf.y());
            double z = p.z();

            this.points[topology.getLinearPoint(ijk)] = new Point3D(x, y, z);
        }
    }

    @Override
    public Point3D getPoint(int i) {
        return this.points[i];
    }

}
