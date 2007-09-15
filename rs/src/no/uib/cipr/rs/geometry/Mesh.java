package no.uib.cipr.rs.geometry;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Pair;
import no.uib.cipr.rs.util.Tolerances;

/**
 * A computational mesh. Consists of elements (3D), interfaces (2D) and points
 * (1D). Connections can be neighbouring (between adjacent interfaces), or
 * non-neighbouring (between any pair of elements). The position of fluid
 * sources is also stored in the mesh
 */
public final class Mesh implements Serializable {

    private static final long serialVersionUID = -1844028475053415156L;

    /**
     * Cornerpoints of the mesh
     */
    public final CornerPoint[] points;

    /**
     * Interfaces of the mesh
     */
    public final Interface[] interfaces;

    /**
     * Elements of the mesh
     */
    public final Element[] elements;

    /**
     * Neighbour connections between adjacent elements
     */
    public final NeighbourConnection[] neighbourConnections;

    /**
     * Non-neighbour connections
     */
    public final Connection[] nonNeighbourConnections;

    /**
     * Collection of both the neighbour and non-neighbour connections
     */
    public final Connection[] connections;

    /**
     * The location of the sources in the mesh
     */
    public Map<String, SourceLocation> sources;

    public Mesh(Geometry geometry, Topology topology, Rock[] rocks) {
        points = buildCornerPoints(topology, geometry);

        interfaces = buildInterfaces(topology, geometry);

        neighbourConnections = buildNeighbourConnections(topology, geometry);

        nonNeighbourConnections = buildNonNeighbourConnections(topology,
                geometry);

        connections = buildConnections(topology);

        elements = buildElements(topology, geometry, rocks);

        testMesh();
    }

    private CornerPoint[] buildCornerPoints(Topology topology, Geometry geometry) {
        CornerPoint[] pArray = new CornerPoint[topology.getNumPoints()];

        for (int i : topology.getPointIndices()) {
            Point3D point = geometry.getPoint(i);

            pArray[i] = new CornerPoint(i, point, topology
                    .getPointInterfaces(i), topology.getPointElements(i));
        }

        return pArray;
    }

    private Interface[] buildInterfaces(Topology topology, Geometry geometry) {
        Interface[] iArray = new Interface[topology.getNumInterfaces()];

        for (int i : topology.getInterfaceIndices()) {
            Vector3D n = geometry.getNormal(i);

            double area = geometry.getInterfaceArea(i);

            Point3D center = geometry.getInterfaceCenter(i);

            boolean boundary = topology.isBoundaryInterface(i);

            int[] ip = topology.getInterfacePoints(i);
            int ie = topology.getInterfaceElement(i);
            int ic = boundary ? -1 : topology.getInterfaceConnection(i);

            iArray[i] = new Interface(i, n, area, center, boundary, ip, ie, ic);
        }

        return iArray;
    }

    private Element[] buildElements(Topology topology, Geometry geometry,
            Rock[] rocks) {
        Element[] eArray = new Element[topology.getNumElements()];

        for (int i : topology.getElementIndices()) {
            Rock rock = rocks[i];

            Point3D center = geometry.getElementCenter(i);
            double volume = geometry.getElementVolume(i);

            int[] ep = topology.getElementPoints(i);
            int[] ei = topology.getElementInterfaces(i);
            int[] ec = topology.getElementConnection(i);

            eArray[i] = new Element(i, center, volume, rock, ep, ei, ec);
        }

        return eArray;
    }

    private NeighbourConnection[] buildNeighbourConnections(Topology topology,
            Geometry geometry) {
        NeighbourConnection[] cArray = new NeighbourConnection[topology
                .getNumNeighbourConnections()];

        for (int i : topology.getNeighbourConnectionIndices()) {
            double m = geometry.getNeighbourConnectionFluxMultiplier(i);

            int hereInterface = topology.getNeighbourConnectionHere(i);
            int thereInterface = topology.getNeighbourConnectionThere(i);

            int hereElement = topology.getInterfaceElement(hereInterface);
            int thereElement = topology.getInterfaceElement(thereInterface);

            cArray[i] = new NeighbourConnection(i, m, hereElement,
                    thereElement, hereInterface, thereInterface);
        }

        return cArray;
    }

    private Connection[] buildNonNeighbourConnections(Topology topology,
            Geometry geometry) {
        Connection[] cArray = new Connection[topology
                .getNumNonNeighbourConnections()];
        int offset = topology.getNumNeighbourConnections();

        for (int i : topology.getNonNeighbourConnectionIndices()) {
            double m = geometry.getNonNeighbourConnectionFluxMultiplier(i);

            int here = topology.getNonNeighbourConnectionHere(i);
            int there = topology.getNonNeighbourConnectionThere(i);

            cArray[i] = new Connection(i + offset, m, here, there);
        }

        return cArray;
    }

    private Connection[] buildConnections(Topology topology) {
        Connection[] cArray = new Connection[topology.getNumConnections()];

        int i = 0;

        // neighbour connections
        for (Connection c : neighbourConnections)
            cArray[i++] = c;

        // non-neighbour connections
        for (Connection c : nonNeighbourConnections)
            cArray[i++] = c;

        return cArray;
    }

    public void setSources(SourceLocation[] sourceLocations) {
        sources = new HashMap<String, SourceLocation>();

        for (SourceLocation q : sourceLocations)
            sources.put(q.name, q);
    }

    /**
     * Iterator over the interfaces of the cornerpoint
     */
    public List<Interface> interfaces(CornerPoint p) {
        return new SubsetList<Interface>(p.interfaces, interfaces);
    }

    /**
     * Iterator over the elements of the cornerpoint
     */
    public List<Element> elements(CornerPoint p) {
        return new SubsetList<Element>(p.elements, elements);
    }

    /**
     * Iterator over the cornerpoints of the interface
     */
    public List<CornerPoint> points(Interface intf) {
        return new SubsetList<CornerPoint>(intf.points, points);
    }

    /**
     * Element associated with the interface
     */
    public Element element(Interface intf) {
        return elements[intf.element];
    }

    /**
     * Neighbour connection associated with the interface
     */
    public NeighbourConnection connection(Interface intf) {
        return intf.boundary ? null
                : neighbourConnections[intf.neighbourConnection];
    }

    /**
     * Iterator over the cornerpoints of the element
     */
    public List<CornerPoint> points(Element el) {
        return new SubsetList<CornerPoint>(el.points, points);
    }

    /**
     * Iterator over the interfaces of the element
     */
    public List<Interface> interfaces(Element el) {
        return new SubsetList<Interface>(el.interfaces, interfaces);
    }

    /**
     * Iterator over the associated non-neighbour connections of the element
     */
    public List<Connection> nonNeighbourConnections(Element el) {
        return new SubsetList<Connection>(el.associatedNonNeighbourConnections,
                nonNeighbourConnections);
    }

    /**
     * Element on the here side of a connection
     */
    public Element here(Connection c) {
        return elements[c.hereElement];
    }

    /**
     * Element on the there side of a connection
     */
    public Element there(Connection c) {
        return elements[c.thereElement];
    }

    /**
     * Interface on the here side of a neighbouring connection
     */
    public Interface hereInterface(NeighbourConnection c) {
        return interfaces[c.hereInterface];
    }

    /**
     * Interface on the there side of a neighbouring connection
     */
    public Interface thereInterface(NeighbourConnection c) {
        return interfaces[c.thereInterface];
    }

    /**
     * Element coupled by the given transmissibility
     */
    public Element element(Transmissibility t) {
        return elements[t.element];
    }

    /**
     * Iterator over the elements of a named source
     */
    public List<Element> elements(String q) {
        return new SubsetList<Element>(sources.get(q).elements, elements);
    }

    /**
     * The names of the mesh sources
     */
    public Set<String> sources() {
        return sources.keySet();
    }

    /**
     * List of elements in a subset
     */
    private static class SubsetList<E> extends AbstractList<E> {

        private final int[] indices;

        private final E[] set;

        public SubsetList(int[] indices, E[] set) {
            this.indices = indices;
            this.set = set;
        }

        @Override
        public E get(int index) {
            return set[indices[index]];
        }

        @Override
        public int size() {
            return indices.length;
        }

    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(points.length).append(" points\n");
        str.append(interfaces.length).append(" interfaces\n");
        str.append(neighbourConnections.length).append(
                " neighbour connections\n");
        str.append(nonNeighbourConnections.length).append(
                " non-neighbour connections\n");
        str.append(connections.length).append(" connections\n");
        str.append(elements.length).append(" elements\n");

        return str.toString();
    }

    /**
     * Returns a list of all the points.
     */
    public List<CornerPoint> points() {
        return Arrays.asList(points);
    }

    /**
     * Returns a list of all the interfaces.
     */
    public List<Interface> interfaces() {
        return Arrays.asList(interfaces);
    }

    /**
     * Returns a list of all the elements.
     */
    public List<Element> elements() {
        return Arrays.asList(elements);
    }

    /**
     * Returns a list of all the neighbour connections.
     */
    public List<NeighbourConnection> neighbourConnections() {
        return Arrays.asList(neighbourConnections);
    }

    /**
     * Returns a list of all the non-neighbour connections.
     */
    public List<Connection> nonNeighbourConnections() {
        return Arrays.asList(nonNeighbourConnections);
    }

    /**
     * Returns a list of all connections of this mesh including non-neighbour
     * connections.
     */
    public List<Connection> connections() {
        return Arrays.asList(connections);
    }

    /**
     * Runs all the tests
     */
    private void testMesh() {
        // Topology tests
        testPointElement();
        testPointInterface();
        testInterfacePoint();
        testInterfaceElementExists();
        testInterfaceElementComplete();
        testInterfaceNeighbourConnection();
        testElementPoint();
        testElementInterface();
        testNeighbourConnectionInterface();
        testNeighbourConnectionInterfaceBoundary();
        testNeighbourConnectionHereThere();
        testConnectionHereThereElements();

        // Geometry tests
        // testInterfaceCenter();
        // testElementCenter();
        testInterfaceUnitNormals();
        testNeighbouringInterfaceAreas();
        testNeighbouringInterfaceNormals();
        testOutwardNormals();
        // testElementOverlap();
    }

    // --------------- Point tests ---------------

    /**
     * Testing point-element consistency.
     */
    private void testPointElement() {
        for (CornerPoint p : points())
            for (Element e : elements(p)) {
                boolean consistent = false;

                // find occurence of this point in element's point list
                for (CornerPoint p2 : points(e))
                    if (p == p2)
                        if (!consistent)
                            consistent = true;
                        else
                            throw new AssertionError(
                                    "Point occurs more than once");

                if (!consistent)
                    throw new AssertionError("Point-Element inconsistency");
            }
    }

    /**
     * Testing point-interface consistency.
     */
    private void testPointInterface() {
        for (CornerPoint p : points())
            for (Interface i : interfaces(p)) {
                boolean consistent = false;

                for (CornerPoint p2 : points(i))
                    if (p == p2)
                        if (!consistent)
                            consistent = true;
                        else
                            throw new AssertionError(
                                    "Point occurs more than once");

                if (!consistent)
                    throw new AssertionError("Point-Interface inconsistency");
            }
    }

    /**
     * Test that all points of interface have this interface.
     */
    private void testInterfacePoint() {
        for (Interface i : interfaces())
            for (CornerPoint p : points(i)) {
                boolean consistent = false;

                for (Interface i2 : interfaces(p))
                    if (i == i2)
                        if (!consistent)
                            consistent = true;
                        else
                            throw new AssertionError(
                                    "Interface occurs more than once");

                if (!consistent)
                    throw new AssertionError("Interface-Point inconsistency");
            }
    }

    /**
     * Test interface center point.
     * 
     * TODO Make more robust
     */
    private void testInterfaceCenter() {
        for (Interface i : interfaces()) {
            Point3D computed = new Point3D(0, 0, 0);
            int num = 0;
            for (CornerPoint p : points(i)) {
                computed = computed.plus(p.coordinate);
                num++;
            }
            if (num == 0)
                throw new AssertionError("We need more than 0 points");

            computed = computed.scale(1. / num);

            Point3D center = i.center;

            if (!equals(computed.x(), center.x()))
                throw new AssertionError(
                        "Interface center does not match computed center (x)");
            if (!equals(computed.y(), center.y()))
                throw new AssertionError(
                        "Interface center does not match computed center (y)");
            if (!equals(computed.z(), center.z()))
                throw new AssertionError(
                        "Interface center does not match computed center (z)");
        }
    }

    /**
     * Test element center point
     * 
     * TODO Make more robust
     */
    private void testElementCenter() {
        for (Element e : elements()) {
            Point3D computed = new Point3D(0, 0, 0);
            int num = 0;
            for (CornerPoint p : points(e)) {
                computed = computed.plus(p.coordinate);
                num++;
            }
            if (num == 0)
                throw new AssertionError("We need more than 0 points");

            computed = computed.scale(1. / num);

            Point3D center = e.center;

            if (!equals(computed.x(), center.x()))
                throw new AssertionError(
                        "Element center does not match computed center (x)");
            if (!equals(computed.y(), center.y()))
                throw new AssertionError(
                        "Element center does not match computed center (y)");
            if (!equals(computed.z(), center.z()))
                throw new AssertionError(
                        "Element center does not match computed center (z)");
        }
    }

    /**
     * Test that all interface elements exist in mesh.
     */
    private void testInterfaceElementExists() {
        Set<Element> eSet = new HashSet<Element>(elements());

        for (Interface i : interfaces()) {
            Element e = element(i);

            // check that interface element exists
            if (!eSet.contains(e))
                throw new AssertionError("Interface element " + e.index
                        + " does not exist");
        }
    }

    /**
     * Test that interface element set is complete.
     */
    private void testInterfaceElementComplete() {
        Set<Element> eSet = new HashSet<Element>(elements());

        Element previous = null;

        for (Interface i : interfaces()) {
            Element e = element(i);

            if (e == previous)
                continue;

            // check that interface element exists. if this test fails, it may
            // be because the interfaces for a certain element is not written
            // in sequence. (if it was desirable to allow them not to be, then
            // we should use contains() instead of remove() below.
            if (!eSet.remove(e))
                throw new AssertionError(String.format(
                        "Interface %d's element %d does not exist in mesh",
                        i.index, e.index));

            previous = e;
        }

        if (!eSet.isEmpty())
            throw new AssertionError("Interface element set is not complete");
    }

    /**
     * Test interface-neighbour-connection consistency
     */
    private void testInterfaceNeighbourConnection() {
        for (Interface i : interfaces()) {

            NeighbourConnection c = connection(i);

            if (i.boundary) {
                if (c != null)
                    throw new AssertionError(
                            "Interface-Connection inconsistency");
            } else {
                Interface here = hereInterface(c);
                Interface there = thereInterface(c);

                boolean consistent = (i == here) ^ (i == there);

                if (!consistent)
                    throw new AssertionError(
                            "Interface-Connection inconsistency");
            }
        }
    }

    /**
     * Test interface normals
     */
    private void testInterfaceUnitNormals() {
        for (Interface i : interfaces()) {
            Vector3D n = i.normal;
            double diff = Math.abs(1.0 - n.norm2());
            if (diff > Tolerances.largeEps)
                throw new AssertionError("Normal should have unit length");
        }
    }

    /**
     * Test consistent neighbouring interface areas
     */
    private void testNeighbouringInterfaceAreas() {
        for (NeighbourConnection c : neighbourConnections()) {
            Interface is = hereInterface(c);
            Interface js = thereInterface(c);

            if (!equals(is.area, js.area))
                throw new AssertionError(
                        "Neighbouring interfaces should have same area");
        }
    }

    /**
     * Test oppositely pointing normals of neighbouring interfaces
     */
    private void testNeighbouringInterfaceNormals() {
        for (NeighbourConnection c : neighbourConnections()) {
            Interface is = hereInterface(c);
            Interface js = thereInterface(c);

            double dot = is.normal.dot(js.normal);

            if (dot >= 0)
                throw new AssertionError(
                        "Neighbouring interfaces must have opposite normals");
        }
    }

    /**
     * Test outward normals
     */
    private void testOutwardNormals() {
        for (Interface i : interfaces()) {
            Element e = element(i);

            Point3D pe = e.center;

            Vector3D n = i.normal;

            Point3D pi = i.center;

            Vector3D ei = new Vector3D(pe, pi);

            Vector3D en = ei.plus(n.mult(Tolerances.largeEps));

            if (en.norm2() <= ei.norm2())
                throw new AssertionError("Normal does not point outwards");
        }
    }

    // --------------- Element tests ---------------

    /**
     * Test element-point consistency
     */
    private void testElementPoint() {
        for (Element e : elements())
            for (CornerPoint p : points(e)) {
                boolean consistent = false;

                for (Element e2 : elements(p))
                    if (e2 == e)
                        if (!consistent)
                            consistent = true;
                        else
                            throw new AssertionError(
                                    "Element occurs more than once");

                if (!consistent)
                    throw new AssertionError("Element-Point inconsistency");
            }
    }

    /**
     * Test element-interface consistency.
     */
    private void testElementInterface() {
        for (Element e : elements())
            for (Interface i : interfaces(e))
                if (e != element(i))
                    throw new AssertionError("Element-Interface inconsistency");
    }

    /**
     * A point is regarded as being "inside" an interface if it is on the same
     * side of the interface as the cell center. This is found by seeing if the
     * vector from the point to the center of the interface (could be any point
     * really, but we have the center handy) is the same direction as the normal
     * vector going away from the interface (all normal vectors are pointing
     * outwards). Note that the point is not inside if it is on the interface,
     * which is why we only test the dot-product for lesser-than, not
     * less-or-equal.
     */
    private final boolean pointInsideInterface(CornerPoint pt, Interface intf) {
        Point3D point = pt.coordinate;
        Point3D center = intf.center;
        Vector3D outwards = intf.normal;
        Vector3D direction = new Vector3D(point, center);
        boolean inside = (direction.dot(outwards) > 0.);
        return inside;
    }

    /**
     * A point is inside an element, if it is inside all the interfaces of the
     * element. This assumes that the element is convex.
     */
    private final boolean pointInsideElement(CornerPoint pt, Element elem) {
        boolean inside = true;
        for (Interface intf : interfaces(elem))
            inside &= pointInsideInterface(pt, intf);
        return inside;
    }

    /**
     * If the element is convex, then the corner points are the extremes of the
     * volume, and we can test if the element is inside one of the others by
     * checking of any of the points are inside the other element. Note that
     * this relation is not symmetric; elem1 inside elem2 does not imply (as
     * through this implementation) that elem2 inside elem1.
     */
    private final boolean elementInsideElement(Element elem1, Element elem2) {
        boolean inside = false;
        for (CornerPoint pt : points(elem1))
            inside |= pointInsideElement(pt, elem2);
        return inside;
    }

    /**
     * Test if there is any element that is inside of another. Due to the way we
     * do this test, the relation is not symmetric (think of a tetrahedra where
     * the apex is piercing through one of the sides of a hexahedra), so we have
     * to do test both the lower and the upper triangle of combinations.
     * 
     * This test will not immediately detect cases where one element pierces
     * through its neighbour, but it will find the neighbour element at which
     * the corner point finally end (with the exception of the rare corner case
     * where an element pierces through another and then the apex make out the
     * border).
     */
    private final void testElementOverlap() {
        int total = elements.length * elements.length;
        double reported = Double.NaN;
        Set<Integer> problematic = new HashSet<Integer>();
        List<Pair<Integer, Integer>> overlap = new ArrayList<Pair<Integer, Integer>>();
        for (int i = 0; i < elements.length; i++) {
            for (int j = 0 /* i+1 */; j < elements.length; j++) {
                double progress = Math.floor((i * elements.length + j)
                        / (double) total * 100.);
                if (progress != reported) {
                    System.out.printf(
                            "Overlap    : percent = %2.0f%%, elements = %d\r",
                            progress, problematic.size());
                    reported = progress;
                }

                if (i != j) {
                    Element elem1 = this.elements[i];
                    Element elem2 = this.elements[j];
                    if (elementInsideElement(elem1, elem2)) {
                        Integer index1 = new Integer(i);
                        Integer index2 = new Integer(j);
                        problematic.add(index1);
                        problematic.add(index2);
                        overlap.add(new Pair<Integer, Integer>(index1, index2));
                        /*
                         * throw new AssertionError(String.format( "Element %d
                         * overlaps with element %d", i, j));
                         */
                    }
                }
            }
        }
        // if i overlaps with j, then j overlaps with i (ideally)
        System.out.printf("Overlap    : elements = %8d, connections = %8d%n",
                problematic.size(), overlap.size() / 2);

        // dump indices of the problematic cells to file
        if (problematic.size() > 0) {
            try {
                PrintWriter pw = new PrintWriter("problem.dat");
                try {
                    for (Integer i : problematic)
                        pw.printf("%8d%n", i.intValue());
                } finally {
                    pw.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // --------------- Connection tests ---------------

    /**
     * Test neighbour connection-interface consistency.
     */
    private void testNeighbourConnectionInterface() {
        Interface i = null;

        for (NeighbourConnection c : neighbourConnections()) {
            i = hereInterface(c);
            if (c != connection(i))
                throw new AssertionError("Connection-Interface inconsistency");

            i = thereInterface(c);
            if (c != connection(i))
                throw new AssertionError("Connection-Interface inconsistency");
        }
    }

    /**
     * Test neighbour connection-interface boundary
     */
    private void testNeighbourConnectionInterfaceBoundary() {
        for (NeighbourConnection c : neighbourConnections()) {
            if (hereInterface(c).boundary)
                throw new AssertionError(
                        "Interface 'here' should not be boundary");

            if (thereInterface(c).boundary)
                throw new AssertionError(
                        "Interface 'there' should not be boundary");
        }
    }

    /**
     * Test distinct neighbour connection here-there interfaces
     */
    private void testNeighbourConnectionHereThere() {
        for (NeighbourConnection c : neighbourConnections())
            if (hereInterface(c) == thereInterface(c))
                throw new AssertionError("Connection here-there interfaces");
    }

    /**
     * Test distinct connection here-there elements
     */
    private void testConnectionHereThereElements() {
        for (Connection c : connections())
            if (c.hereElement == c.thereElement)
                throw new AssertionError("Connection here-there elements");
    }

    private boolean equals(double x, double y) {
        return Math.abs(x - y) < Tolerances.smallEps;
    }

}