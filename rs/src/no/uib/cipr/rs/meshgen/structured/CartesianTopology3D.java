package no.uib.cipr.rs.meshgen.structured;

import static no.uib.cipr.rs.meshgen.structured.Orientation.BACK;
import static no.uib.cipr.rs.meshgen.structured.Orientation.BOTTOM;
import static no.uib.cipr.rs.meshgen.structured.Orientation.FRONT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.LEFT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.RIGHT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.TOP;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.util.Configuration;

/**
 * A 3D structured topology implementation.
 */
public class CartesianTopology3D extends CartesianTopology {

    private static final long serialVersionUID = -232323201189640523L;

    private static final int numInterfacePoints = 4;

    private static final int numElementInterfaces = 6;

    private static final int numElementPoints = 8;

    public CartesianTopology3D(Configuration config, int numi, int numj,
            int numk) {
        super(config, numi, numj, numk);
    }

    /**
     * Creates a naturally ordered 3D mesh topology.
     */
    public CartesianTopology3D(int numi, int numj, int numk) {
        super(numi, numj, numk);
    }

    @Override
    public int[] getPointElements(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        List<Integer> l = new ArrayList<Integer>();

        if (linearElementIJK.valid(i, j, k))
            l.add(getLinearElement(i, j, k));

        if (linearElementIJK.valid(i, j, k - 1))
            l.add(getLinearElement(i, j, k - 1));

        if (linearElementIJK.valid(i, j - 1, k))
            l.add(getLinearElement(i, j - 1, k));

        if (linearElementIJK.valid(i, j - 1, k - 1))
            l.add(getLinearElement(i, j - 1, k - 1));

        if (linearElementIJK.valid(i - 1, j, k))
            l.add(getLinearElement(i - 1, j, k));

        if (linearElementIJK.valid(i - 1, j, k - 1))
            l.add(getLinearElement(i - 1, j, k - 1));

        if (linearElementIJK.valid(i - 1, j - 1, k))
            l.add(getLinearElement(i - 1, j - 1, k));

        if (linearElementIJK.valid(i - 1, j - 1, k - 1))
            l.add(getLinearElement(i - 1, j - 1, k - 1));

        int[] ll = new int[l.size()];
        for (int n = 0, size = l.size(); n < size; n++)
            ll[n] = l.get(n);

        return ll;
    }

    @Override
    int getLinearInterface(int i, int j, int k, Orientation orient) {
        int l = orient.getLocalIndex();

        if (l < 0 || l > numElementInterfaces - 1)
            throw new IndexOutOfBoundsException("Invalid local interface index");

        int index = getLinearElement(i, j, k) * numElementInterfaces + l;

        return index;
    }

    @Override
    int getLinearInterfaceNeighbour(int i, int j, int k, Orientation orient) {
        switch (orient) {
        case BOTTOM:
            return getLinearInterface(i, j, k + 1, TOP);
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
        case TOP:
            // not valid for k=0
            return i + j * numi + (k - 1) * numi * numj;
        case BOTTOM:
            // not valid for k=numk-1
            return i + j * numi + k * numi * numj;
        case FRONT:
            // not valid for j=0;
            return i + (j - 1) * numi + k * numi * (numj - 1) + numi * numj
                    * (numk - 1);
        case BACK:
            // not valid for j=numj-1
            return i + j * numi + k * numi * (numj - 1) + numi * numj
                    * (numk - 1);
        case LEFT:
            // not valid for i=0
            return (i - 1) + j * (numi - 1) + k * (numi - 1) * numj + numi
                    * (numj - 1) * numk + numi * numj * (numk - 1);
        case RIGHT:
            // not valid for i=numi-1
            return i + j * (numi - 1) + k * (numi - 1) * numj + +numi
                    * (numj - 1) * numk + numi * numj * (numk - 1);
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

        if (linearElementIJK.valid(i, j, k)) {
            // pick out local (0,2,4)
            l.add(getLinearInterface(i, j, k, TOP));
            l.add(getLinearInterface(i, j, k, FRONT));
            l.add(getLinearInterface(i, j, k, LEFT));
        }

        if (linearElementIJK.valid(i, j, k - 1)) {
            // pick out local (1, 2, 4)
            l.add(getLinearInterface(i, j, k - 1, BOTTOM));
            l.add(getLinearInterface(i, j, k - 1, FRONT));
            l.add(getLinearInterface(i, j, k - 1, LEFT));
        }

        if (linearElementIJK.valid(i, j - 1, k)) {
            // pick out (0, 3, 4)
            l.add(getLinearInterface(i, j - 1, k, TOP));
            l.add(getLinearInterface(i, j - 1, k, BACK));
            l.add(getLinearInterface(i, j - 1, k, LEFT));
        }

        if (linearElementIJK.valid(i, j - 1, k - 1)) {
            // pick out (1, 3, 4)
            l.add(getLinearInterface(i, j - 1, k - 1, BOTTOM));
            l.add(getLinearInterface(i, j - 1, k - 1, BACK));
            l.add(getLinearInterface(i, j - 1, k - 1, LEFT));
        }

        if (linearElementIJK.valid(i - 1, j, k)) {
            // pick out (0, 2, 5)
            l.add(getLinearInterface(i - 1, j, k, TOP));
            l.add(getLinearInterface(i - 1, j, k, FRONT));
            l.add(getLinearInterface(i - 1, j, k, RIGHT));
        }

        if (linearElementIJK.valid(i - 1, j, k - 1)) {
            // pick out (1, 2, 5)
            l.add(getLinearInterface(i - 1, j, k - 1, BOTTOM));
            l.add(getLinearInterface(i - 1, j, k - 1, FRONT));
            l.add(getLinearInterface(i - 1, j, k - 1, RIGHT));
        }

        if (linearElementIJK.valid(i - 1, j - 1, k)) {
            // pick out (0, 3, 5)
            l.add(getLinearInterface(i - 1, j - 1, k, TOP));
            l.add(getLinearInterface(i - 1, j - 1, k, BACK));
            l.add(getLinearInterface(i - 1, j - 1, k, RIGHT));
        }

        if (linearElementIJK.valid(i - 1, j - 1, k - 1)) {
            // pick out (1, 3, 5)
            l.add(getLinearInterface(i - 1, j - 1, k - 1, BOTTOM));
            l.add(getLinearInterface(i - 1, j - 1, k - 1, BACK));
            l.add(getLinearInterface(i - 1, j - 1, k - 1, RIGHT));
        }

        // convert
        int[] ll = new int[l.size()];
        for (int n = 0, size = l.size(); n < size; n++)
            ll[n] = l.get(n);

        return ll;
    }

    /**
     * Returns an Integer array of global corner point indices for the current
     * input grid cell
     * 
     * @param ijk
     *            element ijk index-triplet
     * 
     * @return An int array of 8 global corner point indices for input grid cell
     * 
     */
    @Override
    public int[] getElementPoints(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        int[] cp = new int[numElementPoints];

        // top
        cp[0] = getLinearPoint(i, j, k);
        cp[1] = cp[0] + 1;
        cp[2] = getLinearPoint(i, j + 1, k) + 1;
        cp[3] = cp[2] - 1;

        // bottom
        cp[4] = getLinearPoint(i, j, k + 1);
        cp[5] = cp[4] + 1;
        cp[6] = getLinearPoint(i, j + 1, k + 1) + 1;
        cp[7] = cp[6] - 1;

        return cp;
    }

    /**
     * Creates an array of 6 global interface indices
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

    @Override
    boolean hasElementNeighbour(int i, int j, int k, Orientation orientation) {
        switch (orientation) {
        case TOP:
            return linearElementIJK.valid(i, j, k - 1);
        case BOTTOM:
            return linearElementIJK.valid(i, j, k + 1);
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
        case TOP:
            return new IJK(i, j, k - 1);
        case BOTTOM:
            return new IJK(i, j, k + 1);
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
        return (numi + 1) * (numj + 1) * (numk + 1);
    }

    @Override
    public int getNumInterfaces() {
        return numi * numj * numk * numElementInterfaces;
    }

    @Override
    public int getNumElements() {
        return numi * numj * numk;
    }

    @Override
    public int getNumConnections() {
        return 3 * numi * numj * numk - numi * numj - numi * numk - numj * numk;
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
    public int getNumPointElements(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        int ni = 2, nj = 2, nk = 2;
        if (i == 0 || i == numi)
            ni = 1;
        if (j == 0 || j == numj)
            nj = 1;

        if (k == 0 || k == numk)
            nk = 1;
        return ni * nj * nk;
    }

    @Override
    public int getNumInterfacePoints() {
        return numInterfacePoints;
    }

    @Override
    public boolean pointOnBoundary(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        return (i == 0 || i == numi) || (j == 0 || j == numj)
                || (k == 0 || k == numk);
    }

    @Override
    public int getNumPointInterfaces(IJK ijk) {
        int i = ijk.i(), j = ijk.j(), k = ijk.k();

        int ni = 2, nj = 2, nk = 2;
        if (i == 0 || i == numi)
            ni = 1;
        if (j == 0 || j == numj)
            nj = 1;
        if (k == 0 || k == numk)
            nk = 2;
        return 3 * ni * nj * nk;
    }

    @Override
    public int getDimension() {
        return 3;
    }
}
