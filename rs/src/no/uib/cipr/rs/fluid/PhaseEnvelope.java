package no.uib.cipr.rs.fluid;

import java.io.FileNotFoundException;
import java.io.IOException;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.util.Configuration;

/**
 * Simple phase-envelope calculator
 */
public class PhaseEnvelope {

    public static void main(String[] args) throws FileNotFoundException,
            IOException {
        System.out.println("\tPVT property calculation\n");

        Paths.checkPresence(Paths.PVT_FILE);
        Configuration config = new Configuration(Paths.PVT_FILE);

        Components components = new Components(config);
        EquationOfState eos = EquationOfState.create(config, components);

        Composition N = new Composition(config, components);

        // Initial pressure and temperature
        double p0 = config.getDouble("Pressure", 1e+6);
        double maxP = config.getDouble("MaximumPressure", 1e+8);
        double dP = config.getDouble("DP", 1e+5);

        double T0 = config.getDouble("Temperature", 300);
        double maxT = config.getDouble("MaximumTemperature", 600);
        double dT = config.getDouble("DT", 2.5);

        /*
         * Start the iteration
         */

        System.out.println();

        for (double p = maxP; p >= p0; p -= dP) {
            for (double T = T0; T <= maxT; T += dT) {
                PhaseData<EquationOfStateData> data = allocateEOS(components);
                eos.calculatePhaseState(p, N, T, data);

                boolean oil = data.oil.isPresent();
                boolean gas = data.gas.isPresent();
                char state = ' ';
                if (oil && gas)
                    state = '2';
                else if (oil)
                    state = 'o';
                else if (gas)
                    state = 'g';

                System.out.print(state);
            }
            System.out.println();
        }

    }

    private static PhaseData<EquationOfStateData> allocateEOS(
            Components components) {
        PhaseData<EquationOfStateData> data = new PhaseData<EquationOfStateData>();
        for (Phase phase : Phase.values())
            data.set(phase, new EquationOfStateData(components, phase));
        return data;
    }
}
