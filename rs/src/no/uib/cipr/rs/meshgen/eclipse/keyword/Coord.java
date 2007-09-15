package no.uib.cipr.rs.meshgen.eclipse.keyword;

import no.uib.cipr.rs.util.Configuration;

/**
 * This class contains data that implies corner point geometry in Eclipse. The
 * data specifies (NDIVIX+1)(NDIVIY+1)NUMRES coordinate lines, each specified by
 * two points, each consisting of 3 values, namely the X, Y and Z coordinates.
 * Reference: Eclipse Reference Manual, pp. 3-186
 * 
 * This class is responsible for returning data in a more naive way.
 * 
 */
public class Coord {

    // the size of this array is [numPillars][3+3]
    // each row stores the XYZ coordinates of two points defining the pillar
    // (i.e. CoordLine).
    private double[][] coord;

    /**
     * @param coordPoints
     */
    public Coord(double[] coordPoints) {

        int numCP = 6;
        int numPillars = coordPoints.length / 6;

        coord = new double[numPillars][numCP];

        for (int pillar = 0, i = 0; pillar < numPillars; pillar++)
            for (int point = 0; point < numCP; point++, i++)
                coord[pillar][point] = coordPoints[i];

    }

    /**
     * Creates Coord from configuration containing a "coord" configuration with
     * double array "coord"
     * 
     * @param config
     *            A Configuration
     */
    public Coord(Configuration config) {
        this(config.getConfiguration("coord").getDoubleArray("coord"));
    }

    /**
     * Returns the x- and y- coordinates for pillar coordIndex at depth d.
     * 
     * This routine could benefit from package with standard vector operations.
     * 
     * @param coordIndex
     * @param depth
     * @return xy array
     * @throws RuntimeException
     */
    public double[] getXY(int coordIndex, double depth) throws RuntimeException {
        double[] xy = new double[2];

        // p1
        double x1 = coord[coordIndex][0];
        double y1 = coord[coordIndex][1];
        double z1 = coord[coordIndex][2];

        // p2
        double x2 = coord[coordIndex][3];
        double y2 = coord[coordIndex][4];
        double z2 = coord[coordIndex][5];

        // check if degenerate input
        if (x1 == x2 && y1 == y2) {
            xy[0] = x1;
            xy[1] = y1;
            return xy;
        }
        if (z2 == z1)
            throw new RuntimeException(" Undefined COORD, z2 = z1...");

        // calculate length of line _r_=_p2p1_ defining COORD
        double rx = x2 - x1;
        double ry = y2 - y1;
        double rz = z2 - z1;

        // find intersection point, p0, between pillar and zero-depth
        double x0 = x1 - z1 * rx / rz;
        double y0 = y1 - z1 * ry / rz;

        // find the fractional length s of vector from zero-depth to depth d
        // parallel to r
        double s = depth / rz;

        // calculate actual point on pillar
        xy[0] = x0 + s * rx;
        xy[1] = y0 + s * ry;

        return xy;
    }

}
