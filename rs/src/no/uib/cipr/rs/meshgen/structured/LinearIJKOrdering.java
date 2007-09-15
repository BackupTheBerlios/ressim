package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.util.Configuration;

/**
 * Computes a linear index form a given (i,j,k)-index triplet.
 */
public abstract class LinearIJKOrdering implements Iterable<IJK> {

    /**
     * Returns true if the given indices are within bounds
     */
    public abstract boolean valid(int i, int j, int k);

    /**
     * Returns an iterable object of IJK index-triplets
     */
    public abstract Iterable<IJK> getIJK();

    /**
     * Returns the linear index for the given IJK index-triplet
     */
    public abstract Integer getLinear(IJK ijk);

    /**
     * Returns the linear index for the given indices.
     */
    public abstract int getLinear(int i, int j, int k)
            throws IllegalArgumentException;

    public static LinearIJKOrdering create(Configuration config, int numi,
            int numj, int numk) {
        return config.getObject("LinearIJKOrdering", LinearIJKOrdering.class,
                NaturalIJKOrdering.class, numi, numj, numk);
    }
}