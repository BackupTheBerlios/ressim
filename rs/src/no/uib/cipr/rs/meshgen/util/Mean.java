package no.uib.cipr.rs.meshgen.util;

/**
 * Compute the mean value of a number series.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Mean {
    // number of observations
    private int i = 0;

    // mean value of all observations up until now
    private double m = 0d;

    /**
     * Ready the mean object for accepting data later.
     */
    public Mean() {
        // void
    }

    /**
     * Run through a given data series upon construction.
     */
    public Mean(double[] x) {
        for (int i = 0; i < x.length; i++) {
            f(x[i]);
        }
    }

    /**
     * Process each observation of the series in turn. Each observation must be
     * processed only once. However, as the mean is a commutative operator, the
     * order in which the elements are passed is insignificant.
     * 
     * @param x
     *            Next observation in the series.
     * @return The same observation that was passed. This value is returned so
     *         that the operator can be used in a pipe.
     */
    public double f(double x) {
        i++;
        // m_{i+1} = m_{i} + ( x_{i+1} - m_{i} ) / {i+1}
        m += (x - m) / i;
        return x;
    }

    public double get() {
        return m;
    }
}
