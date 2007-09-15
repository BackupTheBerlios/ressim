package no.uib.cipr.rs.meshgen.triangle;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

import no.uib.cipr.rs.meshgen.util.StDev;
import no.uib.cipr.rs.util.Pair;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Read one plane of the output from Frac3D.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class SubplaneParser implements Source {
    private Scanner scanner;

    private int numOfPoints;

    private int numOfEdges;

    /**
     * Construct a parser that reads from a certain subplane.
     * 
     * @param fileName
     *            Name of the file that contains the subplane, e.g.
     *            "Subplane3D_000.dat" (the extension must be included).
     */
    public SubplaneParser(String fileName) throws TriExc {
        // setup reading from the source file
        try {
            scanner = new Scanner(new FileReader(fileName));
        } catch (FileNotFoundException fnf) {
            throw TriExc.FILE_NOT_FOUND.create(fnf, fileName);
        }

        // we need to include comma as a delimiter since the header is
        // a comma-separated list of named fields. the data themselves
        // are separated by space and period is always used as a decimal
        // separator
        scanner.useDelimiter(",\\s*|\\s+");
    }

    public static void main(String[] args) throws Exception {
        String fileName = "Subplane3D_000.dat";
        ProblemSetup prob = new ProblemSetup("foobar");
        SubplaneParser subplaneParser = new SubplaneParser(fileName);
        subplaneParser.readAll(prob, prob);
        subplaneParser.close();
        prob.close();
    }

    /**
     * Read the first lines that tells us how to parse the rest
     */
    private void readHeader() {
        // first two lines are title and variable declaration, none of
        // which we are interested. however, we match the exact pattern
        // of these commands to make sure that we have been passed a
        // syntactically correct file.
        scanner.skip("TITLE=");
        scanner.skip("\"[^\"]*\"[ ]{1}\n");
        scanner.skip("VARIABLES=\"X\" \"Y\" \"Z\"[ ]{5}\n");

        // third line contains the information about the number of
        // points and the number of fractures found between them.
        scanner.skip("ZONE[ ]{1}");
        scanner.skip("N=");
        numOfPoints = scanner.nextInt();
        scanner.skip(", E=");
        numOfEdges = scanner.nextInt();
        scanner.skip(", F=FEPOINT");
        scanner.skip(", ET=TRIANGLE");

        // print diagnostics on the screen
        /*
         * System.out.printf("numOfPoints: %d%n", numOfPoints);
         * System.out.printf("numOfEdges: %d%n", numOfEdges);
         */
    }

    /**
     * Each point in the file contains this number of dimensions. Much of our
     * reading is based on looping through all of the fields in a point etc., so
     * we declare it as a constant here to increase readability of the kind.
     */
    private static final int NUM_OF_DIMS = 3;

    private static final int DIMS_IN_PLANE = 2;

    /**
     * Logical axis. By using these constants into the mapping vector d you can
     * find which dimensions in the arrays you should use to get these logical
     * dimensions. For instance, to get the first coordinate you can first
     * lookup d[X] to find the dimension and then index the point array with
     * this value again to find the correct series.
     */
    private static final int X = 0;

    private static final int Y = 1;

    /**
     * Read the points together with statistics from the file. In most other
     * cases we preper to process data in a pipe, but in this case we must
     * preprocess them to figure out which of the elements we should keep (we
     * can only determine that after having looked at the actual data -- the
     * header isn't sufficient enough).
     * 
     * @return First component of the returned pair is an array with data
     *         series. The outermost (first index) of the array denotes which
     *         data series (x, y or z) whereas the second index denotes the
     *         element in which data series (0..numOfPoints-1).
     * 
     * The second component of returned pair is an integer array with only two
     * dimensions, which tells which of the three dimensions that has any
     * significant variation in the data. (The third dimensions which is not
     * mentioned in this array, has a constant value, e.g. 0).
     */
    private Pair<double[][], int[]> readPoints() throws TriExc {
        // if we haven't gotten any points from the file, there isn't
        // much to triangularize
        assert (numOfPoints > 0);

        // allocate the three arrays; one for each dimension. Then, each
        // dimensions is separately initialized to an array to hold all
        // the basis vector values in this dimension for the points
        double[][] xyz = new double[NUM_OF_DIMS][];
        for (int i = 0; i < xyz.length; i++) {
            xyz[i] = new double[numOfPoints];
        }

        // also allocate a statistical variable for each of the dimensions
        // this variable will help us determine which dimension that is
        // constant in this subplane (x, y or z).
        StDev[] s = new StDev[xyz.length];
        for (int j = 0; j < s.length; j++) {
            s[j] = new StDev();
        }

        // read all the points from the file. the inner counter is the
        // dimension since each point is written on its own line. all
        // values are piped through the statistical gatherer so that we
        // can extract information from it afterwards
        for (int i = 0; i < numOfPoints; i++) {
            for (int j = 0; j < xyz.length; j++) {
                xyz[j][i] = s[j].f(scanner.nextDouble());
            }
            /*
             * System.out.printf("x=%f, y=%f, z=%f%n", xyz[X][i], xyz[Y][i],
             * xyz[Z][i]);
             */
        }

        // d contains a mapping from the logical axis to the physical
        // axis so to speak. we look over the physical dimensions and
        // ignore the one that does not have any significant variation
        // in its data (this is a fixed coordinate for that plane, e.g.
        // may y = 5.0 for all the data points. j is the running counter
        // of active dimensions discovered.
        // another way of looking at this is to see the coordinate system
        // being rotated so that the dimension that doesn't have any
        // variation always becomes the z-value.
        int[] d = new int[DIMS_IN_PLANE];
        int j = 0;

        for (int i = 0; i < s.length; i++) {
            if (s[i].get() > Tolerances.smallEps) {
                // error detection
                if (j == d.length) {
                    break;
                }
                d[j++] = i;
            }
        }

        // if we didn't single out exactly one variable then raise an
        // error since we aren't able to use the 2D-solver on a problem
        // that now suddenly has become 3D.
        if (j != DIMS_IN_PLANE) {
            /*
             * System.out.printf("sx=%f, sy=%f, sz=%f%n", sx.stDev(),
             * sy.stDev(), sz.stDev());
             */
            throw TriExc.POINTS_NOT_IN_PLANE.create();
        }

        // return the data values and the map that tells which indices
        // that actually contains any data
        return new Pair<double[][], int[]>(xyz, d);
    }
    
    /**
     * First point that WE have written (assuming nothing about the indices
     * returned by the handler); used in boundary detected.
     */
    private Integer startIndex = null;

    /**
     * Notify a connected point handler about the points that has earlier been
     * discovered.
     * 
     * @param pointHandler
     *            Sink that will receive the points.
     * @param xyz
     *            Array consisting of one array for each physical dimensions
     *            containing its data series of basis values.
     * @param d
     *            Array consisting of one index for each logical dimension that
     *            describes which physical dimension in the above array that
     *            should be used.
     * @return Translation of points from the source's index to the index used
     *         in the sink. This map should be sent to the fracture reader so it
     *         can pass the sink only values it understands
     */
    private Map<Integer, Integer> parsePoints(PointHandler pointHandler,
            double[][] xyz, int[] d) throws TriExc {
        // we know the number of points that are supposed to be read, so
        // we optimize the map for that particular configuration
        Map<Integer, Integer> pointMap = new Hashtable<Integer, Integer>(
                numOfPoints, 1.f);

        // extract the series corresponding to each of the two logical
        // dimensions that we have (we're ignoring the third dimensions
        // which has no variation anyway).
        double[] x = xyz[d[X]];
        double[] y = xyz[d[Y]];

        // give the sink a chance to setup its structures
        pointHandler.prepareForPoints(numOfPoints);

        // raise an event for each point that has been found. note that
        // only the first two dimensions are really used.
        for (int i = 0; i < numOfPoints; i++) {
            int index = pointHandler.onPoint(x[i], y[i], 0d);
            pointMap.put(i + 1, index);
            /*
             * System.out.printf("x=%f, y=%f, z=%f%n", x[i], y[i], 0d);
             */
            if (startIndex == null) {
                startIndex = new Integer(index);
            }
        }

        // we're done writing points
        pointHandler.closePoints();
        return pointMap;
    }

    /**
     * The first lines in the file are always the boundary. Since the indices
     * start at one, then we are on the boundary until we see this index as the
     * destination again.
     */
    private Kind onBoundary = Kind.BOUNDARY; // true

    /**
     * Read the edges/fractures from the plane. Each edge is processed as we
     * read it to keep the working set down (we don't need any statistics about
     * the series)
     * 
     * @param fractureHandler
     *            Destination structure into which we are reading the edges we
     *            find in the file.
     * @param pointMap
     *            Translation of points from the source's index to the index
     *            used in the sink. This map should be sent to the fracture
     *            reader so it can pass the sink only values it understands
     * @param d
     *            Array consisting of one index for each logical dimension that
     *            describes which physical dimension in the above array that
     *            should be used.
     */
    private void readFractures(FractureHandler fractureHandler,
            Map<Integer, Integer> pointMap, int d[]) throws TriExc {
        // notify the sink about the number of fractures we intend to send
        fractureHandler.prepareForFractures(numOfEdges);
        
        // read all the edges. an edge is defined as the line beween two
        // points
        int[] abc = new int[NUM_OF_DIMS];
        for (int i = 0; i < numOfEdges; i++) {
            // read a line for each of the points. the subplanes have
            // three points for each fracture, whereas two of them will
            // always be the same.
            for (int j = 0; j < abc.length; j++) {
                abc[j] = scanner.nextInt();
            }

            // translate each point from our index into the domain that
            // the sink understands; sink is only fed with values it has
            // created itself
            for (int j = 0; j < abc.length; j++) {
                abc[j] = pointMap.get(abc[j]);
            }

            // map from physical dimensions to logical dimensions
            int a = abc[d[X]];
            int b = abc[d[Y]];

            // raise an event inserting the fracture into the structure
            fractureHandler.onFracture(a, b, onBoundary);

            // target is assumed to be the second point here. the logical
            // dimensions are always in the same order as they were in
            // the file, never swapped. when we find the starting point
            // as the destination then we have completed an entire cycle
            // and we start reading points that are not on the boundary
            if (b == startIndex) {
                onBoundary = Kind.REGULAR; // false
            }
            /*
             * System.out.printf("a=%d, b=%d%n", a, b, c);
             */
        }
                
        // done reading edges
        fractureHandler.closeFractures();
    }

    /**
     * Parse a subplane file into a collection of points and edges. (Triangles
     * are not generated here; we'll have to invoke an external program on the
     * intermediate output to do that).
     * 
     * @param pointHandler
     *            Sink that will receive all the points.
     * @param fractureHandler
     *            Sink that will receive all the fractures. This may be the same
     *            as the one that receives the points.
     */
    public void readAll(PointHandler pointHandler,
            FractureHandler fractureHandler) throws TriExc {
        // initialize the key figures about the size
        readHeader();

        // get the points into an internal array, as well as information
        // about which dimensions that should be used
        Pair<double[][], int[]> p = readPoints();
        double[][] xyz = p.x();
        int d[] = p.y();

        // send the points to the sink, passing information about which
        // parts of the data series that should be passed along
        Map<Integer, Integer> pointMap = parsePoints(pointHandler, xyz, d);

        // read the rest of the file consisting of the fractures, passing
        // along the mapping information from earlier reading
        readFractures(fractureHandler, pointMap, d);
    }

    /**
     * Close the input file from which we were reading.
     */
    public void close() {
        scanner.close();
    }
}