package no.uib.cipr.rs.numerics.analytical;

import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Function;

/**
 * Linear potential in the x-direction
 * 
 * @author nmaeo
 */
public class LinearX implements Function {

    private static final long serialVersionUID = -4197173940710118726L;

    private final int dimension;

    private final String name;

    public LinearX(@SuppressWarnings("unused")
    Configuration config, String name) {
        this(name, 3);
    }

    public LinearX(String name, int dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    public LinearX(LinearX analyticalPotential) {
        dimension = analyticalPotential.dimension;
        name = analyticalPotential.name;
    }

    public double get(double... x) {
        if (x.length != dimension)
            throw new IllegalArgumentException("x.length != dimension");
    
        // TODO currently hardcoded function to test implementation
        double u = x[0];
    
        return u;
    }

    public double deriv(int n, double... x) {
        switch (n) {
        case 0: // x-derivative
            return 1;
        case 1: // y-derivative
            return 0;
        case 2: // z-derivative
            return 0;
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
