package no.uib.cipr.rs.numerics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.field.Field;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.util.Configuration;

/**
 * Main entry point of the reservoir flow simulator
 */
public class Main {

    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
        System.out.println("\tReservoir flow simulator\n");

        if (args.length > 1) {
            System.err.println("Input arguments: [restart time]");
            System.exit(1);
        }

        String restartTime = (args.length == 1) ? args[0] : null;

        Paths.checkPresence(Paths.RUN_FILE);
        Configuration config = new Configuration(Paths.RUN_FILE);

        // Numerical parameters
        RunSpec runSpec = new RunSpec(config, restartTime);

        // Read in the mesh, and output its size
        Mesh mesh = Paths.readMesh();
        System.out.print(mesh);

        /*
         * System field state
         */

        Field field = null;

        // Simulation restart
        if (runSpec.restart()) {

            // Check that the restart file is present
            Paths.checkPresence(Paths.SIMULATION_OUTPUT + "/"
                    + runSpec.restartTime());

            // Restart file name: simulation/restartTime
            String file = String.format("%s/%s", Paths.SIMULATION_OUTPUT,
                    runSpec.restartTime());

            // Read in the restart data
            ObjectInput in = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(file)));
            Field restartField = (Field) in.readObject();
            in.close();

            // Create a new field, using only the restart primary data
            field = new Field(restartField, config, mesh, runSpec.isThermal());
        }

        // Starting from scratch
        else
            field = new Field(config, mesh, runSpec.isThermal());

        // Check that we're done with the configuration file
        config.ensureEmpty();

        // Set up discretization and the timestepper
        Discretisation discretisation = new Discretisation(field, runSpec);
        TimeStepper timeStepper = new TimeStepper(runSpec, field,
                discretisation);

        /*
         * Run the simulation
         */

        for (double time : runSpec.getReportTimes()) {
            if (!timeStepper.stepTo(time)) {
                System.err.println("\n\tSimulation is stopping ...");
                return;
            }

            // User-time
            double t = runSpec.getTimeUnit().inSeconds(field.getTime());

            // Save to "simulation/time"
            File file = new File(Paths.SIMULATION_OUTPUT, Paths.restart(t));

            System.out.println("\n\tSaving to output file ... ");

            // Use serialization to save
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(file));
            out.writeObject(field);
            out.close();
        }
    }
}
