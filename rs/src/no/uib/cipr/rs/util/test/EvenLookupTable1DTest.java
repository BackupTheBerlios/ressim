package no.uib.cipr.rs.util.test;

import no.uib.cipr.rs.util.Function;
import no.uib.cipr.rs.util.EvenLookupTable;

/**
 * Tests 1D evenly spaced lookup tables
 */
public class EvenLookupTable1DTest extends LookupTable1DTest {

    private double x0, dx;

    @Override
    protected double[] getCoord() {
        x0 = 0;
        dx = 1 / ((double) n - 1);

        double[] coord = new double[n];
        for (int i = 1; i < n; ++i)
            coord[i] = coord[i - 1] + dx;

        return coord;
    }

    @Override
    protected Function createFunction(String name, double[] data) {
        return new EvenLookupTable(name, new double[] { x0 },
                new double[] { dx }, data);
    }
}
