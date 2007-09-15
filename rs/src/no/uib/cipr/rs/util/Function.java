package no.uib.cipr.rs.util;

import java.io.Serializable;

/**
 * A named function which can be evaluated and differentiated
 */
public interface Function extends Serializable {

    /**
     * Checks if the function is of the given dimension (the number of input
     * arguments it accepts)
     */
    boolean isDimension(int d);

    /**
     * Evaluates the function for the given values
     * 
     * @param x
     *            Coordinates to look up
     * @return The function value
     */
    double get(double... x);

    /**
     * Computes the derivative of the function
     * 
     * @param n
     *            Coordinate to differentiate with respect to
     * @param x
     *            Coordinates to look up
     * @return The derivative
     */
    double deriv(int n, double... x);

    /**
     * Largest output value of the function
     */
    double maxOutput();

    /**
     * Smallest output value of the function
     */
    double minOutput();

    /**
     * Largest input value along the given dimension
     */
    double maxInput(int d);

    /**
     * Smallest input value along the given dimension
     */
    double minInput(int d);
}
