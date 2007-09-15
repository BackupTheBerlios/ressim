package no.uib.cipr.rs.numerics.analytical;

import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Function;

/**
 * Analytical potential function:
 * 
 * u(x,y,z) = sin(sqrt(2)*pi*x) * sinh(pi*y) * sinh(pi*z)
 * 
 * @author nmaeo
 */
public class Function1 implements Function {

    private static final long serialVersionUID = -4197173940710118726L;

    private final int dimension = 3;

    public Function1(@SuppressWarnings("unused")
    Configuration config, String name) {
        // void
    }

    public double get(double... x) {
        if (x.length != dimension)
            throw new IllegalArgumentException("x.length != dimension");

        double u = Math.sin(Math.sqrt(2) * Math.PI * x[0])
                * Math.sinh(Math.PI * x[1]) * Math.sinh(Math.PI * x[2]);

        return u;
    }

    public double deriv(int n, double... x) {
        switch (n) {
        case 0: // x-derivative
            return Math.sqrt(2) * Math.PI
                    * Math.cos(Math.sqrt(2) * Math.PI * x[0])
                    * Math.sinh(Math.PI * x[1]) * Math.sinh(Math.PI * x[2]);
        case 1: // y-derivative
            return Math.PI * Math.sin(Math.sqrt(2) * Math.PI * x[0])
                    * Math.cosh(Math.PI * x[1]) * Math.sinh(Math.PI * x[2]);
        case 2: // z-derivative
            return Math.PI * Math.sin(Math.sqrt(2) * Math.PI * x[0])
                    * Math.sinh(Math.PI * x[1]) * Math.cosh(Math.PI * x[2]);
        default:
            throw new IllegalArgumentException("non-existent dimension");
        }
    }

    public boolean isDimension(int d) {
        return d == dimension;
    }

    public double maxInput(int d) {
        return Double.MAX_VALUE;
    }

    public double maxOutput() {
        return Double.MAX_VALUE;
    }

    public double minInput(int d) {
        return Double.MIN_VALUE;
    }

    public double minOutput() {
        return Double.MIN_VALUE;
    }

}
