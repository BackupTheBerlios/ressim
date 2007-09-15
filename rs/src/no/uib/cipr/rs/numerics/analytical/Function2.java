package no.uib.cipr.rs.numerics.analytical;

import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Function;

/**
 * Analytical potential function:
 * 
 * u(x,y,z) = x^2*y^3*z + 3x*sin(yz)
 * 
 * NB! Non-zero right-hand side. Use with extreme care!!
 * 
 * @author nmaeo
 */
public class Function2 implements Function {

    private static final long serialVersionUID = -4197173940710118726L;

    private final int dimension;

    private final String name;

    public Function2(@SuppressWarnings("unused")
    Configuration config, String name) {
        this(name, 3);
    }

    public Function2(String name, int dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    public Function2(Function2 analyticalPotential) {
        dimension = analyticalPotential.dimension;
        name = analyticalPotential.name;
    }

    public double deriv(int n, double... x) {
        switch (n) {
        case 0: // x-derivative
            return 2 * x[0] * x[1] * x[1] * x[1] * x[2] + 3 * x[0]
                    * Math.sin(x[1] * x[2]);
        case 1: // y-derivative
            return 3 * x[0] * x[0] * x[1] * x[1] * x[2] + 3 * x[0] * x[2]
                    * Math.cos(x[1] * x[2]);
        case 2: // z-derivative
            return x[0] * x[0] * x[1] * x[1] * x[1] + 3 * x[0] * x[1]
                    * Math.cos(x[1] * x[2]);

        default:
            throw new IllegalArgumentException("non-existent dimension");
        }
    }

    public double get(double... x) {
        if (x.length != dimension)
            throw new IllegalArgumentException("x.length != dimension");

        double u = x[0] * x[0] * x[1] * x[1] * x[1] * x[2] + 3 * x[0]
                * Math.sin(x[1] * x[2]);

        return u;
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
