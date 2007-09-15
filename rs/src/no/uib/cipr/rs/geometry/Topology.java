package no.uib.cipr.rs.geometry;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import no.uib.cipr.rs.meshgen.util.ArrayData;

/**
 * A graph topology
 */
public class Topology implements Serializable {

    private static final long serialVersionUID = -4952938414007305641L;

    private static final int HERE = 0, THERE = 1;

    private int numPoints, numInterfaces, numElements, numConnections,
            numNeighbourConnections, numNonNeighbourConnections;

    private Iterable<Integer> points, interfaces, elements, connections,
            neighbourConnections, nonNeighbourConnections;

    /**
     * Set of interface indices per point. No duplicates.
     */
    private List<Set<Integer>> pointInterfaces;

    /**
     * Set of element indices per point. No duplicates.
     */
    private List<Set<Integer>> pointElements;

    /**
     * Set of point indices per interface. No duplicates allowed. Set must be
     * sorted.
     * 
     * TODO implement check for counter-clockwise sorted set of points.
     */
    private List<Set<Integer>> interfacePoints;

    /**
     * Set of point indices per element. No duplicates allowed. Set must be
     * sorted.
     * 
     * TODO implement check for counter-clockwise sorted set of points.
     */
    private List<Set<Integer>> elementPoints;

    /**
     * Set of interface indices per element. No duplicates allowed. Set must be
     * sorted.
     * 
     * TODO implement check for counter-clockwise sorted set of interfaces.
     */
    private List<Set<Integer>> elementInterfaces;

    /**
     * List of element indices per interface.
     */
    private List<Integer> interfaceElement;

    /**
     * List of Connection indices per interface.
     */
    private List<Integer> interfaceConnection;

    /**
     * List of boolean values per interface, true if interface is boundary.
     */
    private List<Boolean> isBoundaryInterface;

    /**
     * List of interface here and there indices per neighbour connection.
     */
    private List<List<Integer>> neighbourConnectionInterfaces;

    /**
     * List of element here and there indices per non-neighbour connection.
     */
    private List<List<Integer>> nonNeighbourConnectionElements;

    /**
     * List of Connection indices per element.
     */
    private List<Set<Integer>> elementConnection;

    public Topology() {
        // subclas constructor
    }

    /**
     * Sets the sizes, and allocates datastructures
     */
    public void setSizes(int numPoints, int numInterfaces, int numElements,
            int numNeighbourConnections, int numNonNeighbourConnections) {
        setNumPoints(numPoints);
        setNumInterfaces(numInterfaces);
        setNumElements(numElements);
        setNumConnections(numNeighbourConnections, numNonNeighbourConnections);
    }

    public void setNumPoints(int numPoints) {
        this.numPoints = numPoints;

        points = ArrayData.getLinearListInteger(numPoints);

        pointInterfaces = ArrayData.getListSetInteger(numPoints);
        pointElements = ArrayData.getListSetInteger(numPoints);
    }

    public void setNumInterfaces(int numInterfaces) {
        this.numInterfaces = numInterfaces;

        interfaces = ArrayData.getLinearListInteger(numInterfaces);

        interfacePoints = ArrayData.getListSetInteger(numInterfaces);
        interfaceElement = ArrayData.getListInteger(numInterfaces);
        interfaceConnection = ArrayData.getListInteger(numInterfaces);

        // array elements set to false for inner interfaces
        isBoundaryInterface = ArrayData.getListBoolean(numInterfaces, true);
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;

        elements = ArrayData.getLinearListInteger(numElements);

        elementPoints = ArrayData.getListSetInteger(numElements);
        elementInterfaces = ArrayData.getListSetInteger(numElements);
        elementConnection = ArrayData.getListSetInteger(numElements);
    }

    public void setNumConnections(int numNeighbourConnections,
            int numNonNeighbourConnections) {
        this.numNeighbourConnections = numNeighbourConnections;
        this.numNonNeighbourConnections = numNonNeighbourConnections;
        numConnections = numNeighbourConnections + numNonNeighbourConnections;

        neighbourConnections = ArrayData
                .getLinearListInteger(numNeighbourConnections);
        nonNeighbourConnections = ArrayData
                .getLinearListInteger(numNonNeighbourConnections);

        connections = ArrayData.getLinearListInteger(numConnections);
        neighbourConnectionInterfaces = ArrayData
                .getListListInteger(numNeighbourConnections);
        nonNeighbourConnectionElements = ArrayData
                .getListListInteger(numNonNeighbourConnections);
    }

    /**
     * Builds list of elements per non-neighbour connection
     * 
     * @param here
     *            Index to the here element
     * @param there
     *            Index to the there element
     */
    public void buildNonNeighbourConnectionTopology(int connection, int here,
            int there) {
        nonNeighbourConnectionElements.get(connection).add(here);
        nonNeighbourConnectionElements.get(connection).add(there);

        elementConnection.get(here).add(connection);
        elementConnection.get(there).add(connection);
    }

    /**
     * Builds list of interfaces per neighbour connection, list of connections
     * per interface and adds false values for inner interfaces to the list of
     * isBoundary-boolean values for all interfaces. Should be called after the
     * interface topology has been built
     * 
     * @param here
     *            Index to the here interface
     * @param there
     *            Index to the there interface
     */
    public void buildNeighbourConnectionTopology(int connection, int here,
            int there) {
        neighbourConnectionInterfaces.get(connection).add(here);
        neighbourConnectionInterfaces.get(connection).add(there);

        if (interfaceConnection.set(here, connection) >= 0)
            throw new IllegalArgumentException(
                    "Interface 'here' has already been associated with a connection");
        if (interfaceConnection.set(there, connection) >= 0)
            throw new IllegalArgumentException(
                    "Interface 'there' has already been associated with a connection");

        isBoundaryInterface.set(here, false);
        isBoundaryInterface.set(there, false);
    }

    /**
     * Associates a set of interfaces with an element, and vice versa. This
     * also associates the element with the interface points, hence this method
     * should be called after the interface topology has been built.
     * 
     * @param element
     *            Element index
     * @param interfaces
     *            Interface indices
     */
    public void buildElementTopology(int element, int[] interfaces) {
        Set<Integer> ei = elementInterfaces.get(element);
        Set<Integer> ep = elementPoints.get(element);
        for (int interf : interfaces) {

            // Add the point to the interface
            ei.add(interf);

            // Add this element to all the interfaces
            interfaceElement.set(interf, element);

            // Add all points on the interface to this element
            Set<Integer> ip = interfacePoints.get(interf);
            ep.addAll(ip);

            // Add this element to all the interface points
            for (int point : ip)
                pointElements.get(point).add(element);
        }
    }

    /**
     * Associates an interface with the given points. The reverse mapping is
     * also done
     * 
     * @param interf
     *            Interface index
     * @param points
     *            Point indices
     */
    public void buildInterfaceTopology(int interf, int[] points) {
        Set<Integer> ip = interfacePoints.get(interf);
        for (int point : points) {

            // Add the point to the interface
            ip.add(point);

            // Add this interface to the current point
            pointInterfaces.get(point).add(interf);
        }
    }

    /**
     * Returns the number of points in this topology.
     */
    public int getNumPoints() {
        return numPoints;
    }

    /**
     * Returns an iterable object of all integer point indices of this topology.
     */
    public Iterable<Integer> getPointIndices() {
        return points;
    }

    /**
     * Returns the total number of connections in this topology.
     */
    public int getNumConnections() {
        return numConnections;
    }

    /**
     * Returns the number of neighbour connections.
     */
    public int getNumNeighbourConnections() {
        return numNeighbourConnections;
    }

    /**
     * Returns the number of non-neighbour connections.
     */
    public int getNumNonNeighbourConnections() {
        return numNonNeighbourConnections;
    }

    /**
     * Returns an iterable object of neighbour connection indices.
     */
    public Iterable<Integer> getNeighbourConnectionIndices() {
        return neighbourConnections;
    }

    /**
     * Returns an iterable object of non-neighbour connection indices.
     */
    public Iterable<Integer> getNonNeighbourConnectionIndices() {
        return nonNeighbourConnections;
    }

    /**
     * Returns an iterable object of all connection indices in this topology.
     */
    public Iterable<Integer> getConnectionIndices() {
        return connections;
    }

    /**
     * Returns the number of elements in this topology.
     */
    public int getNumElements() {
        return numElements;
    }

    /**
     * Returns an integer array of element indices for this point.
     */
    public int[] getPointElements(int i) {
        return ArrayData.integerCollectionToArray(pointElements.get(i));
    }

    /**
     * Returns the number of interfaces in this topology.
     */
    public int getNumInterfaces() {
        return numInterfaces;
    }

    /**
     * Returns an iterable object of all integer interface indices in this
     * topology.
     */
    public Iterable<Integer> getInterfaceIndices() {
        return interfaces;
    }

    /**
     * Returns an array of interface indices for this point.
     */
    public int[] getPointInterfaces(int i) {
        return ArrayData.integerCollectionToArray(pointInterfaces.get(i));
    }

    /**
     * Returns the element index of the given interface.
     */
    public int getInterfaceElement(int i) {
        return interfaceElement.get(i);
    }

    /**
     * Returns the neighbour connection index of this interface.
     */
    public int getInterfaceConnection(int i) {
        if (isBoundaryInterface.get(i))
            throw new IllegalArgumentException(
                    "No connection for boundary interface.");

        return interfaceConnection.get(i);
    }

    /**
     * Returns the non-neighbour connection indices of this element.
     */
    public int[] getElementConnection(int i) {
        return ArrayData.integerSetToSortedArray(elementConnection.get(i));
    }

    /**
     * Returns an iterable object of all integer element indices in this
     * topology.
     */
    public Iterable<Integer> getElementIndices() {
        return elements;
    }

    /**
     * Returns an integer array of point indices for this element.
     */
    public int[] getElementPoints(int i) {
        return ArrayData.integerCollectionToArray(elementPoints.get(i));
    }

    /**
     * Returns an integer array of interface indices for this element.
     */
    public int[] getElementInterfaces(int i) {
        return ArrayData.integerCollectionToArray(elementInterfaces.get(i));
    }

    /**
     * Returns an array of point indices for this interface.
     */
    public int[] getInterfacePoints(int i) {
        return ArrayData.integerCollectionToArray(interfacePoints.get(i));
    }

    /**
     * Returns the 'here'-interface index of the neighbour connection with the
     * given index.
     */
    public int getNeighbourConnectionHere(int i) {
        return neighbourConnectionInterfaces.get(i).get(HERE);
    }

    /**
     * Returns the 'there'-interface index of the neighbour connection with the
     * given index.
     */
    public int getNeighbourConnectionThere(int i) {
        return neighbourConnectionInterfaces.get(i).get(THERE);
    }

    /**
     * Returns the index of the here-element of the non-neighbour connection
     * with the given index.
     */
    public int getNonNeighbourConnectionHere(int i) {
        return nonNeighbourConnectionElements.get(i).get(HERE);
    }

    /**
     * Returns the index of the there-element of the non-neighbour connection
     * with the given index.
     */
    public int getNonNeighbourConnectionThere(int i) {
        return nonNeighbourConnectionElements.get(i).get(THERE);
    }

    /**
     * Returns true if this interface is at boundary.
     */
    public boolean isBoundaryInterface(int i) {
        return isBoundaryInterface.get(i);
    }

}
