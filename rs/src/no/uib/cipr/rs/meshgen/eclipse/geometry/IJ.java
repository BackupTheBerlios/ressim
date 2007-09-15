package no.uib.cipr.rs.meshgen.eclipse.geometry;

import java.util.Locale;

/**
 * This class represents an ordered pair (2-tuplet) of indices (i,j). The
 * ordered pair is sorted.
 */
public class IJ {

    private int j;

    private int i;

    /**
     * Creates an ordered pair of the input indices i and j.
     */
    public IJ(int i, int j) {
        this.i = i < j ? i : j;
        this.j = j >= i ? j : i;
    }

    /**
     * Returns the first index in the ordered pair.
     */
    public int getI() {
        return i;
    }

    /** Returns the second index in the ordered pair. */
    public int getJ() {
        return j;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "(%d, %d)", i, j);
    }

}
