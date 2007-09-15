package no.uib.cipr.rs.meshgen.triangle;

import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.MeshGenerator;
import no.uib.cipr.rs.meshgen.util.Statistics;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Conversions;
import no.uib.cipr.rs.util.OrderedPair;

/**
 * Generate a mesh description from the output of Jonathan Shewchuk's Triangle
 * program. It does this by treating each output file as a source (through the
 * XxxReader classes) and the mesh as a sink (the embedded XxxCreator inner
 * classes), piping data between them. This layout is done to separate the file
 * formats from the structure building.
 * 
 * Every point is first read, then every triangle. Inside the construction of
 * each triangle, its three sides are constructed and it is determined if we
 * have seen this side before to enable us to identify the neighbours.
 * Construction of the sides are realized by having the triangle constructor
 * produce them in the same manner as the file produces triangles.
 * 
 * A parallel implementation may be possible by making the numOfXxx member an
 * AtomicInteger and all the maps synchronized.
 * 
 * Extension to three-dimensions may be done by implementing Pyramid and Side as
 * the counterparts to Triangle and Edge. The key is to find a good abstraction
 * for identifying a set of shared points with a Triple instead of a Pair.
 * Perhaps connect/LinkCreator can be parameterized on this type?
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class TriangleMesh extends MeshGenerator {
    Point3D[] points;

    Triangle[] triangles;

    Triangle.Edge[] edges;

    Link[] links;

    public TriangleMesh(Configuration config) throws TriExc {

        // get the name of the problem from the configuraion; the name
        // of the problem determines the name of the input files
        String stem = config.getString("ProbName", "mesh");

        // number of iterations that are written in the configuration
        // file; we could write statistics that are fed back to the
        // triangularization to determine if we should do another round
        int refinement = config.getInt("Refinement", 1);

        // read fracture specifications
        Kind.setupFractures(config);

        // read all the points from the solution, generating a map from
        // the Triangle definition of points to our definition as a
        // product (the points end up in our structure through the factory)
        Map<Integer, Integer> map;
        PointParser pointReader = new PointParser(stem, refinement);
        try {
            MeshPointHandler pointGen = this.new MeshPointHandler();
            map = pointReader.readAll(pointGen);
        } finally {
            pointReader.close();
        }

        // read all the triangles from the solution, using the map
        // generated above to map points to our structure. the data
        // structures will setup the connections implicitly through
        // their construction
        TriangleParser triReader = new TriangleParser(stem, refinement);
        try {
            MeshTriangleHandler triGen = this.new MeshTriangleHandler();
            triReader.readAll(map, triGen);
        } finally {
            triReader.close();
        }

        // read fractures and annotate the edges with new transmissibility
        // information
        FractureParser fracReader = new FractureParser(stem, refinement);
        try {
            MeshFractureHandler fracGen = this.new MeshFractureHandler();
            fracReader.readAll(map, fracGen);
        } finally {
            fracReader.close();
        }

        // Build the topology and geometry
        Topology topology = buildTopology();
        Geometry geometry = buildGeometry(topology);

        // To complete the mesh, we need permeability and porosity for each cell
        Rock[] rocks = buildRocks(config, topology);

        // Create the mesh
        mesh = new Mesh(geometry, topology, rocks);
    }

    private Topology buildTopology() {
        Topology topology = new Topology();

        topology.setSizes(points.length, edges.length, triangles.length,
                links.length, 0);

        for (int i = 0; i < edges.length; ++i)
            topology.buildInterfaceTopology(i, edges[i].points());

        for (int i = 0; i < triangles.length; ++i)
            topology.buildElementTopology(i, triangles[i].interfaces());

        for (int i = 0; i < links.length; ++i)
            topology.buildNeighbourConnectionTopology(i, links[i].here(),
                    links[i].there());

        return topology;
    }

    private Geometry buildGeometry(Topology topology) {
        Geometry geometry = new Geometry();

        geometry.setSizes(topology);

        for (int i = 0; i < points.length; ++i)
            geometry.buildPoint(i, points[i]);

        for (int i = 0; i < triangles.length; ++i)
            geometry.buildElement(i, triangles[i].getVolume(), triangles[i]
                    .getCenter());

        for (int i = 0; i < edges.length; ++i)
            geometry.buildInterface(i, edges[i].getArea(),
                    edges[i].getCenter(), edges[i].getNormal());

        for (int i = 0; i < links.length; ++i)
            geometry.setNeighbourMultiplier(i, links[i].multiplier);

        return geometry;
    }

    private Rock[] buildRocks(Configuration config, Topology topology) {
        Rock[] rocks = new Rock[topology.getNumElements()];

        // Default of 25% porosity
        final double phi = config.getDouble("Porosity", 0.25);

        // No compaction, since this is just 2D
        final double cr = 0;

        // Default to 1 Darcy permeability
        double perm = config.getDouble("Permeability",
                Conversions.mDarcyInSquareMeter * 1e+3);
        final Tensor3D K = new Tensor3D(perm);

        // A single rock region named 'rock'
        final String rock = "rock";

        for (int i = 0; i < rocks.length; ++i)
            rocks[i] = new Rock(phi, cr, K, rock);

        return rocks;
    }

    // a triangle always has three sides. the reason for defining this
    // as a constant (we do not expect it to change) is to make it easier
    // to identify locations in the source kind that refer to it.
    private static final int NUM_OF_SIDES = 3;

    /**
     * Function object for adding point definitions to the mesh.
     */
    class MeshPointHandler implements PointHandler {
        int pointsAddedTillNow = 0;

        // allocate space for the number of points needed; they are then
        // read directly into the array.
        public void prepareForPoints(int count) {
            points = new Point3D[count];
        }

        // create a new point object and register it in the structure
        public int onPoint(double x, double y, double z) {
            int index = pointsAddedTillNow++;
            points[index] = new Point3D(x, y, z);
            return index;
        }

        // make sure that we got exactly as many points as we were told
        public void closePoints() {
            assert (pointsAddedTillNow == points.length);
        }
    }

    /**
     * A link is a connection between two interfaces from different triangles,
     * thus bridging two elements. It is implemented as a pair of two interface
     * indices. The pair is ordered since there is supposed to be only one link
     * between each pair of triangles.
     * 
     * This class has no behavior of its own and serves only to provide a
     * Connection interface (in a programming sense, not the geometric) for a
     * pair of points.
     */
    static class Link extends OrderedPair<Integer> {

        /**
         * Construct a link from a pair of interface indices. The interfaces
         * must already exist in the mesh' connections array.
         * 
         * @param aa
         *                Index of the first interface. It is named with two
         *                letters to indicate that it should be the index of an
         *                edge, not a point. You do not have to sort the
         *                indices, the ordered pair will do that for you.
         * @param bb
         *                Index of the second interface.
         */
        public Link(int aa, int bb) {
            super(aa, bb);
        }

        /**
         * Methods to observe the contents of the pair in terms of being a
         * connection. The only effect of these methods is to rename the
         * corresponding methods from x -&gt. here and y -&gt. there.
         */
        public int here() {
            return x();
        }

        public int there() {
            return y();
        }

        // initialize to the default value for normal connections; we
        // update this member if we figure out that there is a fracture
        // along this edge (and the edge is not just a biproduct of the
        // triangularization algorithm).
        double multiplier = 1.;

        /**
         * Transmissibility multiplier of a connection.
         * 
         * @return Transmissibility factor for the connection between two
         *         elements. This factor affects the flow of fluid between two
         *         elements through their interfaces that are connected with
         *         this link. Normally, this factor is 1.0, but if the elements
         *         are separated by a low-permeable fracture it may be lower.
         */
        public double getFluxMultiplier() {
            return this.multiplier;
        }
    }

    /**
     * Function that maps two points to an interface that is part of the
     * boundary, i.e. an interface that is not (yet) connection to another
     * triangle. The integers in the pair are indices into the points array,
     * where as the integer that are the values in the map are indices into the
     * interfaces array. Logically, the signature of the function is:
     * 
     * Point x Point -&gt; Interface
     */
    Map<OrderedPair<Integer>, Integer> boundary;

    /**
     * Function that maps two points to a link between two triangles. The two
     * points identify an edge which is shared. All the edges that has two
     * triangles constitute the interior lines of the mesh. Logically, the
     * function has the signature:
     * 
     * Point x Point -> Edge
     * 
     * whereas the Link class that is returned look like this:
     * 
     * Edge -> Interface x Interface
     * 
     * Note that a pair of points can first be added to the boundary and the
     * promoted to the interior by adding another interface (which is precisely
     * what we do in the connect() method below).
     */
    Map<OrderedPair<Integer>, Link> interior;

    /**
     * Global number of interfaces created. This should really have been a
     * member of the creator function object, but that would require us to send
     * the factory around. It is simply easier to keep it as a member in the
     * grid. However, it is only used upon setup and referred to within the
     * method below.
     */
    int intfCreated = 0;

    /**
     * Create two corners of a triangle, creating a leg between them. This
     * function is called trice (once for each point/leg starting from that
     * point) from the constructor of the triangle. The edge is put in either
     * the boundary (if no neighbours exist) or in the interior (if a neighbour
     * is already present for this edge).
     * 
     * This method connects a triangle to the other triangles through the
     * construction of the edges. 'connect' have therefore a dual meaning: (i)
     * to connect two points, and to (ii) possibly connect two triangles. The
     * latter connections are invisible to the triangle itself, but are created
     * as a biproduct.
     * 
     * @param t
     *                Triangle for which a new edge should be created.
     * @param a
     *                Index for one of the points of the edge. This point should
     *                already exist in the points array.
     * @param b
     *                Index for the other of the points of the edge.
     * @return Index for the interface that belongs to the triangle, connecting
     *         the two points specified. The index will now refer to an object
     *         that has been added to the interfaces array.
     */
    int connect(Triangle t, int a, int b) throws TriExc {
        // create the edge as an inner object of the triangle. the edge
        // is now connected to the triangle (through the implicit pointer
        // to the triangle). we don't know at this point which leg it is,
        // so it is left for the constructor of the triangle to connect
        // the triangle to its edges.
        Triangle.Edge ab = t.new Edge(a, b);

        // allocate another slot in the interface array; all edges are
        // interfaces. assign the newly created object this this slot
        // and note the index, which is what we'll be returning
        int i = intfCreated++;
        edges[i] = ab;

        // create a pair that represents the edge between the two points.
        // this edge may be shared, and we are interested in finding out
        // if the neighbouring triangle has been created. in that case,
        // we connect the two. otherwise, we put the edge in a queue for
        // ending up as the border (if noone else declares themselves
        // our neighbour later).
        // i is our side of the edge and j is the other. logically, the
        // pair represents the edge that separates those two sides and
        // it is thus named ij (even if it is a pair of points not a
        // pair of interfaces).
        OrderedPair<Integer> ij = new OrderedPair<Integer>(a, b);

        // check if this edge is already a part of the interior. in that
        // case the edge connects more than two triangles, which indicate
        // an error in the triangularization
        if (interior.containsKey(ij)) {
            throw TriExc.MORE_THAN_TWO_NEIGHBOURS.create(a, b);
        }

        // try to find the other side of the edge; if another leg was
        // added, then it will be in the boundary queue (waiting for its
        // neighbour -- us).
        Integer j = boundary.get(ij);
        if (j != null) {
            // we now know both sides of the edge, both i and j, and can
            // realize the connection into a concrete link object. verify
            // that we haven't been tricked; that they are actually
            // different triangles.
            Link link = new Link(i, j);
            if (edges[i].element() == edges[j].element()) {
                throw TriExc.OWN_NEIGHBOUR.create(i, j);
            }

            // promote the edge from the boundary to the interior; we
            // have now found both its sides.
            boundary.remove(ij);
            interior.put(ij, link);
        } else {
            // associate this pair of points as a boundary; j is not an
            // edge but rather the void that is outside of the mesh (for
            // the time being).
            boundary.put(ij, new Integer(i));
        }

        // the triangle need to know the index of the edge that was
        // created so that it can retrieve the actual data needed for
        // that object.
        return i;
    }

    /**
     * Reverse lookup for the elements array; given the object we can find the
     * index. This map is filled by the create() method since the triangle
     * constructor is not responsible for registring itself (that is a task for
     * the mesh). IdentityHashMap is a way of adding extra fields to a type in a
     * duck-like manner.
     */
    Map<Triangle, Integer> reverse = new IdentityHashMap<Triangle, Integer>();

    /**
     * Function object for creating a new triangle. Since this is an object, we
     * can send it around without having to drag along the entire structure in
     * our signatures (the outer pointer is hidden).
     */
    class MeshTriangleHandler implements TriangleHandler {
        // keep record of how far we have progressed in the creation
        int trianglesCreatedTillNow = 0;

        // allocate space for the elements. Each element has exactly
        // three edges so we know the number of interfaces as well
        // (however, we do not know how many of these interfaces that
        // are boundaries, so the number of connections are unknown).
        public void prepareForTriangles(int count) {
            triangles = new Triangle[count];

            // for each element we need three sides
            int numOfIntf = count * NUM_OF_SIDES;
            edges = new Triangle.Edge[numOfIntf];
            assert (intfCreated == 0);

            // allocate memory for the internal structures that we use
            // to build connections between the triangles. if there are
            // more than one triangle, then at least one side must shared
            // for the area to be continuous (lower bound) whereas the
            // interior may approach the number of triangles on the limit
            // (upper bound).
            boundary = new Hashtable<OrderedPair<Integer>, Integer>(numOfIntf,
                    0.67f);
            interior = new Hashtable<OrderedPair<Integer>, Link>(numOfIntf,
                    1.00f);
        }

        // create the triangles, booting a virtual avalanche as
        // secondary objects such as interfaces and connections are
        // setup as well.
        public int onTriangle(int a, int b, int c) throws TriExc {
            // initialize the triangle object itself. this will in turn
            // trigger the construction of the edges and setup the
            // connections to the other triangles.
            Triangle t = new Triangle(a, b, c);

            // allocate space for the triangle
            int index = trianglesCreatedTillNow++;
            triangles[index] = t;

            // do statistics on the quality of the mesh by piping the
            // quality measurement of this triangle through a statistical
            // gatherer for the entire mesh.
            qualityStatistics.f(t.quality());
            sizeStatistics.f(t.getVolume());

            // register the object in the reverse lookup map so that it
            // is able to identify itself
            reverse.put(t, index);

            // give the client the index of the triangle, if it is used
            // for any purpose
            return index;
        }

        // finalize the structures; first after all the triangles are
        // created can we determine which edges are connections and which
        // that never made a career past being a simple boundary.
        public void closeTriangles() throws TriExc {
            // check that we got the expected number of objects
            assert (trianglesCreatedTillNow == triangles.length);
            assert (intfCreated == edges.length);

            // an edge must be either a boundary or a connection. the objects in
            // the interior collection maintain information for two sides, while
            // the boundary objects only cover one.
            assert (boundary.size() + 2 * interior.size() == edges.length);

            // create an array of the connections; boundaries don't have
            // any peers with which to generate connections.
            links = new Link[interior.size()];
            links = interior.values().toArray(links);

            // after we have created the connections, we still need the
            // maps that tells us which edges that connect which triangles
            // for the construction of the fractures. instead of disposing
            // of the memory right away we let it linger along with the
            // mesh.
            /*
             * / boundary = null; interior = null; //
             */

            // log statistics of the quality of the generated triangles
            System.err.println(String.format("Quality: %s", qualityStatistics
                    .toString()));
            System.err.println(String.format("Size:    %s", sizeStatistics
                    .toString()));
        }
    }

    /**
     * Color the grid according to which edges are fractures. Fractures must be
     * determined after all edges have been read.
     */
    class MeshFractureHandler implements FractureHandler {
        public void onFracture(int a, int b, Kind kind) throws TriExc {

            // compose a key value that will help us identify the edge
            // in order to find the link that is associated with it.
            OrderedPair<Integer> ij = new OrderedPair<Integer>(a, b);

            // if it is a true fracture, then it should be among the
            // interior (the boundary is artificially created so that is
            // outside of all the fractures).
            if (!kind.isBoundary()) {
                Link link = interior.get(ij);

                // sanity check: verify that we have used this edge as a
                // part of the triangle (i.e. that the two points actually
                // were a side of something and not just random)
                if (link == null) {
                    throw TriExc.FRACTURE_NOT_EDGE.create(ij);
                }

                // fractures are not modelled as their own elements but rather
                // by marking the edges with a multiplier that adjusts the
                // transmissibility through this interface
                link.multiplier = kind.multiplier();

            } else {
                // check that we actually have seen this boundary before
                // if not, the triangulizer has not generated any element
                // that fills the space around it.
                // the second condition of the statement was added to
                // accomodate for internal boundaries, which is generated
                // as part of the partitioning.
                if (boundary.get(ij) == null && interior.get(ij) == null) {
                    throw TriExc.BOUNDARY_NOT_EDGE.create(ij);
                }
            }
        }

        public void prepareForFractures(int count) throws TriExc {
            // these two methods are mandated by the interface but we have
            // no use for them since we are not actually creating any objects
            // for the fractures but rather just altering existing edges
        }

        public void closeFractures() throws TriExc {
            // nothing to do here ...
        }
    }

    // statistical information about the quality of the generated meshes
    Statistics qualityStatistics = new Statistics();

    Statistics sizeStatistics = new Statistics();

    /**
     * Main geometric shape resulting from the triangularization. The cells in a
     * triangular mesh are of this kind. Since the specification of the triangle
     * is specified in terms of the arrays of the mesh, this is an inner class
     * (i.e. a triangle is created for a specific mesh).
     */
    class Triangle {
        /**
         * Indices of the interfaces that make up the sides in the triangle. The
         * interfaces are named after the points that they connect. We always
         * construct them clockwise or counter- clockwise (although we cannot
         * know which of them since they are three-dimensional points).
         */
        int ab;

        int bc;

        int ca;

        /**
         * Create a new triangle from a specification of its corners. Since the
         * triangle is a simplex, it does not matter in which order you specify
         * the three points; they are equally important to the construction of
         * the triangle.
         * 
         * Only the create method should call this constructor, to ensure that
         * all triangles created are registered properly in the elements array.
         * 
         * @param a
         *                First corner of the triangle. This must be an index
         *                into the points array in the enclosing mesh.
         * @param b
         *                Second corner of the triangle.
         * @param c
         *                Third corner of the triangle.
         * @throws TriExc
         *                 If the point specification is illegal.
         */
        Triangle(int a, int b, int c) throws TriExc {
            // connect all (n^2-n)/2 pairs of points to each other. note
            // that all the paths through all the connections forms a
            // circle; each of the points are used exactly once as a
            // source and once as a destination (see getCenter()).
            ab = connect(this, a, b);
            bc = connect(this, b, c);
            ca = connect(this, c, a);
        }

        /**
         * Address this triangle relative to the mesh. This method somehow
         * breaks the encapsulation of the triangle; it is forced to know about
         * the (inner workings of the) mesh. We could factor the method into the
         * mesh and then have the triangle call that method, which is about the
         * same as querying the map.
         * 
         * @return Index into the elements array of the slot which this object
         *         occupy, i.e. elements[t.identify()] == t.
         */
        int identify() {
            return reverse.get(this);
        }

        /**
         * Helper methods to turn each of the components into full-blown edge
         * objects; these are named the same as the member indices, since they
         * conceptually refer to the same thing (in two different ways).
         */
        private Triangle.Edge ab() {
            return edges[ab];
        }

        private Triangle.Edge bc() {
            return edges[bc];
        }

        private Triangle.Edge ca() {
            return edges[ca];
        }

        /**
         * Measure the quality of this triangle; the more equal the sides are
         * the better the triangle is considered to be.
         * 
         * See "Qualitative measures for initial meshes" in International
         * Journal for Numerical Methods in Engineering, 47 (2000), pp. 887-906.
         * 
         * @return Measure of quality for this triangle, as a percentage between
         *         0.0 and 1.0, where higher is better.
         */
        double quality() {
            // length/area of each side, represented by the letter of the
            // starting point for that side
            double a = ab().getArea();
            double b = bc().getArea();
            double c = ca().getArea();

            // ratio between the radius of the smallest inscribed circle
            // (times 2) and the largest circumscribed one.
            double q = (b + c - a) * (c + a - b) * (a + b - c) / (a * b * c);
            return q;
        }

        /**
         * Retrieve the centroid of the triangle by inspecting each of the three
         * corners and creating the middle coordinate from this. Since each edge
         * has a different starting point (no two edges start from the same
         * point), we can get all the corners from the three indices we have
         * stored.
         */
        public Point3D getCenter() {
            Point3D a = ab().point();
            Point3D b = bc().point();
            Point3D c = ca().point();

            double x = (a.x() + b.x() + c.x()) / NUM_OF_SIDES;
            double y = (a.y() + b.y() + c.y()) / NUM_OF_SIDES;
            double z = (a.z() + b.z() + c.z()) / NUM_OF_SIDES;

            return new Point3D(x, y, z);
        }

        /**
         * Volume of a triangle. Since we are only considering two- dimensional
         * objects here, we calculate the area and uses that as the volume in
         * the same way as we use the
         */
        public double getVolume() {
            // get two vectors that both originates from the same point
            // which means that we must turn one of them. we have no use
            // for the third vector, since that is just between the two
            // destination points B and C.
            Vector3D ab = ab().vector();
            Vector3D ca = ca().vector();
            Vector3D ac = ca.mult(-1.);

            // area of a triangle is half the area of the parallelogram
            // that is formed by crossing the two side vectors. the
            // magnitude of the inner product is the area.
            double v = 0.5 * ab.cross(ac).norm2();
            return v;
        }

        /**
         * Get the interface indices for each of the three sides. Due to the way
         * we have organized our structure, this is simply returning our member
         * data.
         */
        public int[] interfaces() {
            return new int[] { ab, bc, ca };
        }

        /**
         * An edge is the structure that separates the area within one triangle
         * from that of another. We construct an edge as a property of a
         * triangle; think of it as the "skin" of the triangle. An edge is
         * therefore created specific to one particular triangle; the Link class
         * encapsulates the relation between the two triangles.
         */
        class Edge {
            /**
             * Indices of the points that are the two terminals of the edge. The
             * edge is the straight line between these two points. One of the
             * points is the "source" and the other is the "destination", but
             * those two concepts are only introduced to define the vector along
             * the edge.
             */
            int a;

            int b;

            /**
             * Construct a new edge object. It is intended that only the
             * connect() method of the mesh should call this constructor in
             * order to correctly setup the connections between triangles.
             * 
             * @param a
             *                First point of the edge. This should be an index
             *                into the points array of the mesh to which our
             *                enclosing triangle is connected.
             * @param b
             *                Second point of the edge.
             */
            Edge(int a, int b) {
                this.a = a;
                this.b = b;
            }

            /**
             * Convenience methods to retrieve the underlaying full-blown point
             * object to which the edge refers. They do this by traversing the
             * hidden pointers to the outer triangle and then to the outer mesh.
             * They are named after the points themselves, which is the same as
             * the member variables since the conseptually refer to the same
             * thing.
             */
            private Point3D a() {
                return points[a];
            }

            private Point3D b() {
                return points[b];
            }

            /**
             * Create a vector that follows the edge. This is used in
             * calculations of the normal vector and the centroid.
             */
            private Vector3D ab() {
                Vector3D ab = new Vector3D(a(), b());
                assert (ab.norm2() != 0.);
                return ab;
            }

            /**
             * Convenience methods that gets us the source point and the vector
             * to get to the destination point. These are the same as the
             * inspection methods defined above, but with names that make their
             * purpose clearer, for instance when referring to the source points
             * of more that one edge but none of the destination points.
             */
            Point3D point() {
                return a();
            }

            Vector3D vector() {
                return ab();
            }

            /**
             * Convenience methods to get the outer and inner object
             * respectively which allows us to distinguish them when they have
             * methods with the same name, for instance getCenter(). We could
             * also have identified these references with special language
             * syntax.
             */
            Triangle element() {
                return Triangle.this;
            }

            private Edge edge() {
                return this;
            }

            /**
             * Observe the points of which this edge is made. This is required
             * by the Interface interface.
             */
            public int[] points() {
                return new int[] { a, b };
            }

            /**
             * Area of the interface. Since we are working with two- dimensional
             * shapes, we use the length of the edge as the amount of
             * "connectionness" between two elements.
             */
            public double getArea() {
                // use the magnitude of the vector between the points
                // to calculate the length of the edge.
                double a = ab().norm2();
                assert (a != 0.);
                return a;
            }

            /**
             * Centroid of the edge
             */
            public Point3D getCenter() {
                // start in one of the points, and walk halfway along the
                // vectors between the points, to get to the center of the
                // edge.
                Point3D c = a().apply(ab().mult(0.5));
                return c;
            }

            /**
             * Normal vector of the edge; direction in which the flux goes into
             * or out of the triangle.
             */
            public Vector3D getNormal() {
                // in two dimensions we can easily find the normal
                // vector by considering the diagonal in the box which
                // is turned 90 degrees. we ignore the third dimension.
                // (dimensionality is hardcoded into the Triangle and
                // Edge class anyway, so this kind must be revised if
                // we want to extend it to true 3D).
                // however, we don't know yet that this is the normal
                // vector that points the right way!
                Vector3D ab = ab();
                Vector3D n = new Vector3D(-ab.y(), ab.x(), 0.);
                assert (n.norm2() != 0.);

                // find the centroid for the triangle and the middle
                // point of the edge. the normal vector that points out
                // of the triangle (in constrast to inwards) must point
                // the same way as this one, as the triangle is convex.
                Point3D c = element().getCenter();
                Point3D d = edge().getCenter();
                Vector3D cd = new Vector3D(c, d);

                // project the normal vector onto the vector from the
                // center towards the edge; the sign is positive if they
                // point in the same direction and negative if they
                // point opposite of eachother.
                double s = Math.signum(n.dot(cd));
                assert (s != 0.);

                // by multiplying with the sign, we ensure us that the
                // normal vector that we return always points out of the
                // mass of the triangle, never towards it.
                n = n.mult(s);

                // normal vectors must always have length 1 (it is not
                // only sufficient that it is orthogonal to the edge);
                // scale it appropriately.
                n = n.mult(1. / n.norm2());
                return n;
            }
        }
    }

}