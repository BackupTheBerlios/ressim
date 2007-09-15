package no.uib.cipr.rs.meshgen.structured;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * A box in a structured mesh.
 */
public class Box {

    private int numElements;

    private int i1, i2, j1, j2, k1, k2;

    private int ni, nj, nk;

    /**
     * Creates a box with the given start and end indices for the given
     * topology.
     */
    public Box(int[] dim, CartesianTopology topology) {
        if (dim.length != 6)
            throw new IllegalArgumentException(
                    "6 integers are required to define box");

        i1 = dim[0];
        i2 = dim[1];
        j1 = dim[2];
        j2 = dim[3];
        k1 = dim[4];
        k2 = dim[5];

        IJK startIJK = new IJK(i1 - 1, j1 - 1, k1 - 1);

        if (!topology.isValidElement(startIJK))
            throw new IllegalArgumentException(
                    "Box begin-indices must be within domain");

        IJK endIJK = new IJK(i2 - 1, j2 - 1, k2 - 1);

        if (!topology.isValidElement(endIJK))
            throw new IllegalArgumentException(
                    "Box end-indices must be within domain");

        if (i1 > i2 || j1 > j2 || k1 > k2)
            throw new IllegalArgumentException(
                    "Box begin-indices must not be larger than end-indices");

        ni = i2 - i1 + 1;
        nj = j2 - j1 + 1;
        nk = k2 - k1 + 1;

        numElements = ni * nj * nk;
    }

    /**
     * Fills the given data array with the given value array in the locations
     * specified by this box. If the value array has only one element, that
     * value is used for the whole box. The given topology is used to determine
     * valid indices.
     */
    public double[] fill(double[] data, double[] val, CartesianTopology topology) {
        if (data.length != topology.getNumElements())
            throw new IllegalArgumentException("Illegal length of data array");

        if (val.length == 1) {
            double c = val[0];
            val = new double[numElements];
            Arrays.fill(val, c);
        } else if (val.length != numElements)
            throw new IllegalArgumentException(
                    "Illegal length of box values array");

        int l = 0;

        for (int k = k1 - 1; k < k2; k++) {
            for (int j = j1 - 1; j < j2; j++) {
                for (int i = i1 - 1; i < i2; i++) {
                    int m = topology.getLinearElement(new IJK(i, j, k));
                    data[m] = val[l++];
                }
            }
        }

        return data;
    }

    /**
     * Adds the linear indices specified in this box to the given set. The given
     * topology is used to determine valid indices.
     */
    public List<Integer> add(Set<Integer> set, CartesianTopology topology) {
        for (int k = k1 - 1; k < k2; k++)
            for (int j = j1 - 1; j < j2; j++)
                for (int i = i1 - 1; i < i2; i++)
                    set.add(topology.getLinearElement(new IJK(i, j, k)));

        return new ArrayList<Integer>(set);
    }

    /**
     * Removes the linear indices specified in this box from the given set. The
     * given topology is used to determine valid indices.
     */
    public List<Integer> remove(Set<Integer> set, CartesianTopology topology) {
        for (int k = k1 - 1; k < k2; k++)
            for (int j = j1 - 1; j < j2; j++)
                for (int i = i1 - 1; i < i2; i++)
                    set.remove(topology.getLinearElement(new IJK(i, j, k)));

        return new ArrayList<Integer>(set);
    }

    /**
     * Returns the subset of the given value array that lies within this box.
     * The given topology is used to determine valid indices.
     */
    public double[] get(double[] val, CartesianTopology topology) {
        if (val.length != topology.getNumElements())
            throw new IllegalArgumentException(
                    "Incorrect number of values in given array");

        double[] d = new double[numElements];

        int ind = 0;
        for (int k = k1 - 1; k < k2; k++)
            for (int j = j1 - 1; j < j2; j++)
                for (int i = i1 - 1; i < i2; i++)
                    d[ind++] = val[topology.getLinearElement(new IJK(i, j, k))];

        return d;
    }

    /**
     * Returns the number of elements in the i-direction within this box.
     */
    public int getNumI() {
        return ni;
    }

    /**
     * Returns the number of elements in the j-direction within this box.
     */
    public int getNumJ() {
        return nj;
    }

    /**
     * Returns the number of elements in the k-direction within this box.
     */
    public int getNumK() {
        return nk;
    }
}