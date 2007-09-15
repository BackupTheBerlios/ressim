package no.uib.cipr.rs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Locale;

import no.uib.cipr.rs.geometry.Mesh;

/**
 * Directory paths and file names used by the different parts of the simulation
 * system
 */
public final class Paths {

    private Paths() {
        // No need for an instance
    }

    /**
     * Simulation output directory
     */
    public static final String SIMULATION_OUTPUT = "simulation";

    /**
     * Visualization output directory
     */
    public static final String VISUALIZATION_OUTPUT = "visualization";

    /**
     * Gridding output directory
     */
    public static final String GRIDDING_OUTPUT = "gridding";

    /**
     * Name of the run-file for input to the simulator
     */
    public static final String RUN_FILE = "run";

    /**
     * Name of the input file to the potential calculator
     */
    public static final String POTENTIAL_FILE = "potential";

    /**
     * Format of the restart files as a function of the timestep.
     */
    public static String restart(double timeStep) {
        return String.format(Locale.US, "%014.6f", timeStep);
    }

    /**
     * Name of the fine mesh. The subdomain meshes have an integer suffix
     * denoting the subdomain rank
     */
    public static final String MESH_FILE = "mesh";

    /**
     * File for separate PVT calculations
     */
    public static final String PVT_FILE = "pvt";

    /**
     * File for upscaling
     */
    public static final String UPSCALE_FILE = "upscale";

    /**
     * Checks for the presence of the named file. If not found, an error message
     * is printed and the application stops
     */
    public static void checkPresence(String file) {
        File aFile = new File(file);
        if (!aFile.exists()) {
            System.err.println("File not found: '" + file + "'");
            System.exit(1);
        }
    }

    /**
     * Reads in the mesh
     */
    public static Mesh readMesh() {

        // Check existence of the mesh file
        String meshFile = Paths.GRIDDING_OUTPUT + "/" + Paths.MESH_FILE;
        Paths.checkPresence(meshFile);

        // Read in the mesh
        try {
            ObjectInput in = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(meshFile)));

            Mesh mesh = (Mesh) in.readObject();

            in.close();

            return mesh;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Filters out the simulator output directories (these are just numbers)
     */
    public static class OutputDirectoryFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            String name = pathname.getName();

            try {
                Double.valueOf(name);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        }
    }
}
