package no.uib.cipr.rs.meshgen.transform;

import java.util.Random;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.util.Configuration;

/**
 * Random perturbation transform that transforms all mesh points by a random
 * distortion of positions of the mesh points. It is assumed that the given
 * points are part of a uniform cubic mesh with cells having sides of length
 * 'dh'. The new points are computed as follows (ex. for x-coordinate):
 * 
 * x <- x + sx*dh,
 * 
 * where sx is a random number between -0.3 and 0.3.
 */
public class RandomPerturbationTransform extends Transform {
    private Point3D[] points;

    private double dh;

    private Random rand;

    /**
     * Creates a random perturbation transform.
     * 
     * @param config
     *            Perturbation transform
     * @param topology
     *            Not used
     * @param points
     *            Not used
     */
    public RandomPerturbationTransform(Configuration config,
            CartesianTopology topology, Point3D[] points) {
        if (topology.getDimension() == 1)
            throw new IllegalArgumentException(config.trace()
                    + "PerturbationTransformation is not valid for 1D mesh.");

        long seed = config.getInt("seed", Integer.MAX_VALUE);

        dh = config.getDouble("dh");

        rand = new Random(seed);

        this.points = points;
    }

    @Override
    public Point3D getPoint(int i) {
        Point3D p = points[i];

        double sx = (rand.nextBoolean() ? -1 : 1) * rand.nextDouble() * 0.3;
        double sy = (rand.nextBoolean() ? -1 : 1) * rand.nextDouble() * 0.3;
        double sz = (rand.nextBoolean() ? -1 : 1) * rand.nextDouble() * 0.3;

        return new Point3D(p.x() + sx * dh, p.y() + sy * dh, p.z() + sz * dh);
    }
}
