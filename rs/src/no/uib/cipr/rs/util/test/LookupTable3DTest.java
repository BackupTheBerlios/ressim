package no.uib.cipr.rs.util.test;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;
import no.uib.cipr.rs.util.Function;
import no.uib.cipr.rs.util.LookupTable;

/**
 * Tests 3D lookup tables
 */
public class LookupTable3DTest extends TestCase {

    protected int n[];

    private int repeat;

    private double a, b, c, e;

    protected double[] xcoord, ycoord, zcoord;

    @Override
    protected void setUp() throws Exception {
        Random r = new Random();
        n = new int[3];
        n[0] = r.nextInt(100) + 2;
        n[1] = r.nextInt(100) + 2;
        n[2] = r.nextInt(100) + 2;
        repeat = r.nextInt(100);
        a = Math.random();
        b = Math.random();
        c = Math.random();
        e = Math.random();

        xcoord = getCoord(0);
        ycoord = getCoord(1);
        zcoord = getCoord(2);
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

    public void testConstant3D() {
        double[][][] data = new double[n[0]][n[1]][n[2]];
        for (int i = 0; i < n[0]; ++i)
            for (int j = 0; j < n[1]; ++j)
                for (int k = 0; k < n[2]; ++k)
                    data[i][j][k] = e;

        Function f = createFunction("Constant 3D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double y = Math.random();
            double z = Math.random();
            double v = f.get(x, y, z);
            double dvdx = f.deriv(0, x, y, z);
            double dvdy = f.deriv(1, x, y, z);
            double dvdz = f.deriv(2, x, y, z);

            assertEquals(e, v, 1e-14);
            assertEquals(0, dvdx, 1e-14);
            assertEquals(0, dvdy, 1e-14);
            assertEquals(0, dvdz, 1e-14);
        }
    }

    public void testLinear3D() {
        double[][][] data = new double[n[0]][n[1]][n[2]];
        for (int i = 0; i < n[0]; ++i)
            for (int j = 0; j < n[1]; ++j)
                for (int k = 0; k < n[2]; ++k)
                    data[i][j][k] = a * xcoord[i] + b * ycoord[j] + c
                            * zcoord[k] + e;

        Function f = createFunction("Linear 3D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double y = Math.random();
            double z = Math.random();
            double v = f.get(x, y, z);
            double dvdx = f.deriv(0, x, y, z);
            double dvdy = f.deriv(1, x, y, z);
            double dvdz = f.deriv(2, x, y, z);

            assertEquals(a * x + b * y + c * z + e, v, 1e-4);
            assertEquals(a, dvdx, 1e-14);
            assertEquals(b, dvdy, 1e-14);
            assertEquals(c, dvdz, 1e-14);
        }
    }

    public void testTrigonometric3D() {
        double[][][] data = new double[n[0]][n[1]][n[2]];
        for (int i = 0; i < n[0]; ++i)
            for (int j = 0; j < n[1]; ++j)
                for (int k = 0; k < n[2]; ++k)
                    data[i][j][k] = a * Math.sin(xcoord[i]) + b
                            * Math.sin(ycoord[j]) + c * Math.sin(zcoord[k]) + e;

        Function f = createFunction("Trigonometric 3D test", data);

        for (int i = 0; i < repeat; ++i) {
            double x = Math.random();
            double y = Math.random();
            double z = Math.random();
            double v = f.get(x, y, z);
            double dvdx = f.deriv(0, x, y, z);
            double dvdy = f.deriv(1, x, y, z);
            double dvdz = f.deriv(2, x, y, z);

            assertEquals(a * Math.sin(x) + b * Math.sin(y) + c * Math.sin(z)
                    + e, v, 1e-4);
            assertEquals(a * Math.cos(x), dvdx, 1e-4);
            assertEquals(b * Math.cos(x), dvdy, 1e-4);
            assertEquals(c * Math.cos(x), dvdz, 1e-4);
        }
    }

    protected Function createFunction(String name, double[][][] data) {
        return new LookupTable(name, xcoord, ycoord, zcoord, data);
    }

}
