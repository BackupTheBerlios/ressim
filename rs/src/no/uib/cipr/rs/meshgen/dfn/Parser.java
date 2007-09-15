package no.uib.cipr.rs.meshgen.dfn;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.geometry.Point3D;

public class Parser {

    private Node[] nodes;

    private Segment[] segments;

    private Polygon[] polygons;

    private Connection[] connections;

    private Zone[] zones;

    private int numNodes;

    private int numSegments;

    private int numPolygons;

    private int numConnections;

    private int numZones;

    private int numInterfaces;

    // data for active control volumes
    private List<CV> activeCVs;

    private int numActive;

    private Point3D[] centers;

    private double[] volumes;

    private double[] transmissibilities;

    public Parser(String gridfile, String transfile) throws IOException {
        readGridFile(gridfile);

        // compute active control volumes
        activeCVs = computeActive();

        numActive = activeCVs.size();

        // Compute the number of interfaces
        numInterfaces = computeNumInterfaces();

        readTransFile(transfile);
    }

    /**
     * Runs through the grid to determine the number of interfaces
     */
    private int computeNumInterfaces() {
        int interf = 0;

        for (CV cv : activeCVs) {
            if (cv instanceof Node)
                throw new IllegalArgumentException("Node CVs not implemented");
            else if (cv instanceof Segment)
                interf += 4;
            else if (cv instanceof Polygon) {
                Polygon e = (Polygon) cv;

                int[] segments = e.getSegmentIndices();

                interf += segments.length;

            } else if (cv instanceof Polyhedron)
                throw new IllegalArgumentException(
                        "Polyhedron CVs not implemented");
            else
                throw new IllegalArgumentException("Unknown CV type");
        }

        return interf;
    }

    /**
     * Creates the list of active control volumes. Currently Nodes and
     * Polyhedrons are skipped.
     */
    private List<CV> computeActive() {
        List<CV> active = new ArrayList<CV>();

        for (Segment segment : segments)
            if (segment.isActive())
                active.add(segment);

        for (Polygon polygon : polygons)
            if (polygon.isActive())
                active.add(polygon);

        return active;
    }

    private void readGridFile(String fileName) throws IOException {
        StreamTokenizer stream = getStream(fileName);

        readSizes(stream);

        readNodes(stream);

        readSegments(stream);

        readPolygons(stream);

        // readPolyhedrons(stream);

        readZones(stream);
    }

    private void readTransFile(String fileName) throws IOException {

        StreamTokenizer stream = getStream(fileName);

        int ntd0 = getInt(stream);

        if (ntd0 != numNodes)
            throw new IllegalArgumentException("Mismatch in number of nodes");

        int ncvs = getInt(stream);

        if (ncvs != numActive)
            throw new IllegalArgumentException(
                    "Mismatch in number of active CVs");

        numConnections = getInt(stream);

        readCornerPoints(stream);

        readControlVolumes(stream);

        readConnections(stream);
    }

    /**
     * Reads the sizes
     */
    private void readSizes(StreamTokenizer stream) throws IOException {
        numNodes = getInt(stream);

        numSegments = getInt(stream);

        numPolygons = getInt(stream);

        // This is the number of polyhedrons, which isn't used here
        int numPolyhedrons = getInt(stream);
        if (numPolyhedrons > 0)
            throw new IllegalArgumentException("Cannot handle polyhedrons");

        numZones = getInt(stream);
    }

    private void readNodes(StreamTokenizer stream) throws IOException {
        nodes = new Node[numNodes];

        for (int i = 0; i < numNodes; i++) {
            double x = getDouble(stream);
            double y = getDouble(stream);
            double z = getDouble(stream);

            Point3D coord = new Point3D(x, y, z);

            int code = getInt(stream);

            nodes[i] = new Node(coord, code);
        }
    }

    private void readSegments(StreamTokenizer stream) throws IOException {
        segments = new Segment[numSegments];

        for (int i = 0; i < numSegments; i++) {
            int first = getInt(stream);
            int second = getInt(stream);

            int code = getInt(stream);

            segments[i] = new Segment(first, second, code);
        }
    }

    private void readPolygons(StreamTokenizer stream) throws IOException {
        polygons = new Polygon[numPolygons];

        for (int i = 0; i < numPolygons; i++) {
            int nNodes = getInt(stream);

            int[] nodeList = new int[nNodes];
            for (int aNode = 0; aNode < nNodes; aNode++)
                nodeList[aNode] = getInt(stream);

            int nSegments = getInt(stream);

            int[] segList = new int[nSegments];
            for (int aSeg = 0; aSeg < nSegments; aSeg++)
                segList[aSeg] = getInt(stream);

            int code = getInt(stream);

            polygons[i] = new Polygon(nodeList, segList, code);
        }
    }

    // private void readPolyhedrons(StreamTokenizer stream) throws IOException {
    // TODO Auto-generated method stub
    // }

    private void readZones(StreamTokenizer stream) throws IOException {
        zones = new Zone[numZones];

        for (int i = 0; i < numZones; i++) {
            double poro = getDouble(stream);
            double perm = getDouble(stream);
            double corr = getDouble(stream);

            zones[i] = new Zone(poro, perm, corr);
        }
    }

    private void readConnections(StreamTokenizer stream) throws IOException {
        connections = new Connection[numConnections];
        transmissibilities = new double[numConnections];

        for (int i = 0; i < numConnections; i++) {
            int cv0 = getInt(stream);
            int cv1 = getInt(stream);
            connections[i] = new Connection(cv0, cv1);
            transmissibilities[i] = getDouble(stream);
        }
    }

    private void readCornerPoints(StreamTokenizer stream) throws IOException {
        for (int i = 0; i < numNodes; i++) {
            getDouble(stream); // x-coord
            getDouble(stream); // y-coord
            getDouble(stream); // z-coord
        }
    }

    private void readControlVolumes(StreamTokenizer stream) throws IOException {
        centers = new Point3D[numActive];

        volumes = new double[numActive];

        for (int i = 0; i < numActive; i++) {

            double x = getDouble(stream);
            double y = getDouble(stream);
            double z = getDouble(stream);

            centers[i] = new Point3D(x, y, z);

            volumes[i] = getDouble(stream);

            // read porosity, using zones instead
            getDouble(stream);

            // read corner points. not in use
            int numCP = getInt(stream);

            for (int aCP = 0; aCP < numCP; aCP++) {
                getInt(stream); // cp-index
            }
        }
    }

    private StreamTokenizer getStream(String fileName) {
        BufferedReader fileStream = null;
        try {
            fileStream = new BufferedReader(new FileReader(fileName));
        } catch (IOException ioe) {
            System.out.println("Problem reading " + fileName);
        }

        StreamTokenizer stream = new StreamTokenizer(fileStream);

        setup(stream);

        return stream;
    }

    /**
     * Sets up the stream tokenizer
     */
    private void setup(StreamTokenizer stream) {
        stream.resetSyntax();
        stream.eolIsSignificant(false);
        stream.lowerCaseMode(true);

        // Parse numbers as words
        stream.wordChars('0', '9');
        stream.wordChars('.', '.');
        stream.wordChars('-', '.');

        // Characters as words
        stream.wordChars('\u0000', '\u00FF');

        // Skip comments
        stream.commentChar('%');

        // Skip whitespace and newlines
        stream.whitespaceChars(' ', ' ');
        stream.whitespaceChars('\t', '\t');
        stream.whitespaceChars('\n', '\n');
        stream.whitespaceChars('\f', '\f');
        stream.whitespaceChars('\r', '\r');

    }

    /**
     * Reads an integer
     */
    private int getInt(StreamTokenizer stream) throws IOException {
        stream.nextToken();
        if (stream.ttype == StreamTokenizer.TT_WORD)
            return Integer.parseInt(stream.sval);
        else if (stream.ttype == StreamTokenizer.TT_EOF)
            throw new EOFException("End-of-File encountered during parsing");
        else
            throw new IOException("Unknown token found during parsing");
    }

    /**
     * Reads a double
     */
    private double getDouble(StreamTokenizer stream) throws IOException {
        stream.nextToken();
        if (stream.ttype == StreamTokenizer.TT_WORD)
            return Double.parseDouble(stream.sval);
        else if (stream.ttype == StreamTokenizer.TT_EOF)
            throw new EOFException("End-of-File encountered during parsing");
        else
            throw new IOException("Unknown token found during parsing");
    }

    public Point3D[] getPoints() {
        Point3D[] points = new Point3D[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            points[i] = nodes[i].getCoord();
        }

        return points;
    }

    public Point3D getCenter(int element) {
        return centers[element];
    }

    public double getVolume(int element) {
        return volumes[element];
    }

    public List<CV> getActiveCVs() {
        return activeCVs;
    }

    public double getCorrection(int zone) {
        return zones[zone].getCorr();
    }

    public double getPorosity(int zone) {
        return zones[zone].getPoro();
    }

    public double getPermeability(int zone) {
        return zones[zone].getPerm();
    }

    public Point3D getPoint(int i) {
        return nodes[i].getCoord();
    }

    public Segment getSegment(int i) {
        return segments[i];
    }

    public Point3D getElementCenter(int i) {
        return centers[i];
    }

    public int getNumNodes() {
        return numNodes;
    }

    public int getNumActiveCVs() {
        return numActive;
    }

    public int getNumInterfaces() {
        return numInterfaces;
    }

    public int getNumConnections() {
        return numConnections;
    }

    public Connection getConnection(int connection) {
        return connections[connection];
    }

    public int getNumZones() {
        return numZones;
    }

    public int getElementZone(int element) {
        return activeCVs.get(element).getCode();
    }

    public double getTransmissibility(int connection) {
        return transmissibilities[connection];
    }
}
