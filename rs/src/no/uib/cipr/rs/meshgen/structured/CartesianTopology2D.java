package no.uib.cipr.rs.meshgen.structured;

import static no.uib.cipr.rs.meshgen.structured.Orientation.BACK;
import static no.uib.cipr.rs.meshgen.structured.Orientation.FRONT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.LEFT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.RIGHT;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.util.Configuration;

/**
 * A 2D structured topology implementation.
 */
public class CartesianTopology2D extends CartesianTopology {

    private static final long serialVersionUID = 319367046469261343L;

    private static final int numElementInterfaces = 4;

    private static final int numElementPoints = 4;

    private static final int numInterfacePoints = 2;

    public CartesianTopology2D(Configuration config, int numi, int numj) {
        super(config, numi, numj, 0);
    }

    /**
     * Creates a naturally ordered 2D mesh topology.
     */
    public CartesianTopology2D(int numi, int numj) {
        super(numi, numj, 0);
    }

    @Override
    public int[] getPointElements(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        List<Integer> l = new ArrayList<Integer>();

        if (linearElementIJK.valid(i, j, k))
            l.add(getLinearElement(i, j, k));

        if (linearElementIJK.valid(i, j - 1, k))
            l.add(getLinearElement(i, j - 1, k));

        if (linearElementIJK.valid(i - 1, j, k))
            l.add(getLinearElement(i - 1, j, k));

        if (linearElementIJK.valid(i - 1, j - 1, k))
            l.add(getLinearElement(i - 1, j - 1, k));

        int[] ll = new int[l.size()];
        for (int n = 0, size = l.size(); n < size; n++)
            ll[n] = l.get(n);

        return ll;
    }

    @Override
    int getLinearInterface(int i, int j, int k, Orientation orient) {
        int l = 0;

        switch (orient) {
        case FRONT:
            l = 0;
            break;
        case BACK:
            l = 1;
            break;
        case LEFT:
            l = 2;
            break;
        case RIGHT:
            l = 3;
            break;
        default:
            throw new IllegalArgumentException("Unknown orientation");
        }

        if (l < 0 || l > numElementInterfaces - 1)
            throw new IndexOutOfBoundsException("Invalid local interface index");

        return (getLinearElement(i, j, k) * numElementInterfaces + l);

    }

    @Override
    int getLinearInterfaceNeighbour(int i, int j, int k, Orientation orient) {
        switch (orient) {
        case BACK:
            return getLinearInterface(i, j + 1, k, FRONT);
        case RIGHT:
            return getLinearInterface(i + 1, j, k, LEFT);
        default:
            throw new IllegalArgumentException("Illegal forward orientation");
        }
    }

    @Override
    int getLinearConnection(int i, int j, int k, Orientation orient) {
        switch (orient) {
        case FRONT:
            // not valid for j = 0
            return i + (j - 1) * numi;
        case BACK:
            // not valid for j = numj-1
            return i + j * numi;
        case LEFT:
            // not valid for i=0
            return (i - 1) + j * (numi - 1) + numi * (numj - 1);
        case RIGHT:
            // not valid for i=numi-1
            return i + j * (numi - 1) + numi * (numj - 1);
        default:
            throw new IllegalArgumentException("Unknown orientation");
        }

    }

    /**
     * Returns an Integer array of global indices of all valid interfaces that
     * shares corner point ijk.
     */
    @Override
    public int[] getPointInterfaces(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        List<Integer> l = new ArrayList<Integer>();
        // note: i, j, k point indices are now used as element indices!
        if (linearElementIJK.valid(i, j, k)) {
            l.add(getLinearInterface(i, j, k, FRONT));
            l.add(getLinearInterface(i, j, k, LEFT));
        }

        if (linearElementIJK.valid(i, j - 1, k)) {
            l.add(getLinearInterface(i, j - 1, k, BACK));
            l.add(getLinearInterface(i, j - 1, k, LEFT));
        }

        if (linearElementIJK.valid(i - 1, j, k)) {
            l.add(getLinearInterface(i - 1, j, k, FRONT));
            l.add(getLinearInterface(i - 1, j, k, RIGHT));
        }

        if (linearElementIJK.valid(i - 1, j - 1, k)) {
            l.add(getLinearInterface(i - 1, j - 1, k, BACK));
            l.add(getLinearInterface(i - 1, j - 1, k, RIGHT));
        }

        int[] ll = new int[l.size()];
        for (int n = 0, size = l.size(); n < size; n++)
            ll[n] = l.get(n);

        return ll;
    }

    /**
     * Returns an array of global point indices for the current element. These
     * are given in the following order: (i,j,k), (i+1,j,k), (i+1,j+1,k),
     * (i,j+1,k)
     * 
     * @param ijk
     *            element ijk index-triplet
     * 
     * @return An int array of 4 global point indices for the input element
     * 
     */
    @Override
    public int[] getElementPoints(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        int[] cp = new int[numElementPoints];

        cp[0] = getLinearPoint(i, j, k);
        cp[1] = cp[0] + 1;
        cp[2] = getLinearPoint(i, j + 1, k) + 1;
        cp[3] = cp[2] - 1;

        return cp;
    }

    /**
     * Creates an array of 4 global interface indices
     * 
     * @param ijk
     * 
     * @return An int array containing the global interface indices of the
     *         current element
     */
    @Override
    public int[] getElementInterfaces(IJK ijk) {
        int[] l = new int[numElementInterfaces];

        int csi = getLinearElement(ijk) * numElementInterfaces;

        for (int a = 0; a < numElementInterfaces; a++) {
            l[a] = csi;
            csi++;
        }

        return l;
    }

    /**
     * @param ijk
     * @param orient
     * @return linear index of interface's vertices
     */
    @Override
    public int[] getInterfacePoints(IJK ijk, Orientation orient) {
        int cp[] = getElementPoints(ijk);

        switch (orient) {
        case FRONT:
            return new int[] { cp[1], cp[0] };
        case BACK:
            return new int[] { cp[3], cp[2] };
        case LEFT:
            return new int[] { cp[0], cp[3] };
        case RIGHT:
            return new int[] { cp[2], cp[1] };
        default:
            throw new IllegalArgumentException("Unknown orientation");
        }
    }

    @Override
    boolean hasElementNeighbour(int i, int j, int k, Orientation orientation) {
        switch (orientation) {
        case FRONT:
            return linearElementIJK.valid(i, j - 1, k);
        case BACK:
            return linearElementIJK.valid(i, j + 1, k);
        case LEFT:
            return linearElementIJK.valid(i - 1, j, k);
        case RIGHT:
            return linearElementIJK.valid(i + 1, j, k);
        default:
            throw new IllegalArgumentException("Unknown orientation");
        }
    }

    @Override
    IJK getElementNeighbour(int i, int j, int k, Orientation orientation) {
        if (!hasElementNeighbour(i, j, k, orientation))
            throw new IllegalArgumentException("No neighbour in given diretion");

        switch (orientation) {
        case FRONT:
            return new IJK(i, j - 1, k);
        case BACK:
            return new IJK(i, j + 1, k);
        case LEFT:
            return new IJK(i - 1, j, k);
        case RIGHT:
            return new IJK(i + 1, j, k);

        default:
            throw new IllegalArgumentException("Invalid orientation");
        }
    }

    @Override
    public int getNumPoints() {
        return (numi + 1) * (numj + 1);
    }

    @Override
    public int getNumInterfaces() {
        return numi * numj * numElementInterfaces;
    }

    @Override
    public int getNumElements() {
        return numi * numj;
    }

    @Override
    public int getNumConnections() {
        return 2 * numi * numj - numi - numj;
    }

    @Override
    public int getNumElementInterfaces() {
        return numElementInterfaces;
    }

    @Override
    public int getNumElementPoints() {
        return numElementPoints;
    }

    @Override
    public int getNumInterfacePoints() {
        return numInterfacePoints;
    }

    @Override
    public boolean pointOnBoundary(IJK ijk) {
        int i = ijk.i(), j = ijk.j();

        return (i == 0 || i == numi) || (j == 0 || j == numj);
    }

    @Override
    public int getNumPointElements(IJK ijk) {
        int i = ijk.i(), j = ijk.j();

        int ni = 2, nj = 2;
        if (i == 0 || i == numi)
            ni = 1;
        if (j == 0 || j == numj)
            nj = 1;
        return ni * nj;
    }

    @Override
    public int getNumPointInterfaces(IJK ijk) {
        int i = ijk.i(), j = ijk.j();

        int ni = 2, nj = 2;
        if (i == 0 || i == numi)
            ni = 1;
        if (j == 0 || j == numj)
            nj = 2;
        return 2 * ni * nj;
    }

    @Override
    public int getDimension() {
        return 2;
    }
}
