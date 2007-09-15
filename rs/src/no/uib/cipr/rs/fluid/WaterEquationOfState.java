package no.uib.cipr.rs.fluid;

import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.ConstantValue;
import no.uib.cipr.rs.util.Function;

/**
 * Equation of state calculations for the water phase
 */
abstract class WaterEquationOfState extends EquationOfState {

    /**
     * Molar density of water as a function of pressure and temperature
     */
    private final Function waterMolarDensity;

    /**
     * Molar enthalpy of water as a function of pressure and temperature
     */
    private final Function waterMolarEnthalpy;

    /**
     * Viscosity of water as a function of pressure and temperature
     */
    private final Function waterViscosity;

    /**
     * The water component
     */
    private final Component w;

    /**
     * Reads in the tables for the water equation of state
     */
    public WaterEquationOfState(Configuration config, Components components) {

        w = components.water();

        /*
         * Molar density
         */

        waterMolarDensity = config.getFunction("WaterMolarDensity", 2);

        if (waterMolarDensity.minOutput() <= 0)
            throw new IllegalArgumentException(config.trace()
                    + "Water molar density must be positive");

        /*
         * Molar enthalpy. Defaults to zero for iso-thermal cases
         */

        waterMolarEnthalpy = config.getFunction("WaterMolarEnthalpy", 2,
                ConstantValue.class, 0.);

        if (waterMolarEnthalpy.minOutput() < 0)
            throw new IllegalArgumentException(config.trace()
                    + "Water molar enthalpy must be positive");

        /*
         * Viscosity
         */

        waterViscosity = config.getFunction("WaterViscosity", 2);

        if (waterViscosity.minOutput() <= 0)
            throw new IllegalArgumentException(config.trace()
                    + "Water viscosity must be positive");
    }

    @Override
    public void calculatePhaseState(double p, Composition N, double T,
            PhaseData<EquationOfStateData> data) {
        EquationOfStateData water = data.water;

        double Nw = N.getMoles(w);

        if (Nw > 0) {
            water.setPresent();

            /*
             * Phase composition
             */

            double xiw = waterMolarDensity.get(p, T);
            double Vw = Nw / xiw;

            water.setMolarDensity(xiw);
            water.setVolume(Vw);

            Composition NW = water.getComposition();
            NW.setMoles(w, Nw);

            /*
             * Volume derivatives
             */

            double dxiw_dp = waterMolarDensity.deriv(0, p, T);
            double dxiw_dT = waterMolarDensity.deriv(1, p, T);

            if (dxiw_dp < 0)
                throw new RuntimeException(
                        "Water molar density pressure derivative cannot be negative, but it is "
                                + dxiw_dp + "\n" + printPVT(p, T, N));

            water.setdVdp(-(Vw / xiw) * dxiw_dp);
            water.setdVdT(-(Vw / xiw) * dxiw_dT);
            water.setdVdN(w, 1 / xiw);

            /*
             * Enthalpy
             */

            double hw = waterMolarEnthalpy.get(p, T);
            water.setEnthalpyDensity(hw * xiw);

            double dhw_dT = waterMolarEnthalpy.deriv(1, p, T);
            water.setHeatCapacity(dhw_dT * xiw + hw * dxiw_dT);

            /*
             * Viscosity
             */

            double muw = waterViscosity.get(p, T);
            water.setViscosity(muw);

        } else
            water.setNotPresent();
    }

    String printPVT(double p, double T, Composition N) {
        StringBuilder string = new StringBuilder();

        string.append(String.format("%-12s%15g\n", "pressure", p));
        string.append(String.format("%-12s%15g\n", "temperature", T));
        string.append(N.toString());

        return string.toString();
    }
}
