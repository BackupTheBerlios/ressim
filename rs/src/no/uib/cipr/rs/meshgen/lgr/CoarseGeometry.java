package no.uib.cipr.rs.meshgen.lgr;

import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology3D;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.util.Configuration;

class CoarseGeometry {

    private CartesianTopology topology;

    private double[] deltaX, deltaY, deltaZ;

    private double[] x, y, z;

    /**
     * Creates a coarse geometry from the given configuration
     */
    public CoarseGeometry(Configuration config) {
        Configuration sub = config.getConfiguration("CoarseGeometry");

        double x0 = sub.getDouble("X0");
        double y0 = sub.getDouble("Y0");
        double z0 = sub.getDouble("Z0");

        int[] nx = sub.getIntArray("Nx");
        int[] ny = sub.getIntArray("Ny");
        int[] nz = sub.getIntArray("Nz");

        double[] dx = sub.getDoubleArray("Dx");
        double[] dy = sub.getDoubleArray("Dy");
        double[] dz = sub.getDoubleArray("Dz");

        checkParts(nx, ny, nz, dx, dy, dz);

        // find total number of cells in each direction
        int numX = ArrayData.getTotal(nx);
        int numY = ArrayData.getTotal(ny);
        int numZ = ArrayData.getTotal(nz);

        topology = new CartesianTopology3D(sub, numX, numY, numZ);

        // expand deltas
        deltaX = ArrayData.getExpanded(nx, dx);
        deltaY = ArrayData.getExpanded(ny, dy);
        deltaZ = ArrayData.getExpanded(nz, dz);

        // expand point coordinates
        x = ArrayData.getExpandedPoints(x0, deltaX);
        y = ArrayData.getExpandedPoints(y0, deltaY);
        z = ArrayData.getExpandedPoints(z0, deltaZ);
    }

    public CartesianTopology getTopology() {
        return topology;
    }

    /**
     * Returns the length of the given element.
     */
    public double[] getElementLength(IJK ijk) {
        return new double[] { deltaX[ijk.i()], deltaY[ijk.j()], deltaZ[ijk.k()] };
    }

    /**
     * Return the origin coordinates of the given element.
     */
    public double[] getElementOrigin(IJK ijk) {
        return new double[] { x[ijk.i()], y[ijk.j()], z[ijk.k()] };
    }

    /**
     * Checks parts for consistent values.
     * 
     * Number of parts must be equal for nx-dx, ny-dy and nz-dz. nx, ny and nz
     * must all be positive. dx and dy must be positive and dz must be negative
     */
    private void checkParts(int[] nx, int[] ny, int[] nz, double[] dx,
            double[] dy, double[] dz) {
        if (nx.length != dx.length)
            throw new IllegalArgumentException(
                    "the number of nx and dx values are not equal");
        if (ny.length != dy.length)
            throw new IllegalArgumentException(
                    "the number of ny and dy values are not equal");
        if (nz.length != dz.length)
            throw new IllegalArgumentException(
                    "the number of nz and dz values are not equal");

        for (int val : nx)
            if (!(val > 0))
                throw new IllegalArgumentException("nx must be positive: nx = "
                        + val);
        for (int val : ny)
            if (!(val > 0))
                throw new IllegalArgumentException("ny must be positive: ny = "
                        + val);
        for (int val : nz)
            if (!(val > 0))
                throw new IllegalArgumentException("nz must be positive: nz = "
                        + val);

        for (double val : dz)
            if (!(val < 0.0))
                throw new IllegalArgumentException("dz must be negative: dz = "
                        + val);
        for (double val : dy)
            if (!(val > 0.0))
                throw new IllegalArgumentException("dy must be positive: dy = "
                        + val);
        for (double val : dx)
            if (!(val > 0.0))
                throw new IllegalArgumentException("dx must be positive: dx = "
                        + val);
    }

}
