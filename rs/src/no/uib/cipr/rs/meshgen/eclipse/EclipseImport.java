package no.uib.cipr.rs.meshgen.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Logger;

import no.uib.cipr.rs.meshgen.eclipse.parse.EclipseMeshData;
import no.uib.cipr.rs.meshgen.eclipse.parse.Parser;

/**
 * Filter for importing an Eclipse grid specification and converting it to rs
 * mesh input file.
 * 
 * 1. find specified Eclipse input file
 * 
 * 2. recognize format of Eclipse input file; either complete or only grid
 * 
 * 3. parse input file and check for Eclipse grid type. currently we only
 * consider corner point grids (e.g. dumped from Irap RMS) or structured grids
 * specified by dx, dy, dz. Radial grids are not read at the moment.
 * 
 * 4. output the parsed grid specification to grid.inp, rs internal grid file
 * format
 * 
 * 5. finito
 */
public class EclipseImport {

    private final static Logger log = Logger.getLogger(EclipseImport.class
            .getName());

    /**
     * @param args
     */
    public static void main(String[] args) {

        log.info("starting");

        Parser parser = null;

        String caseName = null;

        try {
            checkArgs(args);

            caseName = args[0].split("\\.")[0];
            log.info(String.format("case: \"%s\"", caseName));

            parser = getEclipseParser(args[0]);

        } catch (IllegalArgumentException e) {
            System.err.println("failure: " + e.getMessage());
            return;
        }

        EclipseMeshData meshData = new EclipseMeshData(parser
                .getConfiguration("mesh"));

        // alternative way
        EclipseMesh mesh = new EclipseMesh(parser);

        MeshConfiguration testOutput = new MeshConfiguration(mesh);

        // tried and true way
        OldMeshConfiguration output = new OldMeshConfiguration(meshData);

        // output rs mesh configuration
        try {
            File outFile = new File(caseName + "-grid.inp");
            output.getConfiguration().outputConfiguration(
                    new FileOutputStream(outFile));
            log.info(String.format("output file: \"%s\"", outFile));

            File testFile = new File(caseName + "-new-grid.inp");
            testOutput.getConfiguration().outputConfiguration(
                    new FileOutputStream(testFile));

        } catch (Exception e1) {
            System.err.println("outputMesh failed: " + e1.getMessage());
        }

        log.info("finished");
    }

    /**
     * @param name
     * @return An Eclipse file format parser
     */
    private static Parser getEclipseParser(String name) {

        File input = getReadableFile(name, "EclipseData");

        try {
            return new Parser(input);
        } catch (Exception e) {
            System.err.println("Eclipse file error: " + e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns a filehandle for the named file, checking that it exists and is
     * readable
     * 
     * @param fileName
     * @param description
     * @return A readable file
     */
    private static File getReadableFile(String fileName, String description) {
        File input = new File(fileName);
        if (!input.exists()) {
            System.err.println(description + " file does not exists: \""
                    + input.getName() + "\"");
            throw new IllegalArgumentException();
        }
        if (!input.canRead()) {
            System.err.println(description + " file is unreadable: \""
                    + input.getName() + "\"");
            throw new IllegalArgumentException();
        }
        return input;
    }

    /**
     * Checks number of arguments
     * 
     * @param args
     *            Array of input strings
     */
    private static void checkArgs(String[] args) {
        // A single Eclipse input file must be specified
        if (args.length != 1) {
            System.err.println("Usage: {eclipseinput.grdecl} ");
            throw new IllegalArgumentException();
        }
    }

}
