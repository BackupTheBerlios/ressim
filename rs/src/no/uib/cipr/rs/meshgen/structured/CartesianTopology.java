package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.util.Configuration;

/**
 * A structured topology.
 */
public abstract class CartesianTopology extends Topology {

    protected int numi, numj, numk;

    protected LinearIJKOrdering linearElementIJK;

    protected LinearIJKOrdering linearPointIJK;

    /**
     * Creates a structured mesh topology.
     * 
     * @param config
     * @param numi
     * @param numj
     * @param numk
     */
    public CartesianTopology(Configuration config, int numi, int numj, int numk) {
        linearElementIJK = LinearIJKOrdering.create(config, numi, numj, numk);

        construct(numi, numj, numk);
    }

    /**
     * Creates a naturally numbered structured mesh topology
     * 
     * @param numi
     * @param numj
     * @param numk
     */
    public CartesianTopology(int numi, int numj, int numk) {
        linearElementIJK = new NaturalIJKOrdering(numi, numj, numk);

        construct(numi, numj, numk);
    }

    private void construct(int numi, int numj, int numk) {
        this.numi = numi;
        this.numj = numj;
        this.numk = numk;

        linearPointIJK = new NaturalIJKOrdering(numi + 1, numj + 1, numk + 1);

        setSizes(getNumPoints(), getNumInterfaces(), getNumElements(),
                getNumConnections(), 0);

        // Build the graph topology
        for (IJK ijk : linearElementIJK) {

            // Build the interface topology
            for (Orientation orient : Orientation
                    .getOrientations(getDimension()))
                buildInterfaceTopology(getLinearInterface(ijk, orient),
                        getInterfacePoints(ijk, orient));

            // Build the element topology
            buildElementTopology(getLinearElement(ijk),
                    getElementInterfaces(ijk));
        }

        // Build the neighbour connection topology
        for (IJK ijk : linearElementIJK)
            for (Orientation forward : Orientation
                    .getForwardOrientations(getDimension()))
                if (hasElementNeighbour(ijk, forward)) {
                    int index = getLinearConnection(ijk, forward);

                    int here = getLinearInterface(ijk, forward);
                    int there = getLinearInterfaceNeighbour(ijk, forward);

                    buildNeighbourConnectionTopology(index, here, there);
                }
    }

    /**
     * Returns the number of points in i-direction
     */
    public int getNumPointsI() {
        return numi + 1;
    }

    /**
     * Returns the number of points in j-direction
     */
    public int getNumPointsJ() {
        return numj + 1;
    }

    /**
     * Returns the number of points in k-direction
     */
    public int getNumPointsK() {
        return numk + 1;
    }

    /**
     * Returns the number of elements in i-direction
     */
    public int getNumElementI() {
        return numi;
    }

    /**
     * Returns the number of elements in j-direction
     */
    public int getNumElementJ() {
        return numj;
    }

    /**
     * Returns the number of elements in k-direction
     */
    public int getNumElementK() {
        return numk;
    }

    /**
     * @return an iterable object of IJK-triplets
     */
    public Iterable<IJK> getElementsIJK() {
        return linearElementIJK.getIJK();
    }

    public Iterable<IJK> getPointsIJK() {
        return linearPointIJK.getIJK();
    }

    /**
     * returns true if the given element ijk-triplet is within bounds
     */
    public boolean isValidElement(IJK ijk) {
        return linearElementIJK.valid(ijk.i(), ijk.j(), ijk.k());
    }

    // linear indices
    /**
     * Returns the linear corner point index for the given point ijk-triplet.
     */
    public int getLinearPoint(IJK ijk) {
        return getLinearPoint(ijk.i(), ijk.j(), ijk.k());
    }

    int getLinearPoint(int i, int j, int k) {
        return linearPointIJK.getLinear(i, j, k);
    }

    /**
     * Returns the linear element index for the given element ijk-triplet.
     */
    public int getLinearElement(IJK ijk) {
        return getLinearElement(ijk.i(), ijk.j(), ijk.k());
    }

    int getLinearElement(int i, int j, int k) {
        return linearElementIJK.getLinear(i, j, k);
    }

    /**
     * Returns a linear interface index given the containing element (i, j, k)
     * and interface orientation type.
     * 
     * @param ijk
     *            element ijk-index triplet
     * @param orient
     *            Interface orientation type
     * @return Global interface index
     */
    public int getLinearInterface(IJK ijk, Orientation orient) {
        return getLinearInterface(ijk.i(), ijk.j(), ijk.k(), orient);
    }

    abstract int getLinearInterface(int i, int j, int k, Orientation orient);

    public int getLinearInterfaceNeighbour(IJK ijk, Orientation orientation) {
        return getLinearInterfaceNeighbour(ijk.i(), ijk.j(), ijk.k(),
                orientation);
    }

    abstract int getLinearInterfaceNeighbour(int i, int j, int k,
            Orientation orientation);

    public int getLinearConnection(IJK ijk, Orientation orient) {
        return getLinearConnection(ijk.i(), ijk.j(), ijk.k(), orient);
    }

    abstract int getLinearConnection(int i, int j, int k, Orientation orient);

    // tests

    public abstract boolean pointOnBoundary(IJK ijk);

    /**
     * Returns true if the given element has a neighbour in the given
     * orientation
     */
    public boolean hasElementNeighbour(IJK ijk, Orientation orientation) {
        return hasElementNeighbour(ijk.i(), ijk.j(), ijk.k(), orientation);
    }

    abstract boolean hasElementNeighbour(int i, int j, int k,
            Orientation orientation);

    /**
     * Returns the ijk-index of the neighbouring element of the element with the
     * given index in the given direction.
     */
    public IJK getElementNeighbour(IJK ijk, Orientation orientation) {
        return getElementNeighbour(ijk.i(), ijk.j(), ijk.k(), orientation);
    }

    abstract IJK getElementNeighbour(int i, int j, int k,
            Orientation orientation);

    // point topology

    public abstract int[] getPointElements(IJK ijk);

    /**
     * Returns an array of global indices of all valid interfaces that share
     * point ijk.
     */
    public abstract int[] getPointInterfaces(IJK ijk);

    /**
     * Returns linear index of interface's vertices
     */
    public abstract int[] getInterfacePoints(IJK ijk, Orientation orient);

    /**
     * Returns an array of global point indices for the given element
     */
    public abstract int[] getElementPoints(IJK ijk);

    /**
     * Creates an array of global interface indices for the given element
     */
    public abstract int[] getElementInterfaces(IJK ijk);

    @Override
    public abstract int getNumPoints();

    @Override
    public abstract int getNumInterfaces();

    @Override
    public abstract int getNumElements();

    @Override
    public abstract int getNumConnections();

    public abstract int getNumInterfacePoints();

    public abstract int getNumElementInterfaces();

    public abstract int getNumElementPoints();

    public abstract int getNumPointElements(IJK ijk);

    public abstract int getNumPointInterfaces(IJK ijk);

    public abstract int getDimension();

}