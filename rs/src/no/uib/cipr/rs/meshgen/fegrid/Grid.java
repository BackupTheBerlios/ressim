package no.uib.cipr.rs.meshgen.fegrid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Tolerances;

public class Grid {
    // --------------------------------------------------------------------------
    /**
     * Three-dimensional coordinates for each vertex in the grid (which in
     * Eclipse parlance is called a 'node').
     */
    Point3D[] nodesPoint;

    /**
     * Number of times each node has been used by an interface. This array is
     * used to determine which nodes that should be written to the output (we
     * don't care about 'dead' nodes that aren't used by anyone).
     */
    int[] nodeUseCount;

    /**
     * For each node in the original list, it contains the index that the node
     * has in the output. Only the items with use count > 0 will have a valid
     * value in this array (but then again, only those nodes will be referred to
     * by other anyway).
     */
    int[] nodesWritten;

    /**
     * Offset into the node list to where the vertices of this interface begins
     * (which in Eclipse parlance is called a 'face'). There will be one more
     * element in this array than there actual surfaces; the last element
     * contains the total number of nodes referred to, so that we can always
     * find the number of nodes by subtracting a[i+1]-a[i]. the first element
     * will always be zero -- we trade that one little memory slot for the gain
     * by not having to test for the first item every time.
     */
    int[] facesNodeStart;

    /**
     * List of node which are used by faces. All lists are concatenated
     * sequentially, so you have to see in facesNodeStart which index you should
     * start listing at, and which index you should stop listing at.
     */
    int[] facesNodeList;

    /**
     * Number of times each face has been used by a cell. This is used to
     * generate extra interfaces for interior cells (both here and there).
     */
    int[] faceUseCount;

    /**
     * For each virtual face this contains the index of the real interface that
     * was generated in the output (i.e. we have to filter face numbers through
     * this list before writing something to the output). This array should be
     * maximum twice as big as the use count list (which has one entry for each
     * face), since each interface can only have a here and a there side.
     */
    int[] facesWritten;

    /**
     * Offset into the face list to where the interfaces of this cell begins.
     * There will be one more element in this array than there are actual cells;
     * the last number contains the total number of faces referred to.
     */
    int[] cellsFaceStart;

    /**
     * List of faces which are used by cells. All lists are concatenated, so you
     * must see in cellsFaceStart for the index to start at. We don't need to
     * know if a cell is 'used' by anyone; we can simply see how many faces it
     * has (all these faces are dependant on the cell)
     */
    int[] cellsFaceList;

    /**
     * For each cell in the input write the corresponding index in the
     * simulation grid, or a special magic value to indicate that it is
     * inactive.
     */
    int[] cellsWritten;

    boolean isOtherSide(int face) {
        return face >= faceUseCount.length;
    }

    int cellUseCount(int cell) {
        int start = cellsFaceStart[cell];
        int end = cellsFaceStart[cell + 1];
        int count = end - start;
        return count;
    }

    // invalid value for indices
    public static int NOT_PRESENT = -1;

    // --------------------------------------------------------------------------
    /**
     * Petrophysics. The first dimension of the array is the actual data
     */
    static final int PORO = 0;

    static final int PERMX = 1;

    static final int PERMY = 2;

    static final int PERMZ = 3;

    double[][] petro = new double[4][];

    String[] keywords = new String[] { "PORO    ", "PERMX   ", "PERMY   ",
            "PERMZ   " };

    int match(String s) {
        for (int i = 0; i < keywords.length; i++) {
            if (keywords[i].equals(s)) {
                return i;
            }
        }
        return NOT_PRESENT;
    }

    // --------------------------------------------------------------------------

    public static Mesh build(String fileName) throws Exception {
        return new Grid().parse(fileName).parse(
                fileName.replace(".FEGRID", ".init")).build();
    }

    Mesh build() {
        // count the number of active interfaces
        int numOfFaces = 0;
        for (int i = 0; i < faceUseCount.length; i++) {
            if (faceUseCount[i] > 0) {
                numOfFaces += faceUseCount[i];
            }
        }

        // use count: which nodes are used by any faces (which themselves are
        // used -- topologically exclude unreferred points)
        for (int i = 0; i < faceUseCount.length; i++) {
            if (faceUseCount[i] > 0) {
                for (int j = facesNodeStart[i]; j < facesNodeStart[i + 1]; j++) {
                    int node = facesNodeList[j];
                    nodeUseCount[node]++;
                }
            }
        }

        // count the number of active nodes
        int numOfNodes = 0;
        for (int i = 0; i < nodeUseCount.length; i++) {
            if (nodeUseCount[i] > 0) {
                numOfNodes++;
            }
        }

        // count the number of active cells
        int numOfCells = 0;
        for (int i = 0; i < cellsFaceStart.length - 1; i++) {
            if (cellUseCount(i) > 0) {
                numOfCells++;
            }
        }

        Geometry geometry = new Geometry();
        Topology topology = new Topology();

        // points
        nodesWritten = new int[nodeUseCount.length];
        for (int i = 0; i < nodesWritten.length; i++) {
            nodesWritten[i] = NOT_PRESENT;
        }
        int currNode = 0;
        int reused = 0;
        Map<Point3D, Integer> nodeMap = new HashMap<Point3D, Integer>(
                numOfNodes, 1f);
        for (int i = 0; i < nodeUseCount.length; i++) {
            if (nodeUseCount[i] > 0) {
                Point3D p = nodesPoint[i];
                // if we have already written this node before, then reuse the
                // value and redirect other to use it too
                if (nodeMap.containsKey(p)) {
                    int prevNode = nodeMap.get(p).intValue();
                    nodesWritten[i] = prevNode;
                    reused++;
                } else {
                    nodesWritten[i] = currNode;
                    nodeMap.put(p, currNode);
                    currNode++;
                }
            }
        }
        assert reused + currNode == numOfNodes;
        System.out.format(
                "Points     : nominal = %8d, active  = %8d, unique  = %d",
                nodeUseCount.length, numOfNodes, nodeMap.size());

        // discard all the reused points and write from the unique map
        geometry.setNumPoints(nodeMap.size());
        topology.setNumPoints(nodeMap.size());
        for (Entry<Point3D, Integer> e : nodeMap.entrySet()) {
            Point3D p = e.getKey();
            int i = e.getValue();
            geometry.buildPoint(i, p);
        }

        // interfaces
        geometry.setNumInterfaces(numOfFaces);
        topology.setNumInterfaces(numOfFaces);
        facesWritten = new int[2 * faceUseCount.length];
        for (int i = 0; i < facesWritten.length; i++) {
            facesWritten[i] = NOT_PRESENT;
        }
        cellsWritten = new int[cellsFaceStart.length - 1];
        for (int i = 0; i < cellsWritten.length; i++) {
            cellsWritten[i] = NOT_PRESENT;
        }
        int currFace = 0;
        int minNodesPerFace = Integer.MAX_VALUE;
        int minNodesPerFaceNdx = NOT_PRESENT;
        int maxNodesPerFace = Integer.MIN_VALUE;
        int maxNodesPerFaceNdx = NOT_PRESENT;

        // number of connections; we can detect these will enumerating
        // interfaces
        int numOfConn = 0;

        // interfaces and cells
        geometry.setNumElements(numOfCells);
        topology.setNumElements(numOfCells);
        int currCell = 0;
        int minFacesPerCell = Integer.MAX_VALUE;
        int minFacesPerCellNdx = NOT_PRESENT;
        int maxFacesPerCell = Integer.MIN_VALUE;
        int maxFacesPerCellNdx = NOT_PRESENT;
        for (int i = 0; i < cellsFaceStart.length - 1; i++) {
            // expand the faces to a point cloud, where each point only
            // occur once
            Set<Point3D> cloud = new HashSet<Point3D>();

            int faceStart = cellsFaceStart[i];
            int faceCount = cellsFaceStart[i + 1] - faceStart;
            int[] faces = new int[faceCount];
            int[][] nodes = new int[faceCount][];
            int[][] nodeIndices = new int[faceCount][];
            Point3D[][] points = new Point3D[faceCount][];

            // write all interfaces for this cell; each interface is referred
            // to only once
            for (int j = 0; j < faces.length; j++) {
                faces[j] = cellsFaceList[faceStart + j];

                // get nodes for face i
                int face = faces[j];
                if (face >= faceUseCount.length) {
                    face -= faceUseCount.length;
                }
                int nodeStart = facesNodeStart[face];
                int nodeCount = facesNodeStart[face + 1] - nodeStart;
                nodes[j] = new int[nodeCount];
                for (int k = 0; k < nodes[j].length; k++) {
                    nodes[j][k] = facesNodeList[nodeStart + k];
                }

                // get a list of real points for this interface
                points[j] = new Point3D[nodes[j].length];
                for (int k = 0; k < nodes[j].length; k++) {
                    points[j][k] = nodesPoint[nodes[j][k]];
                    cloud.add(points[j][k]);
                }
            }

            // calculate statistics for the cell
            Point3D[] p = cloud.toArray(new Point3D[cloud.size()]);
            Point3D cellCenter = Point3D.center(p);

            // restart the interface generation now that we know the center of
            // the entire element
            for (int j = 0; j < faces.length; j++) {
                // calculate statistics for the interface
                Point3D faceCenter = Point3D.center(points[j]);

                Vector3D ab = new Vector3D(points[j][0], points[j][1]);
                Vector3D ac = new Vector3D(points[j][0],
                        points[j][points[j].length - 1]);
                Vector3D normal = ab.cross(ac);
                Vector3D out = new Vector3D(cellCenter, faceCenter);
                double direction = Math.signum(normal.dot(out
                        .mult(Tolerances.largeEps)));
                normal = normal.mult(direction).unitize();

                // TODO; Calculate area for polygon
                double area = 1.;

                // translate the node indices to the ones that were used when
                // writing them to output
                nodeIndices[j] = new int[nodes[j].length];
                for (int k = 0; k < nodeIndices[j].length; k++) {
                    nodeIndices[j][k] = nodesWritten[nodes[j][k]];
                    assert nodeIndices[j][k] != NOT_PRESENT;
                }

                geometry.buildInterface(currFace, area, faceCenter, normal);
                topology.buildInterfaceTopology(currFace, nodeIndices[j]);

                facesWritten[faces[j]] = currFace;
                currFace++;

                // virtual interfaces are the 'there' side of a connection; they
                // correspond one-to-one
                if (faces[j] > faceUseCount.length) {
                    numOfConn++;
                }

                // gather statistics about the complexity of a face
                int nodesPerFace = points.length;
                if (nodesPerFace < minNodesPerFace) {
                    minNodesPerFace = nodesPerFace;
                    minNodesPerFaceNdx = facesWritten[faces[j]];
                }
                if (nodesPerFace > maxNodesPerFace) {
                    maxNodesPerFace = nodesPerFace;
                    maxNodesPerFaceNdx = facesWritten[faces[j]];
                }
            }

            // if the cell is active at all, then write it
            if (faces.length > 0) {
                // translate the face indices to something that is known for the
                // output
                int[] faceIndices = new int[faces.length];
                for (int j = 0; j < faceIndices.length; j++) {
                    faceIndices[j] = facesWritten[faces[j]];
                    assert faceIndices[j] != NOT_PRESENT;
                }

                // TODO: Calculate volume for the figure
                double volume = 1.;

                // write the cell
                geometry.buildElement(currCell, volume, cellCenter);
                topology.buildElementTopology(currCell, faceIndices);
                cellsWritten[i] = currCell;
                currCell++;

                // gather statistics about the complexity of a cell
                int facesPerCell = faces.length;
                if (facesPerCell < minFacesPerCell) {
                    minFacesPerCell = facesPerCell;
                    minFacesPerCellNdx = cellsWritten[i];
                }
                if (facesPerCell > maxFacesPerCell) {
                    maxFacesPerCell = facesPerCell;
                    maxFacesPerCellNdx = cellsWritten[i];
                }
            }
        }
        assert currFace == numOfFaces;
        assert currCell == numOfCells;

        // statistics about use count
        int minNodeUse = Integer.MAX_VALUE;
        int minNodeNdx = NOT_PRESENT;
        int maxNodeUse = Integer.MIN_VALUE;
        int maxNodeNdx = NOT_PRESENT;
        for (int i = 0; i < nodeUseCount.length; i++) {
            int use = nodeUseCount[i];
            // only count active nodes
            if (use > 0) {
                if (use < minNodeUse) {
                    minNodeUse = use;
                    minNodeNdx = nodesWritten[i];
                }
                if (use > maxNodeUse) {
                    maxNodeUse = use;
                    maxNodeNdx = nodesWritten[i];
                }
            }
        }

        int minFaceUse = Integer.MAX_VALUE;
        int minFaceNdx = NOT_PRESENT;
        int maxFaceUse = Integer.MIN_VALUE;
        int maxFaceNdx = NOT_PRESENT;
        for (int i = 0; i < faceUseCount.length; i++) {
            int use = faceUseCount[i];
            // only count active faces
            if (use > 0) {
                if (use < minFaceUse) {
                    minFaceUse = use;
                    minFaceNdx = facesWritten[i];
                }
                if (use > maxFaceUse) {
                    maxFaceUse = use;
                    maxFaceNdx = facesWritten[i];
                }
            }
        }
        System.out.format("Interfaces : nominal = %8d, active  = %8d",
                faceUseCount.length, numOfFaces);
        System.out.format("Elements   : nominal = %8d, active  = %8d",
                cellsWritten.length, numOfCells);
        System.out.format("Face use   : minimum = %8d, maximum = %8d",
                minFaceUse, minFaceNdx, maxFaceUse, maxFaceNdx);
        System.out
                .format(
                        "Node use   : minimum = %8d, (node)  = %8d, maximum = %8d, (node)  = %8d",
                        minNodeUse, minNodeNdx, maxNodeUse, maxNodeNdx);
        System.out
                .format(
                        "Corners    : minimum = %8d, (face)  = %8d, maximum = %8d, (face)  = %8d",
                        minNodesPerFace, minNodesPerFaceNdx, maxNodesPerFace,
                        maxNodesPerFaceNdx);
        System.out
                .format(
                        "Sides      : minimum = %8d, (cell)  = %8d, maximum = %8d, (cell)  = %8d",
                        minFacesPerCell, minFacesPerCellNdx, maxFacesPerCell,
                        maxFacesPerCellNdx);

        // connections
        geometry.setNumConnections(0, 0);
        topology.setNumConnections(0, 0);
        /*
         * geometry.setNumConnections(numOfConn, 0);
         * topology.setNumConnections(numOfConn, 0); // enumerate all the
         * 'there' interfaces; create a connection for each // of them (the
         * 'here' interfaces may be exterior ones too) int currConn = 0; for(int
         * i = faceUseCount.length; i < facesWritten.length; i++) { int there =
         * facesWritten[i]; if (there != NOT_PRESENT) { // get the dual
         * interface int here = facesWritten[i - faceUseCount.length]; // write
         * connection topology.buildNeighbourConnectionTopology(currConn, here,
         * there); currConn++; } } assert currConn == numOfConn;
         */
        System.out.format("Connections: neighbour = %8d", numOfConn);

        // number of unique rock types that have been identified in the file
        // we use a map instead of a set, since a map allows us to actually get
        // out the item that matches another object, so that we can replace
        // object equality with reference equality (saves memory by reuse).
        int numOfRocks = 0;
        HashMap<Rock, Rock> canonicalRocks = new HashMap<Rock, Rock>();
        Rock[] rocks = new Rock[numOfCells];
        double minPoroVal = Double.MAX_VALUE;
        int minPoroNdx = NOT_PRESENT;
        double maxPoroVal = Double.MIN_VALUE;
        int maxPoroNdx = NOT_PRESENT;
        // only active cells have petrophysics associated with them
        /*
         * for(int i = 0; i < cellsWritten.length; i++) { int cell =
         * cellsWritten[i]; assert 0 <= cell && cell <= rocks.length; if(cell !=
         * NOT_PRESENT) {
         */
        for (int i = 0; i < rocks.length; i++) {
            int cell = i;
            if (true) {
                // porosity
                double phi = petro[PORO][i];

                if (phi < minPoroVal) {
                    minPoroVal = phi;
                    minPoroNdx = cell;
                }
                if (phi > maxPoroVal) {
                    maxPoroVal = phi;
                    maxPoroNdx = cell;
                }

                // TODO: shouldn't be necessary to launder data like this
                phi = Math.min(Math.max(phi, 0.), 1.);

                // no compaction
                double cr = 0;

                // diagonal tensor for permeability
                double permx = petro[PERMX][i];
                double permy = petro[PERMY][i];
                double permz = petro[PERMZ][i];
                Tensor3D K = new Tensor3D(permx, permy, permz);

                // create new rock type for this petrophysics found in this cell
                Rock r = new Rock(phi, cr, K, String.format("rock%d",
                        numOfRocks));
                // see if we have found exactly the same type before; if so,
                // replace our reference with one to the canonical rep. the
                // number that was appended to the name gets reused for the next
                // otherwise, we declare this as our new canonical rep. and use
                // it
                if (canonicalRocks.containsKey(r)) {
                    r = canonicalRocks.get(r);
                } else {
                    canonicalRocks.put(r, r);
                    numOfRocks++;
                }

                // assign the rock type that we found to this cell
                rocks[cell] = r;
            }
        }
        System.out.format("Rock types : count   = %8d", numOfRocks);
        System.out
                .format(
                        "Porosity   : minimum = %7.2f, (cell)  = %8d, maximum = %7.2f, (cell)  = %8d",
                        minPoroVal, minPoroNdx, maxPoroVal, maxPoroNdx);

        Mesh mesh = new Mesh(geometry, topology, rocks);
        return mesh;
    }

    Grid parse(String fileName) throws Exception {
        InputStream is = new FileInputStream(fileName);
        Reader reader = new BufferedReader(new InputStreamReader(is));
        StreamTokenizer tokenizer = new StreamTokenizer(reader);

        tokenizer.resetSyntax();
        tokenizer.eolIsSignificant(false);
        tokenizer.whitespaceChars(0, ' ');

        // treat numeric symbols as part of words; we want the string
        // representation of the numbers, in order to retrieve the type
        // that we want
        tokenizer.wordChars('-', '-');
        tokenizer.wordChars('+', '+');
        tokenizer.wordChars('*', '*');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('.', '.');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.quoteChar('\'');

        // we plan to refactor this method out of this class later
        Grid g = this;

        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            String name = tokenizer.sval;
            tokenizer.nextToken();
            int count = Integer.parseInt(tokenizer.sval);
            tokenizer.nextToken();
            String type = tokenizer.sval;

            // index of the current petrophysic property being read
            int prop = NOT_PRESENT;

            if (name.equals("BOXES   ")) {
                tokenizer.nextToken();
                int min_i = Integer.parseInt(tokenizer.sval);
                tokenizer.nextToken();
                int min_j = Integer.parseInt(tokenizer.sval);
                tokenizer.nextToken();
                int min_k = Integer.parseInt(tokenizer.sval);
                tokenizer.nextToken();
                int max_i = Integer.parseInt(tokenizer.sval);
                tokenizer.nextToken();
                int max_j = Integer.parseInt(tokenizer.sval);
                tokenizer.nextToken();
                int max_k = Integer.parseInt(tokenizer.sval);
                System.out.printf("Box       : (%d, %d, %d)-(%d, %d, %d)%n",
                        min_i, min_j, min_k, max_i, max_j, max_k);
            }

            // nodes
            else if (name.equals("NDCOORD ")) {
                int numOfPoints = count / 3;
                g.nodesPoint = new Point3D[numOfPoints];
                for (int i = 0; i < numOfPoints; i++) {
                    tokenizer.nextToken();
                    double x = Double.parseDouble(tokenizer.sval);
                    tokenizer.nextToken();
                    double y = Double.parseDouble(tokenizer.sval);
                    tokenizer.nextToken();
                    double z = Double.parseDouble(tokenizer.sval);
                    g.nodesPoint[i] = new Point3D(x, y, z);
                }

                // each node has a use count, but no-one has referred to them
                // yet
                g.nodeUseCount = new int[numOfPoints];
            }

            // faces: how many nodes in each
            else if (name.equals("NFACENOD")) {
                g.facesNodeStart = new int[count + 1];
                int total = 0;
                // note that we are starting at one instead of zero since the
                // next interface takes off where this one ends
                for (int i = 1; i < g.facesNodeStart.length; i++) {
                    tokenizer.nextToken();
                    int nodes = Integer.parseInt(tokenizer.sval);
                    total += nodes;
                    g.facesNodeStart[i] = total;
                }
            }

            // faces: mapping from a face to its nodes
            else if (name.equals("FACENODS")) {
                g.facesNodeList = new int[count];
                for (int i = 0; i < g.facesNodeList.length; i++) {
                    tokenizer.nextToken();
                    // was one-based, we want zero-based
                    int index = Integer.parseInt(tokenizer.sval) - 1;
                    // enter this index into the list of nodes that the current
                    // face (whatever that is) is using
                    g.facesNodeList[i] = index;
                }
            }

            // cells: number of faces in each
            else if (name.equals("NCELLFAC")) {
                int numOfFaceStarts = count + 1;
                g.cellsFaceStart = new int[numOfFaceStarts];
                int total = 0;
                for (int i = 1; i < g.cellsFaceStart.length; i++) {
                    tokenizer.nextToken();
                    int faces = Integer.parseInt(tokenizer.sval);

                    // negative if the cell is not present
                    if (faces == -1) {
                        faces = 0;
                    }
                    total += faces;
                    g.cellsFaceStart[i] = total;
                }
            }

            // cells: mapping from a cell to its faces
            else if (name.equals("CELLFACS")) {
                g.cellsFaceList = new int[count];
                g.faceUseCount = new int[g.facesNodeStart.length - 1];
                for (int i = 0; i < g.cellsFaceList.length; i++) {
                    tokenizer.nextToken();
                    int index = Integer.parseInt(tokenizer.sval) - 1;

                    // calculate the 'virtual' index of this interface based
                    // on the number of times it has been seen before
                    int use = g.faceUseCount[index]++;
                    int face = use * g.faceUseCount.length + index;
                    g.cellsFaceList[i] = face;
                }
            }

            // petrophysics
            else if ((prop = match(name)) != NOT_PRESENT) {
                petro[prop] = new double[count];
                for (int i = 0; i < petro[prop].length; i++) {
                    tokenizer.nextToken();
                    double value = Double.parseDouble(tokenizer.sval);
                    petro[prop][i] = value;
                }
            }

            else {
                System.out
                        .format(
                                "Ignored    : name    = %s, count   = %8d, type    = %s",
                                name, count, type);

                for (int i = 0; i < count; i++) {
                    tokenizer.nextToken();
                }
            }
        }
        return g;
    }
}
