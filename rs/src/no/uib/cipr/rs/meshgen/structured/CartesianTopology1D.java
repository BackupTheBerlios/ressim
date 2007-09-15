package no.uib.cipr.rs.meshgen.structured;

import static no.uib.cipr.rs.meshgen.structured.Orientation.LEFT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.RIGHT;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.util.Configuration;

/**
 * A 1D structured topology implementation.
 */
public class CartesianTopology1D extends CartesianTopology {

    private static final long serialVersionUID = 6527046691038640011L;

    private static final int numElementInterfaces = 2;

    private static final int numElementPoints = 2;

    private static final int numInterfacePoints = 1;

    public CartesianTopology1D(Configuration config, int numi) {
        super(config, numi, 0, 0);
    }

    /**
     * Creates a topology with natural element ordering.
     * 
     * @param ni
     */
    public CartesianTopology1D(int ni) {
        super(ni, 0, 0);
    }

    @Override
    public int[] getPointElements(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        List<Integer> l = new ArrayList<Integer>();

        if (linearElementIJK.valid(i, j, k))
            l.add(getLinearElement(i, j, k));

        if (linearElementIJK.valid(i - 1, j, k))
            l.add(getLinearElement(i - 1, j, k));

        int[] ll = new int[l.size()];
        for (int n = 0, size = l.size(); n < size; n++)
            ll[n] = l.get(n);

        return ll;
    }

    @Override
    int getLinearInterface(int i, int j, int k, Orientation orient) {
        int l = 0;

        switch (orient) {
        case LEFT:
            l = 0;
            break;
        case RIGHT:
            l = 1;
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
        case RIGHT:
            return getLinearInterface(i + 1, j, k, LEFT);
        default:
            throw new IllegalArgumentException("Illegal forward orientation");
        }
    }

    @Override
    public int[] getPointInterfaces(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        List<Integer> l = new ArrayList<Integer>();

        // note: i, j, k point indices are now used as element indices!
        if (linearElementIJK.valid(i, j, k))
            l.add(getLinearInterface(i, j, k, LEFT));

        if (linearElementIJK.valid(i - 1, j, k))
            l.add(getLinearInterface(i - 1, j, k, RIGHT));

        int[] ll = new int[l.size()];
        for (int n = 0, size = l.size(); n < size; n++)
            ll[n] = l.get(n);

        return ll;
    }

    /**
     * Returns an array of global corner point indices for the input element
     * 
     * @param ijk
     *            element ijk index-triplet
     * 
     * @return An int array of 2 global corner point indices for input element
     * 
     */
    @Override
    public int[] getElementPoints(IJK ijk) {
        int[] cp = new int[numElementPoints];

        cp[0] = getLinearPoint(ijk);
        cp[1] = cp[0] + 1;

        return cp;
    }

    /**
     * Creates an array of 2 global interface indices
     * 
     * @param ijk
     * 
     * @return An int array containing the global interface indices of the
     *         current element
     */
    @Override
    public int[] getElementInterfaces(IJK ijk) {
        int[] l = new int[numElementInterfaces];

        int index = getLinearElement(ijk) * numElementInterfaces;

        for (int a = 0; a < numElementInterfaces; a++) {
            l[a] = index;
            index++;
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
        case LEFT:
            return new int[] { cp[0] };
        case RIGHT:
            return new int[] { cp[1] };
        default:
            throw new IllegalArgumentException("Unknown orientation");
        }
    }

    @Override
    boolean hasElementNeighbour(int i, int j, int k, Orientation orientation) {
        switch (orientation) {
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
        return numi + 1;
    }

    @Override
    public int getNumInterfaces() {
        return numi * numElementInterfaces;
    }

    @Override
    public int getNumElements() {
        return numi;
    }

    @Override
    public int getNumConnections() {
        return numi - 1;
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
    int getLinearConnection(int i, int j, int k, Orientation orient) {
        switch (orient) {
        case LEFT:
            // not valid for i=0
            return i - 1;
        case RIGHT:
            // not valid for i=numi-1
            return i;
        default:
            throw new IllegalArgumentException("Unknown orientation");
        }
    }

    @Override
    public int getNumInterfacePoints() {
        return numInterfacePoints;
    }

    @Override
    public boolean pointOnBoundary(IJK ijk) {
        int i = ijk.i();

        return (i == 0 || i == numi);
    }

    @Override
    public int getNumPointElements(IJK ijk) {
        int i = ijk.i();

        int ni = 2;
        if (i == 0 || i == numi)
            ni = 1;
        return ni;
    }

    @Override
    public int getNumPointInterfaces(IJK ijk) {
        int i = ijk.i();

        int ni = 2;
        if (i == 0 || i == numi)
            ni = 1;
        return ni;
    }

    @Override
    public int getDimension() {
        return 1;
    }

}
