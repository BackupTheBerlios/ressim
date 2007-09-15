package no.uib.cipr.rs.util.test;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;
import no.uib.cipr.rs.util.Function;
import no.uib.cipr.rs.util.LookupTable;

/**
 * Tests 1D lookup tables
 */
public class LookupTable1DTest extends TestCase {

    protected int n;

    private int repeat;

    private double a, e;

    protected double[] xcoord;

    @Override
    protected void setUp() throws Exception {
        Random r = new Random();
        n = r.nextInt(1000) + 2;
        repeat = r.nextInt(100);
        a = Math.random();
        e = Math.random();

        xcoord = getCoord();
    }

    public void testConstant1D() {
        double[] data = new double[n];
        for (int i = 0; i < n; ++i)
            data[i] = e;

        Function f = createFunction("Constant 1D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double v = f.get(x);
            double dvdx = f.deriv(0, x);

            assertEquals(e, v, 1e-14);
            assertEquals(0, dvdx, 1e-14);
        }
    }

    public void testLinear1D() {
        double[] data = new double[n];
        for (int i = 0; i < n; ++i)
            data[i] = a * xcoord[i] + e;

        Function f = createFunction("Linear 1D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double v = f.get(x);
            double dvdx = f.deriv(0, x);

            assertEquals(a * x + e, v, 1e-14);
            assertEquals(a, dvdx, 1e-14);
        }
    }

    public void testTrigonometric1D() {
        double[] data = new double[n];
        for (int i = 0; i < n; ++i)
            data[i] = a * Math.sin(xcoord[i]) + e;

        Function f = createFunction("Trigonometric 1D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double v = f.get(x);
            double dvdx = f.deriv(0, x);

            assertEquals(a * Math.sin(x) + e, v, 1e-4);
            assertEquals(a * Math.cos(x), dvdx, 1e-4);
        }
    }

    protected double[] getCoord() {
        double[] coord = new double[n];
        coord[0] = 0;
        coord[1] = 1;
        for (int i = 2; i < n; ++i)
            coord[i] = Math.random();
        Arrays.sort(coord);

        return coord;
    }

    protected Function createFunction(String name, double[] data) {
        return new LookupTable(name, xcoord, data);
    }
}
