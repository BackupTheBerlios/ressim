package no.uib.cipr.rs.meshgen.structured;

import java.util.Arrays;

public class IJK {

    private int[] ijk;

    /**
     * Constructs an ijk-triplet from the given indices
     */
    public IJK(int i, int j, int k) {
        ijk = new int[] { i, j, k };
    }

    /**
     * Returns the i-index.
     */
    public int i() {
        return ijk[0];
    }

    /**
     * Returns the j-index.
     */
    public int j() {
        return ijk[1];
    }

    /**
     * Returns the k-index.
     */
    public int k() {
        return ijk[2];
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", ijk[0], ijk[1], ijk[2]);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        IJK other = (IJK) obj;
        return Arrays.equals(ijk, other.ijk);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ijk);
    }

}
