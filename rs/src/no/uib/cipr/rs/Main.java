package no.uib.cipr.rs;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import no.uib.cipr.rs.fluid.PVT;
import no.uib.cipr.rs.numerics.PotentialCalculator;
import no.uib.cipr.rs.output.GMVExport;
import no.uib.cipr.rs.output.GMVFieldExport;

/**
 * Entry point for the different parts of the simulation system. Invokes the
 * grid generator, simulator, upscaling, or visualization
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.printf(
                "%n\tCIPR in-house reservoir simulation suite %s%n%n",
                modified());

        String format = "%-10s";
        String mesh = String.format(format, Paths.MESH_FILE);
        String mesh_gmv = String.format(format, Paths.MESH_FILE + "_gmv");
        String run = String.format(format, Paths.RUN_FILE);
        String pvt = String.format(format, Paths.PVT_FILE);
        String upscale = String.format(format, Paths.UPSCALE_FILE);
        String run_gmv = String.format(format, Paths.RUN_FILE + "_gmv");
        String potential = String.format(format, Paths.POTENTIAL_FILE);

        if (args.length < 1) {
            System.err.println("Usage:\n");
            System.err.println("\t" + mesh + "- Generate a mesh");
            System.err
                    .println("\t" + mesh_gmv + "- Visualize a mesh using GMV");
            System.err.println("\t" + run + "- Run a simulation");
            System.err.println("\t" + pvt + "- Calculate PVT properties");
            System.err.println("\t" + upscale + "- Perform upscaling");
            System.err.println("\t" + run_gmv
                    + "- Visualize simulation results using GMV");
            System.err.println("\t" + potential + "- Potential calculations");
            System.err.println();
            System.exit(1);
        }

        // Use args[0] as the application to run
        String mode = args[0];

        // Store args[1 ... n] in appArgs
        String[] appArgs = Arrays.asList(args).subList(1, args.length).toArray(
                new String[0]);

        if (mode.equals(mesh.trim()))
            no.uib.cipr.rs.meshgen.Main.main(appArgs);
        else if (mode.equals(mesh_gmv.trim()))
            GMVExport.main(appArgs);
        else if (mode.equals(run.trim()))
            no.uib.cipr.rs.numerics.Main.main(appArgs);
        else if (mode.equals(pvt.trim()))
            PVT.main(appArgs);
        else if (mode.equals(upscale.trim()))
            no.uib.cipr.rs.upscale.Main.main(appArgs);
        else if (mode.equals(run_gmv.trim()))
            GMVFieldExport.main(appArgs);
        else if (mode.equals(potential.trim()))
            PotentialCalculator.main(appArgs);
        else
            System.err.println("Unknown mode: '" + mode + "'");
    }

    private static String modified() throws Exception {
        // find the name of the class in which this method is located by the
        // stack trace this this location (we don't need to throw the exception
        // to get the location). there must always be at least one element since
        // someone called us.
        Exception e = new Exception();
        StackTraceElement[] trace = e.getStackTrace();
        String name = trace[1].getClassName();

        // find from where the class was loaded; this is the file we need to
        // inspect to find the date this class was last modified
        String path = '/' + name.replace('.', '/') + ".class";
        Class<?> c = Class.forName(name);
        URL url = c.getResource(path);

        // we need different code depending on the source from which the class
        // was loaded; archive entries are not proper files (although they look
        // that way through a virtual file system -- aargh).
        String protocol = url.getProtocol();
        if (protocol.equals("jar")) {
            // look into the archive to find the timestamp of the class
            // (unfortunately, there doesn't seem to be a good way of
            // determining the date of the jar itself unless one uses
            // url.getFile()).
            JarURLConnection juc = (JarURLConnection) url.openConnection();

            // files can only be constructed from identifiers, not locators
            // (good luck in spotting the difference).
            URI uri = new URI(juc.getJarFileURL().toString());
            File f = new File(uri);
            long lastModified = f.lastModified();
            return String.format("(%1$tF %1$tR %1$tZ)", new Date(lastModified));
        } else {
            return "";
        }
    }
}
