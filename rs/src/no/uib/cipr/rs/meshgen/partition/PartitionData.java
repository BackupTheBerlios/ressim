package no.uib.cipr.rs.meshgen.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Subdomain;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.util.ArrayData;

/**
 * Class for storing partition topology data.
 */
public class PartitionData {

    private List<Set<Integer>> innerElements;

    private List<Set<Integer>> borderRingElements;

    private List<Map<Orientation, Set<Integer>>> boundaryElements;

    private List<Map<Orientation, Set<Integer>>> innerConnections;

    private List<Set<Integer>> domainConnections;

    private int numDomains;

    private Mesh mesh;

    // provides the geometry of each domain of the partition
    private Partition2D partition;

    // list of the inner domain dimensions
    private List<double[]> coordinates;

    private List<Map<Integer, List<Integer>>> domainPeerSend;

    private List<Map<Integer, List<Integer>>> domainPeerRecv;

    private ArrayList<int[]> globalElementMap;

    private ArrayList<int[]> globalConnectionMap;

    /**
     * Creates an upscale data object based on the given mesh partition and
     * underlying fine mesh for the domain with the given index.
     */
    public PartitionData(Partition2D partition, DomainTopology topology,
            Mesh mesh) {

        this.partition = partition;

        this.mesh = mesh;

        numDomains = partition.getNumDomains();

        initialize();

        for (int i = 0; i < numDomains; i++)
            coordinates.add(i, partition.getDomain(i).getDimensions());

        partition();

        // determine sendRecv
        sendRecv(topology);

        // save the global indices
        globalElementIndices(topology);

        // save the global connection indices
        globalConnectionIndices(topology);

        // convert to local indices for output
        convertToLocalIndices(topology);
    }

    private void globalConnectionIndices(DomainTopology topology) {
        for (int domain = 0; domain < numDomains; domain++) {
            Map<Integer, Integer> globToLoc = topology.getConnectionMap(domain);

            int[] connections = new int[globToLoc.size()];

            for (Map.Entry<Integer, Integer> c : globToLoc.entrySet()) {
                int global = c.getKey();
                int local = c.getValue();

                connections[local] = global;
            }

            globalConnectionMap.add(domain, connections);
        }
    }

    /**
     * Stores the global element indices for this domain.
     */
    private void globalElementIndices(DomainTopology topology) {
        for (int domain = 0; domain < numDomains; domain++) {
            int inner = innerElements.get(domain).size();
            int border = borderRingElements.get(domain).size();

            Map<Integer, Integer> globToLoc = topology.getElementMap(domain);

            int[] elements = new int[globToLoc.size()];

            if (globToLoc.size() != (inner + border))
                throw new IllegalArgumentException(
                        "Number of local elements must equal number of global-to-local elements");

            for (Map.Entry<Integer, Integer> e : globToLoc.entrySet()) {
                int global = e.getKey();
                int local = e.getValue();

                elements[local] = global;
            }

            globalElementMap.add(domain, elements);
        }
    }

    /**
     * Determine subdomain peers and corresponding send recv element indices.
     * 
     * @param topology
     */
    private void sendRecv(DomainTopology topology) {
        for (int dom = 0; dom < numDomains; dom++) {
            Domain2D domain = partition.getDomain(dom);

            for (NeighbourConnection c : mesh.neighbourConnections()) {
                Element here = mesh.here(c);
                Element there = mesh.there(c);

                int hereInner = topology.getInnerDomain(here);

                // skip to next connection if not in other inner domain
                if (hereInner == dom || hereInner < 0)
                    continue;

                int thereInner = topology.getInnerDomain(there);

                // skip to next connection if not in other domain
                if (thereInner == dom || thereInner < 0)
                    continue;

                if (hereInner == thereInner) {
                    int peer = hereInner;

                    boolean hereInBorder = domain.pointInBorderRing(here
                            .center);

                    boolean thereInBorder = domain.pointInBorderRing(there
                            .center);

                    Map<Integer, List<Integer>> recv = domainPeerRecv.get(dom);
                    Map<Integer, List<Integer>> send = domainPeerSend.get(peer);

                    boolean storeHere = hereInBorder && !thereInBorder;
                    boolean storeThere = !hereInBorder && thereInBorder;

                    boolean storeElement = storeHere || storeThere;

                    if (storeElement) {
                        int element;

                        element = storeHere ? hereindex : there
                                index;

                        if (!recv.containsKey(peer))
                            recv.put(peer, new ArrayList<Integer>());
                        recv.get(peer).add(element);

                        if (!send.containsKey(dom))
                            send.put(dom, new ArrayList<Integer>());
                        send.get(dom).add(element);
                    }
                }
            }
        }
        sendRecvToLocal(topology);
    }

    /**
     * Convert global indices in send/recv datastructure to domain-local indices
     * 
     * @param topology
     * 
     */
    private void sendRecvToLocal(DomainTopology topology) {
        // sort the global indices then get the corresponding local indices
        for (int domain = 0; domain < numDomains; domain++) {
            for (Map.Entry<Integer, List<Integer>> entry : domainPeerSend.get(
                    domain).entrySet()) {
                // remove duplicates from global element list and sort
                Set<Integer> global = new TreeSet<Integer>(entry.getValue());

                Set<Integer> local = topology.getLocalElements(domain, global);

                entry.setValue(new ArrayList<Integer>(local));
            }

            for (Map.Entry<Integer, List<Integer>> entry : domainPeerRecv.get(
                    domain).entrySet()) {
                // remove duplicates from global element list and sort
                Set<Integer> global = new TreeSet<Integer>(entry.getValue());

                Set<Integer> local = topology.getLocalElements(domain, global);

                entry.setValue(new ArrayList<Integer>(local));
            }
        }
    }

    /**
     * Converts the created global index lists to local index lists using the
     * domain topology.
     */
    private void convertToLocalIndices(DomainTopology topology) {
        for (int index = 0; index < numDomains; index++) {
            innerElements.set(index, topology.getLocalElements(index,
                    innerElements.get(index)));

            borderRingElements.set(index, topology.getLocalElements(index,
                    borderRingElements.get(index)));

            domainConnections.set(index, topology.getLocalConnections(index,
                    domainConnections.get(index)));

            Map<Orientation, Set<Integer>> bmap = boundaryElements.get(index);
            Map<Orientation, Set<Integer>> cmap = innerConnections.get(index);
            for (Orientation orient : Orientation.getOrientations2D()) {
                bmap.put(orient, topology.getLocalElements(index, bmap
                        .get(orient)));
                cmap.put(orient, topology.getLocalConnections(index, cmap
                        .get(orient)));
            }
            boundaryElements.set(index, bmap);
            innerConnections.set(index, cmap);
        }
    }

    private void initialize() {
        coordinates = new ArrayList<double[]>(numDomains);
        innerElements = new ArrayList<Set<Integer>>(numDomains);
        borderRingElements = new ArrayList<Set<Integer>>(numDomains);
        domainConnections = new ArrayList<Set<Integer>>(numDomains);

        globalElementMap = new ArrayList<int[]>();
        globalConnectionMap = new ArrayList<int[]>();

        boundaryElements = new ArrayList<Map<Orientation, Set<Integer>>>(
                numDomains);
        innerConnections = new ArrayList<Map<Orientation, Set<Integer>>>(
                numDomains);

        domainPeerSend = new ArrayList<Map<Integer, List<Integer>>>(numDomains);
        domainPeerRecv = new ArrayList<Map<Integer, List<Integer>>>(numDomains);

        // initialize lists
        for (int i = 0; i < numDomains; i++) {
            innerElements.add(new HashSet<Integer>());
            borderRingElements.add(new HashSet<Integer>());
            domainConnections.add(new HashSet<Integer>());

            domainPeerSend.add(new HashMap<Integer, List<Integer>>());
            domainPeerRecv.add(new HashMap<Integer, List<Integer>>());

            boundaryElements.add(new HashMap<Orientation, Set<Integer>>());
            innerConnections.add(new HashMap<Orientation, Set<Integer>>());

            for (Orientation orient : Orientation.getOrientations2D()) {
                boundaryElements.get(i).put(orient, new HashSet<Integer>());
                innerConnections.get(i).put(orient, new HashSet<Integer>());
            }
        }
    }

    private void partition() {
        Element here, there;

        int hIndex, tIndex;

        Point3D hCenter, tCenter;

        boolean hInDomain, tInDomain;

        // loop through all neighbour connections of the underlying mesh
        for (NeighbourConnection c : mesh.neighbourConnections()) {
            int cIndex = index;

            here = mesh.here(c);
            there = mesh.there(c);

            hCenter = here.center;
            tCenter = there.center;

            hIndex = hereindex;
            tIndex = thereindex;

            // check relation with each domain
            for (int domInd = 0; domInd < numDomains; domInd++) {
                Domain2D domain = partition.getDomain(domInd);

                hInDomain = domain.pointInDomain(hCenter);
                tInDomain = domain.pointInDomain(tCenter);

                // 4 possible configurations
                if (hInDomain) {
                    if (tInDomain) { // i in, j in
                        // add connection to domain connection list
                        domainConnections.get(domInd).add(cIndex);

                        // check inner/border ring connection and add
                        boolean hInInner = domain.pointInInner(hCenter);
                        boolean tInInner = domain.pointInInner(tCenter);

                        // 4 possible configurations
                        if (hInInner) {
                            // add here to inner element list
                            innerElements.get(domInd).add(hIndex);

                            if (tInInner) {
                                // add there to inner element list
                                innerElements.get(domInd).add(tIndex);
                            } else {
                                // add there to border ring element list
                                borderRingElements.get(domInd).add(tIndex);

                                // connection is inner/border ring connection
                                // get orientation and add
                                Orientation orient = domain.getClosestInner(c
                                        .hereInterface().center);
                                innerConnections.get(domInd).get(orient).add(
                                        cIndex);
                            }

                        } else {
                            // add here to border ring element list
                            borderRingElements.get(domInd).add(hIndex);

                            if (tInInner) {
                                // add there to inner element list
                                innerElements.get(domInd).add(tIndex);

                                // connection is inner/border ring connection
                                // get orientation and add
                                Orientation orient = domain.getClosestInner(c
                                        .hereInterface().center);
                                innerConnections.get(domInd).get(orient).add(
                                        cIndex);
                            } else {
                                // add there to border ring element list
                                borderRingElements.get(domInd).add(tIndex);
                            }
                        }

                    } else { // i in, j not in
                        // TODO consistency check: i may not be in inner!

                        // check which boundary here belongs to and add
                        Point3D center = mesh.hereInterface(c).center;
                        Orientation orient = domain.getClosestBoundary(center);
                        boundaryElements.get(domInd).get(orient).add(hIndex);

                    }
                } else if (!hInDomain) {
                    if (tInDomain) { // i not in, j in
                        // TODO consistency check: j may not be in inner!

                        // check which boundary there belongs to and add
                        Point3D center = mesh.hereInterface(c).center;
                        Orientation orient = domain.getClosestBoundary(center);
                        boundaryElements.get(domInd).get(orient).add(tIndex);

                    } else { // i not in, j not in
                        // do nothing
                    }
                }
            }
        }
    }

    public Subdomain getSubdomain(Mesh subMesh, int domain) {

        int rank = domain;
        int size = numDomains;

        int[] peer = getPeers(domain);

        int[][] send = new int[numDomains][];
        int[][] recv = new int[numDomains][];
        for (int i = 0; i < numDomains; ++i) {
            send[i] = getPeerSendElements(domain, i);
            recv[i] = getPeerRecvElements(domain, i);
        }

        int[] elementMap = getLocalToGlobalElements(domain);
        int[] connectionMap = getLocalToGlobalConnections(domain);

        double[] coordinates = getCoordinates(domain);
        int[] innerElements = getInnerElements(domain);

        int[] borderElements = getBorderElements(domain);

        Map<Orientation, int[]> boundaryElements = new HashMap<Orientation, int[]>();
        for (Orientation orient : Orientation.getOrientations2D())
            boundaryElements.put(orient, getBoundaryElements(domain, orient));

        Map<Orientation, int[]> innerConnections = new HashMap<Orientation, int[]>();
        for (Orientation orient : Orientation.getOrientations2D())
            innerConnections.put(orient, getInnerConnections(domain, orient));

        return new Subdomain(subMesh, rank, size, peer, send, recv, elementMap,
                connectionMap, coordinates, innerElements, borderElements,
                boundaryElements, innerConnections);
    }

    /**
     * Returns an array of border ring element indices (global) for the domain
     * with the given index.
     */
    public int[] getBorderElements(int i) {
        Set<Integer> set = borderRingElements.get(i);
        return ArrayData.integerSetToSortedArray(set);
    }

    /**
     * Returns an array of element indices (global) for the domain with the
     * given index.
     */
    public int[] getInnerElements(int i) {
        Set<Integer> set = innerElements.get(i);
        return ArrayData.integerSetToSortedArray(set);
    }

    /**
     * Returns an array of inner connection indices (global) for the domain with
     * the given index and the boundary at the given orientation.
     */
    public int[] getInnerConnections(int i, Orientation orientation) {
        Set<Integer> set = innerConnections.get(i).get(orientation);
        return ArrayData.integerSetToSortedArray(set);
    }

    /**
     * Returns the list of integer lists of domain (inner and border ring)
     * connections for the domain with the given index.
     */
    public int[] getDomainConnections(int i) {
        Set<Integer> set = domainConnections.get(i);
        return ArrayData.integerSetToSortedArray(set);
    }

    /**
     * Returns an array of boundary element indices (global) for the domain with
     * the given index and boundary of the given orientation type.
     */
    public int[] getBoundaryElements(int i, Orientation orientation) {
        Set<Integer> set = boundaryElements.get(i).get(orientation);
        return ArrayData.integerSetToSortedArray(set);
    }

    /**
     * Returns an array containing the diagonal corner point coordinates (x0,
     * y0, x1, y1) of the inner domain with the given index.
     */
    public double[] getCoordinates(int i) {
        return coordinates.get(i);
    }

    public int[] getPeers(int index) {
        Set<Integer> sendPeers = domainPeerSend.get(index).keySet();
        Set<Integer> recvPeers = domainPeerRecv.get(index).keySet();

        if (!sendPeers.equals(recvPeers))
            throw new IllegalArgumentException(
                    "Set of send and receive peers are not equal for domain "
                            + index);

        return ArrayData.integerSetToSortedArray(sendPeers);
    }

    public int[] getPeerSendElements(int index, int peer) {
        List<Integer> send = domainPeerSend.get(index).get(peer);

        if (send == null)
            return new int[0];
        else
            return ArrayData.integerListToArray(send);
    }

    public int[] getPeerRecvElements(int index, int peer) {
        List<Integer> recv = domainPeerRecv.get(index).get(peer);

        if (recv == null)
            return new int[0];
        else
            return ArrayData.integerListToArray(recv);
    }

    public int[] getLocalToGlobalElements(int index) {
        return globalElementMap.get(index);
    }

    public int[] getLocalToGlobalConnections(int index) {
        return globalConnectionMap.get(index);
    }

}
