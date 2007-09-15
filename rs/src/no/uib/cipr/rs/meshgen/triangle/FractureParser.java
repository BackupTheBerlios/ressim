package no.uib.cipr.rs.meshgen.triangle;

import java.util.Map;

import no.uib.cipr.rs.util.Pair;

/**
 * Read fractures from a file. Fractures are lines that constrains a mesh
 * triangularization, since we want to have an interface (between elements) to
 * model detailed behaviour at that position.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class FractureParser extends ParserBase {
    // triangularizer stored the fractures in an output file too since
    // it may add points to setup intersections.
    @Override
    protected String suffix() {
        return "poly";
    }

    // only defined because one cannot inherit constructors
    public FractureParser(String stem, int refinement) throws TriExc {
        super(stem, refinement);
    }

    // number of fractures that are stored in the files
    private int numOfFractures;

    // read a header describing the contents of the rest of the file
    private void readHeader() throws TriExc {
        // first line of the poly file is intended to store points, and
        // look like this:
        // <num> <dims> <attr-count> <has-bounds>
        // however the points aren't stored in this file but in the
        // node output file. the line is still present to make the file
        // compatible with the format I guess. read this line to make
        // sure we're not reading from a
        int numOfPoints = scanner.nextInt();

        // verify that we're not starting to read from a file we don't
        // really understand or expect
        if (numOfPoints != 0) {
            throw TriExc.UNEXPECTED_POINTS.create(numOfPoints);
        }

        // we have no use of the dimensions, attribute count or boundary
        // flag of the nodes so simply drop those three components
        for (int i = 0; i < 3; i++) {
            scanner.nextInt();
        }

        // now let's get down to business. the header that declares the
        // fractures only contains two values:
        // <count> <boundary-flag>
        this.numOfFractures = scanner.nextInt();
        boolean hasBoundaries = (scanner.nextInt() != 0);

        // there must be a valid number of fractures in the file
        if (!(numOfFractures >= 0)) {
            throw TriExc.INVALID_COUNT_FRACTURES.create(numOfFractures);
        }

        // to separate fractures from boundaries we'll need a flag that the
        // algorithm put there for us. if the flag is missing we shouldn't
        // continue reading from the file
        if (!hasBoundaries) {
            throw TriExc.MUST_HAVE_BOUNDARIES.create();
        }

        // file pointer is just before the data starts in the file,
        // ready to read fractures.
    }

    // read one more data point from the file. return value is a triple
    // consisting of first the flag of whether it is a boundary or not
    // and then a pair of indices to the two points of the edge
    private Pair<Integer, Pair<Integer, Integer>> readNextFrac() {
        // each fracture is specified on each its line on the format:
        // <index> <source-point> <target-point> <boundary-flag>
        // there are no attributes for fractures (unfortunately).
        /* int index = */scanner.nextInt();
        int source = scanner.nextInt();
        int target = scanner.nextInt();
        int marker = scanner.nextInt();
        
        // compose a return value that consists of all the information
        // that we are interested in from this function, as one object
        return new Pair<Integer, Pair<Integer, Integer>>(marker,
                new Pair<Integer, Integer>(source, target));
    }

    // read the rest of the file after the points, to test if we are
    // able to parse it completely without errors
    private void readFooter() throws TriExc {
        // after the polygons (lines) there comes a section with holes;
        // this should be empty as well (just like the points).
        int numOfHoles = scanner.nextInt();
        if (numOfHoles != 0) {
            throw TriExc.UNEXPECTED_HOLES.create(numOfHoles);
        }
    }

    /**
     * Pump all fractures from the output of the Triangle program into your own
     * data structure.
     * 
     * @param pointMap
     *            Map from the internal indices to external indices used in the
     *            creator. The creator will only retrieve indices that it
     *            created itself. (Whereas we need our own indices when reading
     *            from file)
     * @param factory
     *            Sink that will retrieve fractures from the file and register
     *            them it another data structure.
     */
    public void readAll(Map<Integer, Integer> pointMap, FractureHandler factory)
            throws TriExc {
        // read the header to figure out how many fractures we are
        // talking about. this will initialize our members.
        readHeader();

        // communicate this number with the sink, letting it allocate
        // memory as necessary
        factory.prepareForFractures(numOfFractures);

        // loop through each of the points, piping them to the sink
        for (int i = 0; i < numOfFractures; i++) {
            // read one data line from file
            Pair<Integer, Pair<Integer, Integer>> data = readNextFrac();

            // split it into flag and edge
            Kind c = Kind.fromMarker(data.x().intValue());
            Pair<Integer, Integer> edge = data.y();

            // convert into indices that the factory understands
            int a = pointMap.get(edge.x());
            int b = pointMap.get(edge.y());

            // pass each of these along to the factory
            factory.onFracture(a, b, c);
        }

        // complete the parsing of the file
        readFooter();

        // tell the sink that we're done
        factory.closeFractures();
    }
}