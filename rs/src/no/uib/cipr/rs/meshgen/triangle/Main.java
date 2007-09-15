package no.uib.cipr.rs.meshgen.triangle;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.util.Configuration;

public class Main {
    /**
     * Driver routine to do the triangularization before reading the mesh into
     * the simulator.
     * 
     * @param args
     *            First argument has to be the name of the file containing the
     *            subplane, e.g. "Subplane3D_000.dat". The second argument is
     *            the stem of the output file, e.g. "mesh" will generate a file
     *            called "mesh.poly".
     */
    public static void main(String[] args) throws Exception {
        // we need two arguments to this program
        if (args.length < 3) {
            System.err
                    .printf("Synopsis: triangle.Main input mesh output%n");
            System.exit(1);
        }
        
        // list of files to be read
        String inputFile  = args[0];
        String meshFile   = args[1];
        String outputFile = args[2];

        // list of sources from which we'll read
        List<Source> sources = new ArrayList<Source>();

        // get the name of the fracture generation. choose between
        // various types of input by their extension
        if (inputFile.endsWith(".dat")) { // Tecplot
            sources.add(new SubplaneParser(inputFile));
        } else if (inputFile.endsWith(".mat")) { // Matlab
            sources.add(new MatlabParser(inputFile));
        } else if (inputFile.endsWith(".odg")) { // OpenOffice >= 2.0
            sources.add(new OpenOfficeParser(inputFile));
        } else if (inputFile.endsWith(".odp")) { // OpenOffice >= 2.0
            sources.add(new OpenOfficeParser(inputFile));
        } else if (inputFile.endsWith(".sxd")) { // OpenOffice == 1.0
            sources.add(new OpenOfficeParser(inputFile));
        } else if (inputFile.endsWith(".svg")) {
            sources.add(new SvgParser(inputFile));
        } else {
            throw TriExc.UNKNOWN_FILE_FORMAT.create(inputFile);
        }
        
        // read the configuration and extract the mesh description
        Configuration config = new Configuration(meshFile);
        Configuration partition = config.getConfiguration("PartitionDescription");
        
        // read codes for fracture types from the configuration
        Configuration generator = config.getConfiguration("MeshGenerator");               
        Kind.setupFractures(generator);

        // if a partition is specified, then include its boundaries in
        // the list of sources as well
        if (partition != null) {
            sources.add(new PartitionParser(partition));
        }

        // setup the problem by converting the subplane input to one that
        // the triangularizer understands
        ProblemSetup prob = new ProblemSetup(outputFile);
        ParallelSetup parser = new ParallelSetup(sources);
        try {
            try {
                parser.readAll(prob, prob);
            } finally {
                parser.close();
            }
        } finally {
            prob.close();
        }

        // triangularize the problem by spawning the external program
        /*
         * Process triangularizer = new ProcessBuilder("triangle", "-pq",
         * prob.name()).start(); triangularizer.waitFor();
         */
    }
}
