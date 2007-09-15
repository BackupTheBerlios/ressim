package no.uib.cipr.rs.meshgen.triangle;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * Common functionality for all type of data that are stored in the Triangle
 * input/output data format.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
abstract class ParserBase {
    // helper member that parse text into tokens (numbers)
    protected Scanner scanner;

    // subclasses will have to override this to determine which input
    // file that should be read
    protected abstract String suffix();

    /**
     * Read points from a file with the given stem. The extension added by
     * Triangle is automatically added. Readers for higher-level objects (such
     * as triangles) must be provided with a way to map the corners to actual
     * coordinates (i.e. you probably have to setup a PointParser also).
     * 
     * @param stem
     *            Base part of the file name that contains the points from the
     *            triangularization.
     * @param refinement
     *            Level of refinement performed on the triangularization (i.e.
     *            how many times have 'triangle -rqaX stem.I' been run); the
     *            default is one (output after first iteration -- no refinement)
     */
    public ParserBase(String stem, int refinement) throws TriExc {
        // read from the output file of Triangle instead of the input
        // file since the algorithm may have added extra points
        String fileName = String.format("%s.%d.%s", stem, refinement, suffix());
        try {
            // store the source so that we know from where to read later
            this.scanner = new Scanner(new FileReader(fileName));
        } catch (FileNotFoundException fnf) {
            throw TriExc.FILE_NOT_FOUND.create(fnf, fileName);
        }

        // delimiters are either whitespace in form of blank and/or tab,
        // or a hash character and throughout the rest of the line
        scanner.useDelimiter("(?:[ \t\r\n]+)|(?:#.*\r?\n)");
    }

    /**
     * We have read what we expected from the content so far on the line; the
     * rest of the line has no interest to us, so we skip it.
     */
    protected void skipRestOfLine() {
        scanner.skip(".*\r?\n");
    }

    /**
     * Allow the reader to adopt its input stream so that one only has to close
     * the outermost object to have the operation propagate to the inner layers.
     * If the stream is designated to accept those requests (e.g. files can be
     * closed, but strings cannot). we know that Scanner implements the
     * Closeable interface so there is no point checking.
     */
    public void close() {
        scanner.close();
    }
}