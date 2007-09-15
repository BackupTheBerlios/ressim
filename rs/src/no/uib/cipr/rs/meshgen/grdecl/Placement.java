package no.uib.cipr.rs.meshgen.grdecl;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.structured.HexCell;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.structured.HexCell.QuadFace;
import no.uib.cipr.rs.util.Tolerances;
import no.uib.cipr.rs.util.Uniquifier;

class Placement {
    // ---------------------- Coordinate line members --------------------------
    // starting point for this coordinate line (in the bottom of the grid)
    Point3D[/* coord */] anchor;

    // beam on which all the points for this coordinate line lays, from the
    // point specified above
    Vector3D[/* coord */] beam;

    // points along the coordinate line; the outer array represents the
    // coordinate line, the inner arrays are the hinges per line
    double[/* coord */][/* hinge */] hinge;

    // accumulated number of hinges before this coordinate line, i.e. the sum
    // of the lengths for all preceeding coordinate lines. to get the global
    // index of a point, the starting point for the coordinate is added to the
    // relative index within the coordinate line. (the last index contains the
    // total number of points in the grid)
    int[/* coord */] start;

    // number of corners that are filled for each coordinate line
    int[/* coord */] counter;

    // --------------------------- Block members -------------------------------
    // index into the hinge at the respective coordinate line for each corner
    // these must be substituted afterwards. the outer array contains an
    // element per corner, whereas the inner array are per block (same as the
    // other members in this section)
    int[/* corner */][/* block */] corner;

    // true if this cell should be used in the simulator, false if it is
    // specified in the geometry only
    boolean[/* block */] active;

    // array containing the points to which the corner array is referring
    Point3D[] points;

    // --------------------------- Grid members --------------------------------
    private Grid grid;

    // we believe is the rule specified by the Eclipse Reference Manual
    // is a right-hand one, but we have seen grids that follows the left-hand
    // side rule; thus we attempt to detect the rule of the grid
    // automatically from the data read
    private Rule rule;

    // ----------------------- Construction methods ----------------------------
    Placement(Grid grid) {
        // we need the format for traversing the grid later on
        this.grid = grid;

        // allocate memory for each of the arrays since the navigator will
        // tell us the format needed for the grid it operates upon
        int numOfPillars = grid.format.numOfPillars();
        anchor = new Point3D[numOfPillars];
        beam = new Vector3D[numOfPillars];
        hinge = new double[numOfPillars][];

        // prepare the corner elements to receive indices. the counter array is
        // no longer needed after we are through reading the data
        counter = new int[numOfPillars];

        corner = new int[Corner.values().length][];
        for (Corner c : Corner.values()) {
            corner[c.ordinal()] = new int[grid.format.numOfBlocks()];
        }
    }

    void setCoord(PillarNav p, Point3D start, Point3D end) {
        // let the navigator tell us where to put it
        int ij = p.pillar();

        // Eclipse uses the convention that vertical lines have the same start
        // and end point; the beam vector then becomes (0,0,1) and the point is
        // only used to position the beam in the bottom plane (x and y).
        if (start.equals(end)) {
            end = new Point3D(end.x(), end.y(), end.z() + 1.);
        }

        // calculate a point + a vector (= a line) that represents this pillar
        anchor[ij] = start;
        beam[ij] = new Vector3D(start, end);

        // make room for the hinges that will be located on this coordinate line
        hinge[ij] = new double[p.maxNumOfHinges()];
    }

    void setDepth(CornerNav c, double depth) {
        // get the coordinate line index
        int ij = c.pillar();

        // enter the depth itself into the data structure for the pillar
        hinge[ij][counter[ij]] = depth;

        // note for the appropriate corner to which point in refers
        corner[c.corner()][c.block()] = counter[ij];

        // register that we have gotten yet another point; increment to prepare
        // for the next point to arrive in the next slot
        counter[ij]++;
    }

    void setActive(boolean[] active) {
        this.active = active;
    }

    // -------------------------- Observation methods --------------------------
    void complete(Geometry geo, Topology topo) {
        // rule (of cartesian coordinate system)
        rule = detectRule();
        System.out.format("Rule: %s", rule);

        // points
        completePoints();
        buildPoints(geo, topo);

        // elements, interfaces and connections
        completeElements(geo, topo);
        geo.setNumConnections(0, 0);
        topo.setNumConnections(0, 0);
    }

    // -------------------------- Rule methods --------------------------
    /**
     * Figure out whether the coordinate lines have been specified using a
     * left-hand or right-hand rule coordinate system. Since the order of the
     * lines are given in the file format, the value of the coordinates reveal
     * the implicit rule.
     */
    // TODO: Check if the grid is specified sideways. This could probably be
    // done by checking which unit vector (ex, ey, ez) that has the greatest
    // absolute dot product with the z vector below and then compare the sign
    // of that product afterward into an rule
    Rule detectRule() {
        // get the corners along the first two axis in the upper back left cell
        // these corners must always be present
        Vector3D x = new Vector3D(anchor[0], anchor[1]);
        Vector3D y = new Vector3D(anchor[0], anchor[grid.format.ni + 1]);

        // according to the way these points are specified, in which direction
        // does the z-axis grow?
        Vector3D z = x.cross(y);

        // compare this against a right-hand rule coordinate system, and set
        // the rule accordingly
        Vector3D ez = new Vector3D(0, 0, 1.);
        Rule r = (z.dot(ez) > 0.) ? Rule.RightHand : Rule.LeftHand;
        return r;
    }

    // ---------------------------- Points methods -----------------------------
    /**
     * Convert the coordinate-line based structure read from file into a true
     * corner point format that are block based. We have some guarantees about
     * the points, though: If two points are on the same coordinate line, then a
     * higher index means that it is higher up on the line.
     */
    void completePoints() {
        normalizeHinges();
        countPoints();
        realizePoints();
        makePointIndicesAbsolute();
        clearMemory();
        sanityCheck();
    }

    /**
     * Convert corner point "depth" into length along the pillar. By having the
     * depth written as a vector multiplum instead of an absolute coordinate, we
     * can easily figure out the other coordinates for this point as well.
     */
    void normalizeHinges() {
        // loop through every coordinate line
        for (PillarNav p : grid.pillars()) {
            // identify the part of the grid with which we are working
            int ij = p.pillar();

            // sort and compress all hinge coordinates along this pillar
            Uniquifier u = new Uniquifier(hinge[ij], Tolerances.smallEps, true);
            hinge[ij] = u.get();

            // replace indices for the blocks that referred to the old pillar
            for (HingeNav h : grid.hinges(p)) {
                int c = h.corner();
                int b = h.block();
                corner[c][b] = u.remap(corner[c][b]);
            }

            // what should the depth be if we should follow the line exactly; we
            // use this to adjust the other way -- from depths back into
            // relative vectors (which is then used to generate points).
            double startDepth = anchor[ij].z();
            double length = beam[ij].z();

            for (int k = 0; k < hinge[ij].length; ++k) {
                // subtract the starting depth of the coordinate line, to get
                // the relative depth
                hinge[ij][k] -= startDepth;

                // convert relative depth into vector length
                hinge[ij][k] /= length;
            }
        }
    }

    /**
     * Tally the number of points and make room for their realization
     */
    void countPoints() {
        // we haven't seen any points yet
        int total = 0;

        // allocate a starting index for each coordinate line
        start = new int[hinge.length];

        // add the number of points of each pillar encountered this far; when
        // we write points from each pillar afterwards, then this start index
        // will be the offset to this pillar in the large point array
        for (int ij = 0; ij < start.length; ij++) {
            start[ij] = total;
            total += hinge[ij].length;
        }

        // return the grand total of points in the form of an allocated array
        points = new Point3D[total];
    }

    /**
     * Create a point object for a corner point in the mesh given by its
     * structured coordinates.
     * 
     * @param ij
     *                Index of the pillar on which the point belongs. The
     *                parameter is so named because this index encapsulates both
     *                the structured i- and j- coordinate of the pillar. This
     *                parameter should be gotten from the .pillar() method of a
     *                navigator.
     * @param k
     *                Index of the point along the pillar. (Note that this is
     *                not the depth).
     * @return Object representation of this point
     */
    Point3D realize(int ij, int k) {
        // start out at the anchor, walk up along the pillar as far as we are
        // told by the relative length
        return anchor[ij].apply(beam[ij].mult(hinge[ij][k]));
    }

    /**
     * Generate an object for each point in the grid. We require the total to be
     * specified to avoid having to use a dynamic vector.
     */
    void realizePoints() {
        // we haven't encountered any points yet
        int counter = 0;

        // for each pillar, and then for each hinge on that pillar, realize a
        // point and put at the next slot in the global point array
        for (PillarNav p : grid.pillars()) {
            int ij = p.pillar();
            for (int k = 0; k < hinge[ij].length; k++) {
                points[counter] = realize(ij, k);
                counter++;
            }
        }
        assert counter == points.length;
    }

    /**
     * Rewrite indices for the corner points so they don't refer to a local
     * point along the appropriate hinge, but rather an index into the global
     * point array. (We don't gain or loose any hinge information by this
     * operation, just collect all the points into one array instead of having
     * them scattered around as information in the coordinate line members).
     * 
     */
    void makePointIndicesAbsolute() {
        // add the starting offset for each pillar to every hinge point on that
        // pillar, converting the points indices from local indices to global
        for (PillarNav p : grid.pillars()) {
            int offset = start[p.pillar()];

            for (HingeNav hn : grid.hinges(p)) {
                corner[hn.corner()][hn.block()] += offset;
            }
        }
    }

    /**
     * After we have generated point objects and converted indices into pointing
     * to these, we don't need to hold on to any of the coordinate line members
     * anymore; all the information we need is more available through the corner
     * array and the points array!
     */
    void clearMemory() {
        // we don't need these arrays anymore; all the information we need is
        // now encoded in the corner array for each block!
        anchor = null;
        beam = null;
        hinge = null;
        start = null;

        // we don't need this member anymore neither; dispose off it here so
        // that it will be catched in the same garbage collection cycle as the
        // other
        counter = null;
    }

    // don't bother reporting more than this number of errors because it will
    // only flood the console
    private final static int MAX_ERRORS = 25;

    /**
     * Make sure that all points that are generated conforms to a hexahedra grid
     */
    void sanityCheck() {
        // total number of errors
        int count = 0;

        // buffer containing the error messages that we have encountered; we
        // only write a message if there is any error, and then we want to
        // include the block number in that message
        StringBuilder buf = new StringBuilder();

        // local array containing just the points for this one block (in every
        // iteration in the loop below)
        Point3D[] p = new Point3D[Corner.values().length];

        // test each block seperately
        for (BlockNav b : grid.blocks()) {
            // no errors encountered yet for this block
            buf.setLength(0);

            // get the points for each of the corners
            for (Corner c : Corner.values()) {
                p[c.ordinal()] = points[corner[c.ordinal()][b.block()]];
            }

            // consistency checks; test the starting against the ending point
            // for each of the twelve (four corners (of a quad-face) in each of
            // the three dimensions) ridges found in the cube.
            // TODO: rewrite this test as either table-based or as looping
            // through some form of enumeration.
            if ((p[0].x() - p[1].x()) > 0.)
                buf
                        .append(String.format("%n\tx: p0 = %s, p1 = %s", p[0],
                                p[1]));
            if ((p[2].x() - p[3].x()) > 0)
                buf
                        .append(String.format("%n\tx: p2 = %s, p3 = %s", p[2],
                                p[3]));
            if ((p[4].x() - p[5].x()) > 0)
                buf
                        .append(String.format("%n\tx: p4 = %s, p5 = %s", p[4],
                                p[5]));
            if ((p[6].x() - p[7].x()) > 0)
                buf
                        .append(String.format("%n\tx: p6 = %s, p7 = %s", p[6],
                                p[7]));

            if ((p[0].y() - p[2].y()) * rule.y() > 0)
                buf
                        .append(String.format("%n\ty: p0 = %s, p2 = %s", p[0],
                                p[2]));
            if ((p[1].y() - p[3].y()) * rule.y() > 0)
                buf
                        .append(String.format("%n\ty: p1 = %s, p3 = %s", p[1],
                                p[3]));
            if ((p[4].y() - p[6].y()) * rule.y() > 0)
                buf
                        .append(String.format("%n\ty: p4 = %s, p6 = %s", p[4],
                                p[6]));
            if ((p[5].y() - p[7].y()) * rule.y() > 0)
                buf
                        .append(String.format("%n\ty: p5 = %s, p7 = %s", p[5],
                                p[7]));

            if ((p[0].z() - p[4].z()) > 0)
                buf
                        .append(String.format("%n\tz: p0 = %s, p4 = %s", p[0],
                                p[4]));
            if ((p[1].z() - p[5].z()) > 0)
                buf
                        .append(String.format("%n\tz: p2 = %s, p6 = %s", p[1],
                                p[5]));
            if ((p[2].z() - p[6].z()) > 0)
                buf
                        .append(String.format("%n\tz: p1 = %s, p5 = %s", p[2],
                                p[6]));
            if ((p[3].z() - p[7].z()) > 0)
                buf
                        .append(String.format("%n\tz: p3 = %s, p7 = %s", p[3],
                                p[7]));

            // if we found something suspicious, then report it
            if (buf.length() > 0) {
                System.out.format("Block# %s:%s", b, buf);
                if (++count > MAX_ERRORS) {
                    System.err.println("Too many errors");
                    break;
                }
            }
        }
        System.out.format("Sanity check: %d errors found", count);
    }

    // report every point found to the geometry for the rest of the simulator
    void buildPoints(Geometry geo, Topology topo) {
        geo.setNumPoints(points.length);
        topo.setNumPoints(points.length);
        for (int i = 0; i < points.length; ++i) {
            geo.buildPoint(i, points[i]);
        }
    }

    // --------------------------- Elements methods ----------------------------
    /**
     * Mapping beween our Side enumeration and the old Orientation enumeration;
     * I guess that the definition of "top" is not univocal
     */
    final static int[] s2o = new int[] {
    // 0, 1, 2, 3, 4, 5
            0, 1, 5, 4, 2, 3 };

    void completeElements(Geometry geo, Topology topo) {
        // allocate room for the blocks
        int numOfActive = 0;
        for (int i = 0; i < active.length; ++i) {
            if (active[i]) {
                numOfActive++;
            }
        }
        geo.setNumElements(numOfActive);
        geo.setNumInterfaces(numOfActive * Side.values().length);
        topo.setNumElements(numOfActive);
        topo.setNumInterfaces(numOfActive * Side.values().length);

        // indices for interfaces and blocks respectively; these are analoguous
        // to heap top pointers when we allocate new items
        int interfaceIndex = 0;
        int blockIndex = 0;

        for (BlockNav b : grid.blocks()) {
            // only bother to process active blocks
            if (active[b.block()]) {

                // get the corner points for this block, remapping them from the
                // index they are in the eclipse data file, to the index the
                // helper structure requires
                int[] allCorners = new int[Corner.values().length];
                for (Corner c : Corner.values()) {
                    allCorners[c.rightHandIndex()] = corner[c.ordinal()][b
                            .block()];
                }

                // generate helper object for the hexahedral cell
                HexCell h = new HexCell(geo, allCorners);
                geo.buildElement(blockIndex, h.getVolume(), h.getCenterPoint());

                int[] interfaces = new int[Side.values().length];
                for (Side s : Side.values()) {
                    // ugly hack to convert from the enumeration that we have to
                    // the one from the old Eclipse package (which contains the
                    // HexCell helper class)
                    Orientation o = Orientation.values()[s2o[s.ordinal()]];

                    // retrieve data structure to compute vital statistics about
                    // the side we are looking
                    QuadFace qf = h.getFace(o);
                    geo.buildInterface(interfaceIndex, qf.getArea(), qf
                            .getCenterPoint(), rule.adjust(qf.getNormal()));

                    // get the indices of the points for this particular side
                    // (first get the corner indices for the side, then the
                    // point indices for those corners in the current block)
                    int[] cornerOrdinals = s.corners();
                    int[] cornerPoints = new int[cornerOrdinals.length];
                    for (int i = 0; i < cornerOrdinals.length; ++i) {
                        cornerPoints[i] = corner[cornerOrdinals[i]][b.block()];
                    }
                    topo.buildInterfaceTopology(interfaceIndex, cornerPoints);

                    // take note of which interface that belongs to this element
                    // for the given side
                    interfaces[s.ordinal()] = interfaceIndex;

                    // increment the index so that the next interface lands
                    // after this one in the array
                    interfaceIndex++;
                }

                // connect the interfaces to the element
                topo.buildElementTopology(blockIndex, interfaces);

                // increment the index so that the next interface lands
                // after this one in the array
                blockIndex++;
            }
        }
    }
}