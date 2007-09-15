package no.uib.cipr.rs.meshgen.structured;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import no.uib.cipr.rs.util.Configuration;

/**
 * Computes a linear index from given i, j, k indices following: linear = i + j *
 * numi + k * numi * numj.
 * 
 * Looping over the indices is possible by means of an Iterable object of IJK
 * index-triplets. Here the k-index is varying most quickly.
 * 
 */
public class NaturalIJKOrdering extends LinearIJKOrdering {

    private int numi, numj, numk;

    private List<IJK> ijkIndices;

    public NaturalIJKOrdering(@SuppressWarnings("unused")
    Configuration config, int numi, int numj, int numk) {
        this(numi, numj, numk);
    }

    /**
     * Creates an ijk-to-linear-index computer based on a natural ordering.
     * 
     * @param numi
     *            number of elements in i-direction
     * @param numj
     *            number of elements in j-direction
     * @param numk
     *            number of elements in k-direction
     */
    public NaturalIJKOrdering(int numi, int numj, int numk) {
        if (numi <= 0)
            throw new IllegalArgumentException("Too few i-direction elements");
        if (numj < 0)
            throw new IllegalArgumentException("Too few j-direction elements");
        if (numk < 0)
            throw new IllegalArgumentException("Too few k-direction elements");

        if (numj == 0 && numk > 0)
            throw new IllegalArgumentException("Too few j-direction elements");

        this.numi = numi;
        this.numj = numj;
        this.numk = numk;

        ijkIndices = new ArrayList<IJK>();

        // generate indices, allow for reduced dimension
        if (numj == 0 && numk == 0) // 1D
            for (int i = 0; i < numi; i++)
                ijkIndices.add(new IJK(i, 0, 0));

        else if (numj != 0 && numk == 0) // 2D
            for (int i = 0; i < numi; i++)
                for (int j = 0; j < numj; j++)
                    ijkIndices.add(new IJK(i, j, 0));
        else
            // 3D
            for (int i = 0; i < numi; i++)
                for (int j = 0; j < numj; j++)
                    for (int k = 0; k < numk; k++)
                        ijkIndices.add(new IJK(i, j, k));
    }

    @Override
    public boolean valid(int i, int j, int k) {
        boolean inRange = true;

        if (i < 0 || i > (numi - 1))
            inRange = false;

        if (j < 0 || (j > (numj - 1) && j != 0))
            inRange = false;

        if (k < 0 || (k > numk - 1 && k != 0))
            inRange = false;
        return inRange;
    }

    @Override
    public Iterable<IJK> getIJK() {
        return Collections.unmodifiableList(ijkIndices);
    }

    @Override
    public Integer getLinear(IJK ijk) {
        return getLinear(ijk.i(), ijk.j(), ijk.k());
    }

    @Override
    public int getLinear(int i, int j, int k) throws IllegalArgumentException {
        if (!valid(i, j, k))
            throw new IllegalArgumentException("(i, j, k) not in valid range");

        return (i + j * numi + k * numi * numj);
    }

    public Iterator<IJK> iterator() {
        return getIJK().iterator();
    }

}
