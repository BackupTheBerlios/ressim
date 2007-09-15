package no.uib.cipr.rs.util;

/**
 * Collection of tolerance constants
 */
public final class Tolerances {

    /**
     * This class cannot be instantiated
     */
    private Tolerances() {
        // void
    }

    /**
     * Small tolerance
     */
    public final static double smallEps = 1.0e-9;

    /**
     * Large tolerance. This is the smallest amount with which we can multiply
     * and guarantee that we get a different value from the original.
     */
    public final static double largeEps = 1.0e-6;
}
