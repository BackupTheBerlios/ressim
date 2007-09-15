package no.uib.cipr.rs.meshgen.util;

/**
 * Compute the standard deviation of a number series.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class StDev {
    // number of observations
    private int i = 0;

    // mean value of all observations from 1..i
    private double m = 0d;

    // sum of squared differences between the observations 1..i and
    // their mean value
    private double s = 0d;

    /**
     * Ready the standard deviation object for accepting data later.
     */
    public StDev() {
        // void
    }

    /**
     * Run through a given data series upon construction.
     */
    public StDev(double[] x) {
        for (int i = 0; i < x.length; i++) {
            f(x[i]);
        }
    }

    /**
     * Process each observation of the series in turn. Each observation must be
     * processed only once. However, as the standard deviation is a commutative
     * operator, the order in which the elements are passed in insignificant.
     * 
     * @param x
     *            Next observation in the series.
     * @return The same observation that was passed. This value is returned so
     *         that the operator can be used in a pipe.
     */
    public double f(double x) {
        // see Alan Miller. Journal of Computational Physics, vol. 85(2),
        // Dec. 1989 pp. 500-501.
        i++;
        double d = x - m; // d_{i+1} = x_{i+1} - m_i
        m += d / i; // m_{i+1} = m_{i} + d_{i+1} / {i+1}
        s += d * (x - m); // s_{i+1} = s_{i} + d_{i+1} * ( x_{i+1} - m_{i+1} )
        return x;
    }

    public double get() {
        return Math.sqrt(s / i);
    }
}
