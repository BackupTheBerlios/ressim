package no.uib.cipr.rs.meshgen.triangle;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Setup the triangularization problem by receiving points from the subplanes
 * generated by another source, such as Frac3D.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class ProblemSetup implements PointHandler, FractureHandler, Closeable {
    /**
     * Destination file to which all the output is written.
     */
    private PrintWriter out;

    /**
     * Name of the file we are writing. Needed so we can pass it to the external
     * program.
     */
    private String fileName;

    /**
     * Get the name of the file that was written. This method can be called even
     * after the input has been closed (which is when the file becomes available
     * to others)
     */
    public String name() {
        return fileName;
    }

    /**
     * Construct a new input file to the triangularizer.
     * 
     * @param stem
     *            First part of the filename that is used to hold the
     *            information. This should be the same as is used in later
     *            handlers.
     * @throws TriExc
     *             If the intermediate file which is sent to the external
     *             program could not be created.
     */
    public ProblemSetup(String stem) throws TriExc {
        // input files to the Triangle program are on the PLSG format
        // and have the default extension of poly for polygon, since
        // that are the shapes of the constraints
        fileName = (stem.endsWith(".poly") ? stem : stem + ".poly");

        // open the output file for writing, readying it for the output
        // from the SubplaneParser
        try {
            out = new PrintWriter(new FileWriter(fileName));
        } catch (IOException ioe) {
            throw TriExc.CANNOT_OPEN_OUTPUT.create(ioe, fileName);
        }
    }

    /**
     * Close the underlaying stream.
     */
    public void close() {
        out.close();
    }

    /**
     * Number of points written to file. Doubles as the current index of the
     * point being written right now since the index is one-based.
     */
    private int numOfPointsWritten = 0;

    private int numOfPointsExpected;

    public void prepareForPoints(int count) throws TriExc {
        // note the number of points we are supposed to receive so we
        // can compare notes afterwards and see if we got the number
        // that was expected.
        numOfPointsExpected = count;

        // header for vertices. first comes the number of points that
        // follows, then comes the number of dimensions which is currently
        // locked to two for the Triangle program, followed by the number
        // of attributes we wish to write for each point (we don't have
        // any yet) and finally a flag that tells us if there are boundary
        // marks for each point. we don't need any boundary marks since
        // the output of the Triangle program will tell us if an edge is
        // along the boundary or not.
        out.printf("%d 2 0 0%n", count);
    }

    public int onPoint(double x, double y, double z) throws TriExc {
        // we are adding another point. the new count will also be the
        // index of this point. this should be the only place in the
        // object where this member is mutated
        int index = ++numOfPointsWritten;

        // write the declaration of the point with a given index using
        // only the first two coordinates since only two-dimensional
        // problems are currently allowed by the Triangle program.
        out.printf("%d %f %f%n", index, x, y);

        // return our identifier for this point (will be used when we're
        // writing the fractures
        return index;
    }

    public void closePoints() throws TriExc {
        // check that we actually got the number of points we were told
        if (numOfPointsWritten != numOfPointsExpected) {
            throw TriExc.POINT_MISMATCH.create(numOfPointsExpected,
                    numOfPointsWritten);
        }

        // space between sections in the file (to increase readability)
        out.printf("%n");
    }

    /**
     * Number of constraining edges written to file. Also used as the index of
     * each edge since they are one-based.
     */
    private int numOfEdgesWritten = 0;

    private int numOfEdgesExpected;

    public void prepareForFractures(int count) throws TriExc {
        // take note of the number of fractures we are promised
        numOfEdgesExpected = count;

        // edges header. first comes the number of edges, then comes a
        // flag that indicates if we bother to write edges or not. we
        // don't NEED them, the external program is capable of figuring
        // them ou as part of the algorithm, but we use them to propagate
        // information about the edges inserted as a result of the
        // partitioning.
        out.printf("%d 1%n", count);
    }

    public void onFracture(int a, int b, Kind kind) throws TriExc {
        // we've gotten one more edge in the file
        int index = ++numOfEdgesWritten;

        // declare the edge to the file using the index of the source
        // and destination points (the edge is undirected, so it doesn't
        // matter which order they come in).
        out.printf("%d %d %d %d%n", index, a, b, kind.toMarker());
    }

    public void closeFractures() throws TriExc {
        // we're not getting any more factures; make sure that we got
        // all the fractures that we were expecting
        if (numOfEdgesWritten != numOfEdgesExpected) {
            throw TriExc.FRACTURE_MISMATCH.create(numOfEdgesWritten,
                    numOfEdgesExpected);
        }

        // an extra line to separate the line constraints from the holes,
        // this marks the end of the edge contraints section
        out.printf("%n");

        // holes. we don't have any. our universe is a set of fractures
        // that are all within the same bounding box.
        out.printf("0%n");
    }
}