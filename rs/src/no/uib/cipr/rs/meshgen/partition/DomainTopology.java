package no.uib.cipr.rs.meshgen.partition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Point3D;

/**
 * Class for creating index maps between global (underlying fine mesh) and local
 * (per domain) indices.
 */
public class DomainTopology {

    private List<Set<Integer>> globalElem;

    private List<Set<Integer>> globalConn;

    private List<Set<Integer>> globalPoint;

    private List<Set<Integer>> globalIntf;

    private int numDomains;

    // topological maps for each domain: key - global index, value - local index
    private ArrayList<Map<Integer, Integer>> pointMap;

    private ArrayList<Map<Integer, Integer>> interfaceMap;

    private ArrayList<Map<Integer, Integer>> elementMap;

    private ArrayList<Map<Integer, Integer>> connectionMap;

    private HashMap<Integer, DomainLocal<Integer, Integer>> elementGlobalToLocal;

    // cache of the center point of an element to the domain that contains this
    // element in its inner region. this allows us to determine which of the
    // entries in elementMap that should be used to map to the local address for
    // that domain. (the index of the element may vary depending on whether we
    // are looking at the element in the fine mesh or in the partition, but the
    // center point will always be the same). the local address is not yet
    // assigned when we create this map (see comments below for the construction
    // of elementGlobalToLocal). within one run the center point does not change
    // and we gain 10% by using an identity hash map (i.e. only exactly the same
    // point matches -- the equals() method is not used).
    // TODO: replace this map with elementCenterToGlobal, and use the global
    // index to figure out the domain (requires a clarification of
    // elementGlobalToLocal)
    private IdentityHashMap<Point3D, Domain2D> elementCenterToDomain;

    private final static int NOT_IN_ANY_DOMAIN = -1;

    /**
     * Returns the index of the domain that has the given element within its
     * inner part. A negative number is returned if the given element is not
     * within any inner domain.
     */
    public int getInnerDomain(Element element) {
        Point3D center = element.center;
        Domain2D domain = elementCenterToDomain.get(center);
        return domain == null ? NOT_IN_ANY_DOMAIN : domainindex;
    }

    /**
     * Creates the domain topology from the given partition geometry and
     * underlying fine mesh.
     * 
     * @param partition
     *            Partition geometry
     * @param mesh
     *            Underlying fine mesh
     */
    public DomainTopology(Partition2D partition, Mesh mesh) {
        numDomains = partition.getNumDomains();

        // store unique geometry object indices for each domain
        globalElem = new ArrayList<Set<Integer>>(numDomains);
        globalConn = new ArrayList<Set<Integer>>(numDomains);

        // initialize
        for (int i = 0; i < numDomains; i++) {
            // insertion ordered unique indices achieved using LinkedHashSet
            globalElem.add(new LinkedHashSet<Integer>());
            globalConn.add(new LinkedHashSet<Integer>());
        }

        // setup cache that let us answer to reverse domain membership queries
        List<Element> elements = mesh.elements();
        elementCenterToDomain = new IdentityHashMap<Point3D, Domain2D>(elements
                .size());
        for (Element element : elements) {
            Point3D center = element.center;
            for (Domain2D domain : partition.domains())
                if (domain.pointInInner(center))
                    elementCenterToDomain.put(center, domain);
        }

        // The mesh partition is done by looping over all neighbour
        // connections of the underlying fine mesh. For each connection, the
        // routine determines which domain that contains the here- and
        // there-element center points.
        // 
        // Element and connection indices are listed for each domain.
        // 
        // 4 possible element-domain configurations:
        // 
        // 1. here and there within domain: add element and connection indices
        // 
        // 2. here within domain: add here element index
        // 
        // 3. there within domain: add there element index
        //  
        // 4. none within domain: skip
        //
        // loop through all connections of the underlying mesh and determine
        // which domain elements and connections belong to. The indices are
        // stored per domain in insertion ordered sets. These sets are used
        // in subsequent extraction of other geometry and topology data
        for (NeighbourConnection c : mesh.neighbourConnections()) {
            int connInd = index;

            Element here = mesh.here(c);
            Element there = mesh.there(c);

            Point3D hereCenter = here.center;
            Point3D thereCenter = there.center;

            int hereInd = hereindex;
            int thereInd = thereindex;

            // check location of center point relative to each domain
            for (Domain2D domain : partition.domains()) {
                int domInd = domainindex;

                boolean hereIn = domain.pointInDomain(hereCenter);
                boolean thereIn = domain.pointInDomain(thereCenter);

                if (hereIn) {
                    globalElem.get(domInd).add(hereInd);
                    if (thereIn) {
                        globalElem.get(domInd).add(thereInd);
                        globalConn.get(domInd).add(connInd);
                    }
                } else {
                    if (thereIn) {
                        globalElem.get(domInd).add(thereInd);
                    }
                }
            }
        }

        // extract point and interface indices for each domain
        globalPoint = new ArrayList<Set<Integer>>(numDomains);
        globalIntf = new ArrayList<Set<Integer>>(numDomains);

        for (int domInd = 0; domInd < numDomains; domInd++) {
            // for each element, add point and interface indices, insertion
            // order determines subsequent local indexing for the current
            // domain.
            Set<Integer> pnts = new LinkedHashSet<Integer>();
            Set<Integer> intf = new LinkedHashSet<Integer>();

            for (int elem : globalElem.get(domInd)) {
                Element element = mesh.elements().get(elem);
                for (CornerPoint p : element.points())
                    pnts.add(pindex);
                for (Interface i : element.interfaces())
                    intf.add(iindex);
            }

            globalPoint.add(domInd, pnts);
            globalIntf.add(domInd, intf);
        }

        // set up maps between global and local indices per domain
        pointMap = new ArrayList<Map<Integer, Integer>>(numDomains);
        interfaceMap = new ArrayList<Map<Integer, Integer>>(numDomains);
        elementMap = new ArrayList<Map<Integer, Integer>>(numDomains);
        connectionMap = new ArrayList<Map<Integer, Integer>>(numDomains);

        for (int domInd = 0; domInd < numDomains; domInd++) {
            pointMap.add(domInd, globalToLocalMap(getPointIndices(domInd)));
            interfaceMap.add(domInd,
                    globalToLocalMap(getInterfaceIndices(domInd)));
            elementMap.add(domInd, globalToLocalMap(getElementIndices(domInd)));
            connectionMap.add(domInd,
                    globalToLocalMap(getConnectionIndices(domInd)));
        }

        // set up map between global element index and (domain, local) index
        // pair.
        elementGlobalToLocal = new HashMap<Integer, DomainLocal<Integer, Integer>>();
        for (int domInd = 0; domInd < numDomains; domInd++) {
            Map<Integer, Integer> globLoc = elementMap.get(domInd);
            for (int global : globLoc.keySet()) {
                int local = globLoc.get(global);

                // Only include elements in the interior of the domain
                if (getInnerDomain(mesh.elements().get(global)) != domInd)
                    continue;

                DomainLocal<Integer, Integer> domainLocal = new DomainLocal<Integer, Integer>(
                        domInd, local);

                elementGlobalToLocal.put(global, domainLocal);
            }
        }
    }

    public Collection<Integer> getConnectionIndices(int domInd) {
        return globalConn.get(domInd);
    }

    public Collection<Integer> getElementIndices(int domInd) {
        return globalElem.get(domInd);
    }

    public Collection<Integer> getInterfaceIndices(int domInd) {
        return globalIntf.get(domInd);
    }

    public Collection<Integer> getPointIndices(int domInd) {
        return globalPoint.get(domInd);
    }

    private Map<Integer, Integer> globalToLocalMap(
            Collection<Integer> globalIndices) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        int local = 0;
        for (int global : globalIndices)
            map.put(global, local++);

        return map;
    }

    /**
     * Returns the point index map for the given domain
     * 
     * @param index
     *            Domain index
     */
    public Map<Integer, Integer> getPointMap(int index) {
        return pointMap.get(index);
    }

    /**
     * Returns the interface index map for the given domain
     * 
     * @param index
     *            Domain index
     */
    public Map<Integer, Integer> getInterfaceMap(int index) {
        return interfaceMap.get(index);
    }

    /**
     * Returns the element index map for the given domain
     * 
     * @param index
     *            Domain index
     */
    public Map<Integer, Integer> getElementMap(int index) {
        return elementMap.get(index);
    }

    /**
     * Returns the neighbour connection index map for the given domain
     * 
     * @param index
     *            Domain index
     */
    public Map<Integer, Integer> getConnectionMap(int index) {
        return connectionMap.get(index);
    }

    /**
     * Returns a set of local element indices corresponding to the given set of
     * global element indices for the domain with the given index.
     * 
     * @param index
     *            Domain index
     * @param global
     *            Set of global element indices.
     */
    public Set<Integer> getLocalElements(int index, Set<Integer> global) {
        Map<Integer, Integer> map = getElementMap(index);

        Set<Integer> local = new LinkedHashSet<Integer>(global.size());

        for (Integer i : global)
            local.add(map.get(i));

        return local;
    }

    /**
     * Returns a set of local connection indices corresponding to the given set
     * of global connection indices for the domain with the given index.
     * 
     * @param index
     *            Domain index
     * @param global
     *            Set of global connection indices
     */
    public Set<Integer> getLocalConnections(int index, Set<Integer> global) {
        Map<Integer, Integer> map = getConnectionMap(index);

        Set<Integer> local = new LinkedHashSet<Integer>(global.size());

        for (Integer i : global)
            local.add(map.get(i));

        return local;
    }

    /**
     * Returns the domain - element index pair for the element with the given
     * global index.
     */
    public DomainLocal<Integer, Integer> getDomainLocal(int global) {
        return elementGlobalToLocal.get(global);
    }
}