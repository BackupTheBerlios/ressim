package no.uib.cipr.rs.fluid;

import java.io.FileNotFoundException;
import java.io.IOException;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.util.Configuration;

/**
 * PVT calculation application
 */
public class PVT {

    public static void main(String[] args) throws FileNotFoundException,
            IOException {
        System.out.println("\tPVT property calculation\n");

        Paths.checkPresence(Paths.PVT_FILE);
        Configuration config = new Configuration(Paths.PVT_FILE);

        Components components = new Components(config);
        EquationOfState eos = EquationOfState.create(config, components);

        double p = config.getDouble("Pressure");
        double T = config.getDouble("Temperature");
        Composition N = new Composition(config, components);

        final double delta = config.getDouble("Delta", 1e-5);
        if (delta < 0 || delta > 1)
            throw new IllegalArgumentException(
                    "Delta must be between zero and one");

        boolean verify = config.getBoolean("verify", false);

        config.ensureEmpty();

        /*
         * Calculate base-case, and output
         */

        PhaseData<EquationOfStateData> base = allocateEOS(components);

        eos.calculatePhaseState(p, N, T, base);

        System.out.println("\n---------------------------------------");
        System.out.println("\tPVT properties");
        System.out.println("---------------------------------------\n");

        for (Phase phase : Phase.values())
            System.out.println(base.get(phase));

        /*
         * Check the phase composition derivatives
         */

        if (!verify)
            return;

        System.out.println("---------------------------------------");
        System.out.println("\tDerivative verification");
        System.out.println("---------------------------------------");

        double deltap = p * delta;
        double deltaT = T * delta;
        double[] deltaN = new double[components.numComponents()];
        for (Component nu : components)
            deltaN[nu.index()] = Math.max(delta * N.getMoles(nu), delta);

        PhaseData<EquationOfStateData> perturb;

        String format = "%-20s%15g%15g%15g\n";

        /*
         * Check the dV/dp, dV/dT, and dV/dN derivatives
         */

        // dV/dp
        printHeader();

        perturb = allocateEOS(components);
        eos.calculatePhaseState(p + deltap, N, T, perturb);

        double dVdpA = 0, dVdpN = 0;
        for (Phase phase : Phase.values()) {
            double analytical = base.get(phase).getdVdp();

            double numerical = (perturb.get(phase).getVolume() - base
                    .get(phase).getVolume())
                    / deltap;

            System.out.format(format, "dV(" + phase + ")/dp", analytical,
                    numerical, Math.abs(analytical - numerical));

            dVdpA += analytical;
            dVdpN += numerical;
        }

        System.out.format(format, "dV(SUM)/dp", dVdpA, dVdpN, Math.abs(dVdpA
                - dVdpN));

        if (dVdpA > 0 || dVdpN > 0)
            System.out
                    .println("\nThe PVT data is inconsistent. dV(SUM)/dp cannot be positive");

        // dV/dT
        printHeader();

        perturb = allocateEOS(components);
        eos.calculatePhaseState(p, N, T + deltaT, perturb);

        for (Phase phase : Phase.values()) {
            double analytical = base.get(phase).getdVdT();

            double numerical = (perturb.get(phase).getVolume() - base
                    .get(phase).getVolume())
                    / deltaT;

            System.out.format(format, "dV(" + phase + ")/dT", analytical,
                    numerical, Math.abs(analytical - numerical));
        }

        // dV/dN
        printHeader();
        Composition Nd = new Composition(components);
        for (Component nu : components) {
            Nd.set(N);
            Nd.addMoles(nu, deltaN[nu.index()]);

            perturb = allocateEOS(components);
            eos.calculatePhaseState(p, Nd, T, perturb);

            for (Phase phase : Phase.values()) {
                double analytical = base.get(phase).getdVdN(nu);

                double numerical = (perturb.get(phase).getVolume() - base.get(
                        phase).getVolume())
                        / deltaN[nu.index()];

                System.out.format(format, "dV(" + phase + ")/dN(" + nu.name()
                        + ")", analytical, numerical, Math.abs(analytical
                        - numerical));
            }
        }

        /*
         * Check the heat capacity against the enthalpy
         */

        printHeader();
        eos.calculatePhaseState(p, N, T + deltaT, perturb);
        for (Phase phase : Phase.values()) {
            double analytical = base.get(phase).getHeatCapacity();
            double numerical = (perturb.get(phase).getEnthalpyDensity() - base
                    .get(phase).getEnthalpyDensity())
                    / deltaT;

            System.out.format(format, "dh(" + phase + ")/dT", analytical,
                    numerical, Math.abs(analytical - numerical));
        }
    }

    private static void printHeader() {
        String headerFormat = "\n%-20s%15s%15s%15s\n";
        System.out.format(headerFormat, "Derivative", "Analytical",
                "Numerical", "Difference");
    }

    private static PhaseData<EquationOfStateData> allocateEOS(
            Components components) {
        PhaseData<EquationOfStateData> data = new PhaseData<EquationOfStateData>();
        for (Phase phase : Phase.values())
            data.set(phase, new EquationOfStateData(components, phase));
        return data;
    }

}
