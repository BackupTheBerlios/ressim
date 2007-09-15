package no.uib.cipr.rs.meshgen.util;

/**
 * Compute the minimum value of a data series.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Maximum {
    // smallest value seen so far; initially a value smaller than anything
    // else we can observe from double values.
    private double m = Double.NEGATIVE_INFINITY;
    
    public double f(double x) {
        // use a math function for this as most JVMs can inline it into a
        // native processor instruction!
        m = Math.max(m, x);
        return x;
    }
    
    // observe the minimum value from the entire data series up till now
    public double get() {
        return m;
    }
}
