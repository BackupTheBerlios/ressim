package no.uib.cipr.rs.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Solves and holds solution of cubic polynomial equations. The equation is
 * <code>z^3 + a2 z^2 + a1 z^1 + a0 = 0</code>, and the real roots are
 * stored.
 * <p>
 * Formulas taken from <a
 * href="http://mathworld.wolfram.com/CubicFormula.html">MathWorld</a>.
 */
public class CubicEquation {

    /**
     * Equation coefficients
     */
    private double a0, a1, a2;

    /**
     * The largest, middle, and smallest real roots
     */
    private double zMin, zMid, zMax;

    /**
     * Dummy constructor. Does not solve any cubic equation
     */
    public CubicEquation() {
        // void
    }

    /**
     * Computes real roots of the cubic equation
     * 
     * @param a0
     *            Zeroth order coefficient
     * @param a1
     *            First order coefficient
     * @param a2
     *            Second order coefficient
     */
    public CubicEquation(double a0, double a1, double a2) {
        setCoeff(a0, a1, a2);
    }

    /**
     * Sets new coefficients, and recomputes the real roots.
     * 
     * @param a0
     *            Zeroth order coefficient
     * @param a1
     *            First order coefficient
     * @param a2
     *            Second order coefficient
     */
    public void setCoeff(double a0, double a1, double a2) {
        this.a0 = a0;
        this.a1 = a1;
        this.a2 = a2;

        solve();
    }

    private void solve() {
        double Q = (3 * a1 - a2 * a2) / 9.;
        double R = (9 * a1 * a2 - 27 * a0 - 2 * a2 * a2 * a2) / 54.;

        double D = Q * Q * Q + R * R;

        // One real root
        if (D > 0 || Q == 0) {
            double Sarg = R + Math.sqrt(D);
            double Targ = R - Math.sqrt(D);
            double S = Math.signum(Sarg) * Math.pow(Math.abs(Sarg), 1. / 3.);
            double T = Math.signum(Targ) * Math.pow(Math.abs(Targ), 1. / 3.);

            zMin = zMid = zMax = -a2 / 3. + S + T;
        }

        // Three real roots
        else {
            double theta = Math.acos(R / Math.sqrt(-Q * Q * Q));

            double factor = 2 * Math.sqrt(-Q);

            double z1 = factor * Math.cos(theta / 3.) - a2 / 3;
            double z2 = factor * Math.cos((theta + 2 * Math.PI) / 3.) - a2 / 3;
            double z3 = factor * Math.cos((theta + 4 * Math.PI) / 3.) - a2 / 3;

            // Sort the roots
            List<Double> list = Arrays.asList(z1, z2, z3);
            Collections.sort(list);
            zMin = list.get(0);
            zMid = list.get(1);
            zMax = list.get(2);
        }
    }

    /**
     * Gets the smallest real root
     */
    public double getSmallestRoot() {
        return zMin;
    }

    /**
     * Gets the middle root
     */
    public double getMiddleRoot() {
        return zMid;
    }

    /**
     * Gets the largest real root
     */
    public double getLargestRoot() {
        return zMax;
    }

}
