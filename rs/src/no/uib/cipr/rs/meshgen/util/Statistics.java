package no.uib.cipr.rs.meshgen.util;

/**
 * Collect statistical information about a data series.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Statistics {
    // statistical measures for the data series
    private Mean mean = new Mean();

    private StDev stdev = new StDev();

    private Minimum minimum = new Minimum();

    private Maximum maximum = new Maximum();

    // gather statistics from the data series as it passes by
    public double f(double x) {
        // collect both the mean and the statistics, for each data value
        return minimum.f(maximum.f(mean.f(stdev.f(x))));
    }

    @Override
    public String toString() {
        // return a formatted synopsis of the statistical properties
        // of the data series so far
        return String.format("mean=%f, st.dev.=%f, min.=%f, max.=%f", mean
                .get(), stdev.get(), minimum.get(), maximum.get());
    }
}
