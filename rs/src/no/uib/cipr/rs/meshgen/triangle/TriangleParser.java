package no.uib.cipr.rs.meshgen.triangle;

import java.util.Map;
import no.uib.cipr.rs.util.Pair;

/**
 * Read triangles from a .ele file generated by Triangle. This class
 * encapsulates a function containing inner functions. The reader is a recursive
 * descent parser.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class TriangleParser extends ParserBase {
    // triangles are stored in a separate file
    @Override
    protected String suffix() {
        return "ele";
    }

    // only overridden because one cannot inherit constructors
    public TriangleParser(String stem, int refinement) throws TriExc {
        super(stem, refinement);
    }

    // information about the format of the file read from its header
    private int numOfTriangles; // total number of triangles

    private int numOfPoints; // number of points per triangle

    private int numOfAttrs; // number of attributes per triangle

    /**
     * Read the header of the file. This should be the first operation that is
     * performed, so the object can be properly initialized with the information
     * needed to read the rest of it.
     */
    private void readHeader() throws TriExc {
        // first line of the file provide meta-information about the
        // contents. it has the format:
        // <num> <dim> <attr-count>
        // each of these statistics is used as a loop boundary (in either
        // readNextElement or in readAll itself).
        this.numOfTriangles = scanner.nextInt();
        this.numOfPoints = scanner.nextInt();
        this.numOfAttrs = scanner.nextInt();

        // sanity check on the information that we just read. there must
        // be a positive number of triangles
        if (numOfTriangles < 0) {
            throw TriExc.INVALID_COUNT_TRIANGLES.create(numOfTriangles);
        }

        // we are not capable of reading structures having anything
        // other than three points (i.e. triangles).
        if (numOfPoints != 3) {
            throw TriExc.NOT_THREE_POINTS.create(numOfPoints);
        }

        // no more information is anticipated on the header line
        skipRestOfLine();
    }

    /**
     * Read one more triangle element from the input source (file).
     * 
     * @return Composite consisting of the index of the element which the file
     *         declares, and an array of the points that make out this
     *         structure.
     */
    private Pair<Integer, Integer[]> readNextElement() {
        // each data line in the file has the format:
        // <index> <corner1> <corner2> <corner3> [<attr>...]
        Integer index = scanner.nextInt();

        // read each of the points into an array (we could have created
        // a triple structure, but we are not going to do anything with
        // the triple itself so we may just as well use an array).
        Integer[] points = new Integer[numOfPoints];
        for (int i = 0; i < numOfPoints; i++) {
            points[i] = scanner.nextInt();
        }

        // read all attributes associated with this points and throw
        // them away (we don't have any use for them (yet)).
        for (int i = 0; i < numOfAttrs; i++) {
            scanner.nextInt();
        }

        // compose a terminal parser node for the element that is to be
        // returned to the abstract syntax tree (which exists on the call
        // stack of the main parsing routine).
        return new Pair<Integer, Integer[]>(index, points);
    }

    /**
     * 
     * @param pointMap
     * @param factory
     */
    public void readAll(Map<Integer, Integer> pointMap, TriangleHandler factory)
            throws TriExc {
        // read the header first to get a tally of the elements
        readHeader();

        // declare our intentions to the sink
        factory.prepareForTriangles(numOfTriangles);

        // read each of the elements and
        for (int i = 0; i < numOfTriangles; i++) {
            Pair<Integer, Integer[]> elem = readNextElement();

            // we don't have any needs for the index (since we are not
            // reading anything that refers to the triangles), so dump
            // the index and continue with the points
            Integer[] corners = elem.y();

            // translate each of the corners into the index used by the
            // sink. since both of these types are integers, we don't
            // bother creating a new array but rather replace them inline
            for (int j = 0; j < corners.length; j++) {
                corners[j] = pointMap.get(corners[j]);
            }

            // send the (translated) triple of corners to the sink, which
            // will insert it into its own structure
            factory.onTriangle(corners[0], corners[1], corners[2]);
        }

        // tell the sink that we are done reading
        factory.closeTriangles();
    }
}