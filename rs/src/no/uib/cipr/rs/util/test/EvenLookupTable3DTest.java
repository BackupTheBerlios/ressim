package no.uib.cipr.rs.util.test;

import no.uib.cipr.rs.util.Function;
import no.uib.cipr.rs.util.EvenLookupTable;

/**
 * Tests 3D evenly spaced lookup tables
 */
public class EvenLookupTable3DTest extends LookupTable3DTest {

    private double[] offset, delta;

    @Override
    protected void setUp() throws Exception {
        offset = new double[3];
        delta = new double[3];

        super.setUp();
    }

    @Override
    protected double[] getCoord(int dim) {
        offset[dim] = 0;
        delta[dim] = 1 / ((double) n[dim] - 1);

        double[] coord = new double[n[dim]];
        for (int i = 1; i < n[dim]; ++i)
            coord[i] = coord[i - 1] + delta[dim];

        return coord;
    }

    @Override
    protected Function createFunction(String name, double[][][] data) {
        return new EvenLookupTable(name, offset, delta, data);
    }
}
