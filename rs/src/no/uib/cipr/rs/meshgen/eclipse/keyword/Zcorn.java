package no.uib.cipr.rs.meshgen.eclipse.keyword;

import no.uib.cipr.rs.util.Configuration;

/**
 * This class contains data to specify corner point geometry in Eclipse.
 * Specifically, this class stores the depths, d, of grid block corners. Thus,
 * the z-coordinate for a corner point is z = -d.
 * 
 * Reference: Eclipse Reference Manual, pp. 3-1683
 * 
 * The keyword line is followed by 2*NDIVIX*2*NDIVIY*2*NDIVIZ values, with the
 * two corners in the I-direction of the first grid block being specified first,
 * then two corners for the next block in the i-direction.
 */
public class Zcorn {

    private int numDepths; // in general, this is identical to number of

    // corner points

    private double[] depths;

    /**
     * @param depths
     *            zCornDepths
     */
    public Zcorn(double[] depths) {
        numDepths = depths.length;
        this.depths = depths;
    }

    /**
     * Creates Zcorn from configuration containing a "zcorn" configuration with
     * double array "zcorn"
     * 
     * @param config
     *            A Configuration
     */
    public Zcorn(Configuration config) {
        this(config.getConfiguration("zcorn").getDoubleArray("zcorn"));
    }

    /**
     * Returns the number of depths, i.e. number of cell specific corner points
     */
    public int getNumDepths() {
        return numDepths;
    }

    /**
     * @param i
     *            Corner point index following the numbering of the ZCORN
     *            keyword.
     * @return depth
     */
    public double getDepth(int i) {
        return depths[i];
    }

}
