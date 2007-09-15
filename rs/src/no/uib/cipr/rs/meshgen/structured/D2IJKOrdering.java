package no.uib.cipr.rs.meshgen.structured;

import static no.uib.cipr.rs.meshgen.structured.Direction.I;
import static no.uib.cipr.rs.meshgen.structured.Direction.J;
import static no.uib.cipr.rs.meshgen.structured.Direction.K;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import no.uib.cipr.rs.util.Configuration;

public class D2IJKOrdering extends LinearIJKOrdering {

    /**
     * Stores the direction index and number of elements of the dimension.
     */
    private static class Dimension implements Comparable<Dimension> {

        private Direction direction;

        private int size;

        public Dimension(Direction direction, int size) {
            this.direction = direction;
            this.size = size;
        }

        public int size() {
            return size;
        }

        public Direction getDirection() {
            return direction;
        }

        public int compareTo(Dimension o) {
            if (o.size < this.size)
                return -1;
            else if (o.size > this.size)
                return 1;
            else
                return 0;
        }

    }

    /**
     * Sorts dimension indices and sizes according to dimensions of decreasing
     * size.
     */
    private static class SortedDimensions {

        private List<Dimension> dimensions;

        public SortedDimensions(int numi, int numj, int numk) {
            dimensions = new ArrayList<Dimension>();

            dimensions.add(new Dimension(I, numi));
            dimensions.add(new Dimension(J, numj));
            dimensions.add(new Dimension(K, numk));

            Collections.sort(dimensions);
        }

        public int numi() {
            return dimensions.get(I.dir()).size();
        }

        public int numj() {
            return dimensions.get(J.dir()).size();
        }

        public int numk() {
            return dimensions.get(K.dir()).size();
        }

        public IJK getIJK(int i, int j, int k) {
            int[] orig = new int[] { i, j, k };

            return new IJK(orig[dimensions.get(I.dir()).getDirection().dir()],
                    orig[dimensions.get(J.dir()).getDirection().dir()],
                    orig[dimensions.get(K.dir()).getDirection().dir()]);
        }

    }

    private static final long serialVersionUID = 7989502995651055122L;

    private int numi, numj, numk;

    private List<IJK> ijkIndices;

    private SortedDimensions sortedDims;

    public D2IJKOrdering(@SuppressWarnings("unused")
    Configuration config, int numi, int numj, int numk) {
        this(numi, numj, numk);
    }

    /**
     * Creates an ijk-to-linear-index computer based on a D2-ordering.
     * 
     * @param numi
     *            number of elements in i-direction
     * @param numj
     *            number of elements in j-direction
     * @param numk
     *            number of elements in k-direction
     */
    public D2IJKOrdering(int numi, int numj, int numk) {
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

        sortedDims = new SortedDimensions(numi, numj, numk);

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
        // apply sorting
        IJK ijk = sortedDims.getIJK(i, j, k);

        int ni = sortedDims.numi();
        int nj = sortedDims.numj();
        int nk = sortedDims.numk();

        // check dimensionionality
        if (nj == 0 && nk == 0)
            return getD2Linear3D(ijk.i(), ijk.j(), ijk.k(), ni, 1, 1);
        else if (nj > 0 && nk == 0)
            return getD2Linear3D(ijk.i(), ijk.j(), ijk.k(), ni, nj, 1);
        else
            return getD2Linear3D(ijk.i(), ijk.j(), ijk.k(), ni, nj, nk);

    }

    private int getD2Linear3D(int i, int j, int k, int ni, int nj, int nk) {
        // 3D diagonal index
        int d3 = i + j + k + 1;

        // 2D diagonal index
        int d2 = i + j + 1;

        // previous tetrahedron elements
        int ntet = getNum3D(d3 - 1, ni, nj, nk);

        // previous triangle elements
        int ntri = getNum2D(d2 - 1, ni, nj)
                - getNum2D(Math.max(d3 - nk, 0), ni, nj);

        // correction
        int c = Math.max(d2 - nj, 0);

        // linear index
        return ntet + ntri + i - c;
    }

    private int getNum3D(int d, int ni, int nj, int nk) {
        int l0 = d;
        int n0 = getNumTetElements(l0);

        int lx = Math.max(d - ni, 0);
        int nx = getNumTetElements(lx);

        int ly = Math.max(d - nj, 0);
        int ny = getNumTetElements(ly);

        int lz = Math.max(d - nk, 0);
        int nz = getNumTetElements(lz);

        int lxy = Math.max(d - ni - nj, 0);
        int nxy = getNumTetElements(lxy);

        int lxz = Math.max(d - ni - nk, 0);
        int nxz = getNumTetElements(lxz);

        int lyz = Math.max(d - nj - nk, 0);
        int nyz = getNumTetElements(lyz);

        int lxyz = Math.max(d - ni - nj - nk, 0);
        int nxyz = getNumTetElements(lxyz);

        return n0 - nx - (ny - nxy) - (nz - nxz - (nyz - nxyz));
    }

    /**
     * Returns the number of elements within the right-angled tetrahedron of
     * dimension d.
     * 
     * @param d
     *            Dimension
     */
    private int getNumTetElements(int d) {
        return (int) Math.ceil(d / 3.0 + d * d / 2.0 + d * d * d / 6.0);
    }

    private int getNum2D(int d, int ni, int nj) {

        int l0 = d;
        int n0 = getNumTriElements(l0);

        int lx = Math.max(d - ni, 0);
        int nx = getNumTriElements(lx);

        int ly = Math.max(d - nj, 0);
        int ny = getNumTriElements(ly);

        int lxy = Math.max(d - ni - nj, 0);
        int nxy = getNumTriElements(lxy);

        return n0 - nx - (ny - nxy);
    }

    private int getNumTriElements(int d) {
        return (int) (0.5 * d * (d + 1));
    }

    public Iterator<IJK> iterator() {
        return getIJK().iterator();
    }

}
