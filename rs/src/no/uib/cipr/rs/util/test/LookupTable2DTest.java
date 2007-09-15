package no.uib.cipr.rs.util.test;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;
import no.uib.cipr.rs.util.Function;
import no.uib.cipr.rs.util.LookupTable;

/**
 * Tests 2D lookup tables
 */
public class LookupTable2DTest extends TestCase {

    protected int n[];

    private int repeat;

    private double a, b, e;

    protected double[] xcoord, ycoord;

    @Override
    protected void setUp() throws Exception {
        Random r = new Random();
        n = new int[2];
        n[0] = r.nextInt(1000) + 2;
        n[1] = r.nextInt(1000) + 2;
        repeat = r.nextInt(100);
        a = Math.random();
        b = Math.random();
        e = Math.random();

        xcoord = getCoord(0);
        ycoord = getCoord(1);
    }

    protected double[] getCoord(int dim) {
        double[] coord = new double[n[dim]];
        coord[0] = 0;
        coord[1] = 1;
        for (int i = 2; i < n[dim]; ++i)
            coord[i] = Math.random();
        Arrays.sort(coord);

        return coord;
    }

    public void testConstant2D() {
        double[][] data = new double[n[0]][n[1]];
        for (int i = 0; i < n[0]; ++i)
            for (int j = 0; j < n[1]; ++j)
                data[i][j] = e;

        Function f = createFunction("Constant 2D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double y = Math.random();
            double v = f.get(x, y);
            double dvdx = f.deriv(0, x, y);
            double dvdy = f.deriv(1, x, y);

            assertEquals(e, v, 1e-14);
            assertEquals(0, dvdx, 1e-14);
            assertEquals(0, dvdy, 1e-14);
        }
    }

    public void testLinear2D() {
        double[][] data = new double[n[0]][n[1]];
        for (int i = 0; i < n[0]; ++i)
            for (int j = 0; j < n[1]; ++j)
                data[i][j] = a * xcoord[i] + b * ycoord[j] + e;

        Function f = createFunction("Linear 2D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double y = Math.random();
            double v = f.get(x, y);
            double dvdx = f.deriv(0, x, y);
            double dvdy = f.deriv(1, x, y);

            assertEquals(a * x + b * y + e, v, 1e-14);
            assertEquals(a, dvdx, 1e-14);
            assertEquals(b, dvdy, 1e-14);
        }
    }

    public void testTrigonometric2D() {
        double[][] data = new double[n[0]][n[1]];
        for (int i = 0; i < n[0]; ++i)
            for (int j = 0; j < n[1]; ++j)
                data[i][j] = a * Math.sin(xcoord[i]) + b * Math.sin(ycoord[j])
                        + e;

        Function f = createFunction("Trigonometric 2D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double y = Math.random();
            double v = f.get(x, y);
            double dvdx = f.deriv(0, x, y);
            double dvdy = f.deriv(1, x, y);

            assertEquals(a * Math.sin(x) + b * Math.sin(y) + e, v, 1e-4);
            assertEquals(a * Math.cos(x), dvdx, 1e-4);
            assertEquals(b * Math.cos(y), dvdy, 1e-4);
        }
    }

    protected Function createFunction(String name, double[][] data) {
        return new LookupTable(name, xcoord, ycoord, data);
    }
}
