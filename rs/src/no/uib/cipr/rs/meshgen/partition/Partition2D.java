package no.uib.cipr.rs.meshgen.partition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology2D;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.util.Configuration;

/**
 * The geometry of partition regions in 2D.
 */
public class Partition2D {

    private double x0;

    private double y0;

    private int z0;

    private int numX;

    private int numY;

    private double[] deltaX;

    private double[] deltaY;

    private double[] x;

    private double[] y;

    private double[] z;

    private CartesianTopology2D topology;

    private Point3D[] p;

    private Domain2D[] domains;

    private double border;

    /**
     * Creates the regions from the given configuration.
     */
    public Partition2D(Configuration config) {
        border = config.getDouble("Border");

        Configuration dConfig = config.getConfiguration("Domains");

        x0 = dConfig.getDouble("X0");
        y0 = dConfig.getDouble("Y0");
        z0 = 0;

        int[] nx = dConfig.getIntArray("Nx");
        int[] ny = dConfig.getIntArray("Ny");

        double[] dx = dConfig.getDoubleArray("Dx");
        double[] dy = dConfig.getDoubleArray("Dy");
        double[] dz = { -1 };

        checkParts(nx, ny, dx, dy, dz);

        // find total number of cells in each direction
        numX = ArrayData.getTotal(nx);
        numY = ArrayData.getTotal(ny);

        // expand deltas
        deltaX = ArrayData.getExpanded(nx, dx);
        deltaY = ArrayData.getExpanded(ny, dy);
        x = ArrayData.getExpandedPoints(x0, deltaX);
        y = ArrayData.getExpandedPoints(y0, deltaY);
        z = new double[] { z0 };

        topology = new CartesianTopology2D(dConfig, numX, numY);

        // store non-transformed point coordinates
        p = new Point3D[topology.getNumPoints()];
        for (IJK ijk : topology.getPointsIJK())
            p[topology.getLinearPoint(ijk)] = new Point3D(x[ijk.i()],
                    y[ijk.j()], z[ijk.k()]);

        domains = new Domain2D[topology.getNumElements()];

        for (IJK ijk : topology.getElementsIJK()) {
            int[] cp = topology.getElementPoints(ijk);

            int domainIndex = topology.getLinearElement(ijk);

            Domain2D domain = new Domain2D(p[cp[0]], p[cp[1]], p[cp[2]],
                    p[cp[3]], border, domainIndex);

            domains[domainIndex] = domain;
        }
    }

    /**
     * Checks parts for consistent values. The number of parts must be equal for
     * Nx-Dx, Ny-Dy. Nx and Ny must all be positive. Dx and Dy must be positive
     * and Dz must be negative.
     */
    private void checkParts(int[] nx, int[] ny, double[] dx, double[] dy,
            double[] dz) {
        if (nx.length != dx.length)
            throw new IllegalArgumentException(
                    "the number of Nx and Dx values are not equal");
        if (ny.length != dy.length)
            throw new IllegalArgumentException(
                    "the number of Ny and Dy values are not equal");
        if (dz.length != 1)
            throw new IllegalArgumentException(
                    "the number of Dz values must be 1 for 2D geometry");

        for (int val : nx)
            if (!(val > 0))
                throw new IllegalArgumentException("Nx must be positive");
        for (int val : ny)
            if (!(val > 0))
                throw new IllegalArgumentException("Ny must be positive");

        for (double val : dz)
            if (!(val < 0.0))
                throw new IllegalArgumentException("Dz must be�negative");
        for (double val : dy)
            if (!(val > 0.0))
                throw new IllegalArgumentException("Dy must be positive");
        for (double val : dx)
            if (!(val > 0.0))
                throw new IllegalArgumentException("Dx must be positive");
    }

    /**
     * Returns the number of domains in this partition.
     */
    public int getNumDomains() {
        return domains.length;
    }

    /**
     * Returns the domain with the given linear index.
     */
    public Domain2D getDomain(int i) {
        return domains[i];
    }

    /**
     * Returns a collection of the domains in this partition.
     */
    public Collection<Domain2D> domains() {
        return Arrays.asList(domains);
    }

    /**
     * Enumerate the boundaries with in the partition. The collection
     * of boundaries are a set of lines which divides the domains in the
     * partition from eachother.
     * 
     * @param set
     *  Set to which the boundaries will be added. To get the boundaries
     *  for this partition only, pass a new Set as parameter.
     * @return
     *  The same set that was passed as argument to the method, to allow
     *  piping of the set through various sources.
     */
    public Set<Segment3D> boundaries(Set<Segment3D> set) {
        // add the boundaries for all the domains in the partition
        for(Domain2D domain : domains()) {
            domain.boundaries(set);
        }
        // return the complete set of boundaries for the partition
        return set;
    }
}
