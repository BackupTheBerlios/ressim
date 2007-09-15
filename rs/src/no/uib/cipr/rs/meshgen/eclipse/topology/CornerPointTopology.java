package no.uib.cipr.rs.meshgen.eclipse.topology;

import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.structured.LinearIJKOrdering;
import no.uib.cipr.rs.meshgen.structured.NaturalIJKOrdering;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * A 3D corner point mesh topology
 */
public class CornerPointTopology {

    // number of cells in i-direction
    private int ni;

    // number of cells in j-direction
    private int nj;

    // number of cells in k-direction
    private int nk;

    private LinearIJKOrdering linearElementIJK;

    private LinearIJKOrdering linearPointIJK;

    /**
     * Each cell has 6 interfaces
     */
    private static final int numElementInterfaces = 6;

    /**
     * Each cell has 8 vertices
     */
    private static final int numElementPoints = 8;

    /**
     * Each interface has 4 vertices
     */
    private static final int numInterfacePoints = 4;

    /**
     * Creates the corner point topology.
     */
    public CornerPointTopology(int ni, int nj, int nk) {
        this.ni = ni;
        this.nj = nj;
        this.nk = nk;

        linearElementIJK = new NaturalIJKOrdering(ni, nj, nk);

        linearPointIJK = new NaturalIJKOrdering(2 * ni, 2 * nj, 2 * nk);
    }

    /**
     * Returns the linear pillar index for the given corner point. This is used
     * to get the desired pillar from COORD.
     */
    public int getPointPillar(IJK ijk) {
        // collapse point indices to pillar indices. Relies on integer division.
        int i = ijk.i() / 2 + ijk.i() % 2;
        int j = ijk.j() / 2 + ijk.j() % 2;

        return i + j * (ni + 1);
    }

    /**
     * Returns the number of interfaces, 6 per cell;
     */
    public int getNumInterfaces() {
        return numElementInterfaces * getNumElements();
    }

    /**
     * Returns the number of corner points.
     */
    public int getNumPoints() {
        return 2 * ni * 2 * nj * 2 * nk;
    }

    /**
     * Returns the linear index for the given corner point ijk-index triplet.
     * The depth of this corner point is the value in ZCORN for the same linear
     * index.
     */
    public int getLinearPoint(IJK ijk) {
        return linearPointIJK.getLinear(ijk);
    }

    /**
     * Returns an iterable object of corner point ijk-index triplets.
     */
    public Iterable<IJK> getPointsIJK() {
        return linearPointIJK.getIJK();
    }

    /**
     * Returns the number of cells
     */
    public int getNumElements() {
        return ni * nj * nk;
    }

    /**
     * Returns the linear element index
     */
    public int getLinearElement(IJK ijk) {
        return linearElementIJK.getLinear(ijk);
    }

    public int getLinearElement(int i, int j, int k) {
        return linearElementIJK.getLinear(i, j, k);
    }

    /**
     * Returns an ordered array of linear point indices that build up this
     * element.
     */
    public int[] getElementPoints(IJK ijk) {

        // compute first corner point (i,j,k) index of the given element
        int i = 2 * ijk.i(), j = 2 * ijk.j(), k = 2 * ijk.k();

        int[] cp = new int[numElementPoints];

        cp[0] = linearPointIJK.getLinear(i, j, k);
        cp[1] = linearPointIJK.getLinear(i + 1, j, k);
        cp[2] = linearPointIJK.getLinear(i + 1, j + 1, k);
        cp[3] = linearPointIJK.getLinear(i, j + 1, k);
        cp[4] = linearPointIJK.getLinear(i, j, k + 1);
        cp[5] = linearPointIJK.getLinear(i + 1, j, k + 1);
        cp[6] = linearPointIJK.getLinear(i + 1, j + 1, k + 1);
        cp[7] = linearPointIJK.getLinear(i, j + 1, k + 1);

        return cp;
    }

    /**
     * Returns an iterable object of element ijk-index triplets
     */
    public Iterable<IJK> getElementsIJK() {
        return linearElementIJK.getIJK();
    }

    /**
     * Returns the linear interface index given element ijk-index triplet and
     * interface orientation type.
     */
    public int getLinearInterface(IJK ijk, Orientation orientation) {
        int l = orientation.getLocalIndex();

        if (l < 0 || l > numElementInterfaces - 1)
            throw new IndexOutOfBoundsException("Invalid local interface index");

        int index = getLinearElement(ijk) * numElementInterfaces + l;

        return index;
    }

    public int getNumInterfacePoints() {
        return numInterfacePoints;
    }

    public int getNumElementInterfaces() {
        return numElementInterfaces;
    }

    /**
     * Returns an ordered array of linear point indices for the given interface
     */
    public int[] getInterfacePoints(IJK ijk, Orientation orientation) {
        int[] cp = getElementPoints(ijk);

        switch (orientation) {
        case TOP:
            return new int[] { cp[0], cp[1], cp[2], cp[3] };
        case BOTTOM:
            return new int[] { cp[7], cp[6], cp[5], cp[4] };
        case FRONT:
            return new int[] { cp[4], cp[5], cp[1], cp[0] };
        case BACK:
            return new int[] { cp[6], cp[7], cp[3], cp[2] };
        case LEFT:
            return new int[] { cp[7], cp[4], cp[0], cp[3] };
        case RIGHT:
            return new int[] { cp[5], cp[6], cp[2], cp[1] };
        default:
            throw new IllegalArgumentException("Unknown orientation");
        }
    }

    /**
     * Returns an ordered array of linear interface indices for the given
     * element
     */
    public int[] getElementInterfaces(IJK ijk) {

        int[] l = new int[numElementInterfaces];

        int csi = getLinearElement(ijk) * numElementInterfaces;

        for (int a = 0; a < numElementInterfaces; a++) {
            l[a] = csi;
            csi++;
        }

        return l;

    }

    public int getNumElementsI() {
        return ni;
    }

    public int getNumElementsJ() {
        return nj;
    }

    public int getNumElementsK() {
        return nk;
    }
}
