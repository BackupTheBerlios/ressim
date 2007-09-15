package no.uib.cipr.rs.fluid;

import java.util.Arrays;
import java.util.Collections;

import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.ConstantValue;
import no.uib.cipr.rs.util.Function;

/**
 * Black-oil equation of state calculations for a binary hydrocarbon mixture
 */
public class BlackOilEquationOfState extends WaterEquationOfState {

    /**
     * Dew-point and bubble-point curves, functions of pressure and temperature
     */
    private final Function dewPoint, bubblePoint;

    /**
     * Molar densities as functions of pressure, composition, and temperature
     */
    private final Function oilMolarDensity, gasMolarDensity;

    /**
     * Molar enthalpies as functions of pressure, composition, and temperature
     */
    private final Function oilMolarEnthalpy, gasMolarEnthalpy;

    /**
     * Viscosities as functions of pressure, composition, and temperature
     */
    private final Function oilViscosity, gasViscosity;

    /**
     * Components belonging to the oil group
     */
    private final Component[] O;

    /**
     * Components belonging to the gas group
     */
    private final Component[] G;

    /**
     * Less than this is zero moles
     */
    private static final double zeroMoles = 1e-300;

    /**
     * Sets up the tables for the black-oil equation of state calculations
     */
    public BlackOilEquationOfState(Configuration config, Components components) {
        super(config, components);

        System.out
                .println(config.trace() + "Using black-oil equation of state");

        /*
         * Component grouping
         */

        // Oil group
        String[] oil = config.getStringArray("Oil");
        O = new Component[oil.length];
        for (int i = 0; i < oil.length; ++i)
            O[i] = components.getComponent(oil[i]);

        // Gas group
        String[] gas = config.getStringArray("Gas");
        G = new Component[gas.length];
        for (int i = 0; i < gas.length; ++i)
            G[i] = components.getComponent(gas[i]);

        // Check the groupings for consistency
        checkGroupings(config, components);

        /*
         * Mass transfer functions. Defaults to no mass transfer
         */

        dewPoint = config.getFunction("DewPoint", 2, ConstantValue.class, 0.);
        bubblePoint = config.getFunction("BubblePoint", 2, ConstantValue.class,
                0.);

        if (dewPoint.minOutput() < 0 || dewPoint.maxOutput() > 1)
            throw new IllegalArgumentException(config.trace()
                    + "Dew point curve must be between zero and one");

        if (bubblePoint.minOutput() < 0 || bubblePoint.maxOutput() > 1)
            throw new IllegalArgumentException(config.trace()
                    + "Bubble point curve must be between zero and one");

        /*
         * Molar densities
         */

        oilMolarDensity = config.getFunction("OilMolarDensity", 3);
        gasMolarDensity = config.getFunction("GasMolarDensity", 3);

        if (oilMolarDensity.minOutput() <= 0)
            throw new IllegalArgumentException(config.trace()
                    + "Oil molar density must be positive");

        if (gasMolarDensity.minOutput() <= 0)
            throw new IllegalArgumentException(config.trace()
                    + "Gas molar density must be positive");

        /*
         * Molar enthalpies. Defaults to zero, for iso-thermal runs
         */

        oilMolarEnthalpy = config.getFunction("OilMolarEnthalpy", 3,
                ConstantValue.class, 0.);
        gasMolarEnthalpy = config.getFunction("GasMolarEnthalpy", 3,
                ConstantValue.class, 0.);

        if (oilMolarEnthalpy.minOutput() < 0)
            throw new IllegalArgumentException(config.trace()
                    + "Oil molar enthalpy must be positive");

        if (gasMolarEnthalpy.minOutput() < 0)
            throw new IllegalArgumentException(config.trace()
                    + "Gas molar enthalpy must be positive");

        /*
         * Viscosities
         */

        oilViscosity = config.getFunction("OilViscosity", 3);
        gasViscosity = config.getFunction("GasViscosity", 3);

        if (oilViscosity.minOutput() <= 0)
            throw new IllegalArgumentException(config.trace()
                    + "Oil viscosity must be positive");

        if (gasViscosity.minOutput() <= 0)
            throw new IllegalArgumentException(config.trace()
                    + "Gas viscosity must be positive");
    }

    /**
     * Performs miscellaneous checks on the groupings to detect inconsistencies
     */
    private void checkGroupings(Configuration config, Components components) {
        // Check that the groups are disjoint
        if (!Collections.disjoint(Arrays.asList(O), Arrays.asList(G)))
            throw new IllegalArgumentException(config.trace()
                    + "Oil and gas groups are not disjoint");

        // Check that all components (except water) are grouped
        boolean[] mapped = new boolean[components.numComponents()];
        for (Component g : G)
            mapped[g.index()] = true;
        for (Component o : O)
            mapped[o.index()] = true;

        if (mapped[components.water().index()])
            throw new IllegalArgumentException(config.trace()
                    + "The water component may not be grouped");

        for (Component nu : components.hc())
            if (!mapped[nu.index()])
                throw new IllegalArgumentException(config.trace()
                        + "Component " + nu.name() + " has not been grouped");
    }

    @Override
    public void calculatePhaseState(double p, Composition N, double T,
            PhaseData<EquationOfStateData> data) {

        /*
         * Calculate water properties
         */

        super.calculatePhaseState(p, N, T, data);

        /*
         * Determine which phases are present
         */

        EquationOfStateData oil = data.oil;
        EquationOfStateData gas = data.gas;

        double No = getMoles(N, O);
        double Ng = getMoles(N, G);
        double NHC = No + Ng;

        // No hydrocarbon means no hydrocarbon phases
        if (NHC < zeroMoles) {
            oil.setNotPresent();
            gas.setNotPresent();
        } else {

            double Cb = 0, Cd = 0;
            double Zo = No / NHC, Zg = Ng / NHC;

            // Single-phase oil
            if (Zg <= (Cb = bubblePoint.get(p, T))) {
                oil.setPresent();
                gas.setNotPresent();

                calculateSinglePhaseOil(p, T, N, No, Ng, oil);
            }

            // Single-phase gas
            else if (Zo <= (Cd = dewPoint.get(p, T))) {
                oil.setNotPresent();
                gas.setPresent();

                calculateSinglePhaseGas(p, T, N, No, Ng, gas);
            }

            // Two-phase oil/gas
            else {
                oil.setPresent();
                gas.setPresent();

                calculateTwoPhaseOilGas(p, T, N, No, Ng, Cb, Cd, oil, gas);
            }
        }
    }

    /**
     * Calculates hydrocarbon properties for a single-phase oil case
     */
    private void calculateSinglePhaseOil(double p, double T, Composition N,
            double No, double Ng, EquationOfStateData oil) {

        // Phase composition
        inverseGroupComposition(N, No, Ng, No, Ng, oil);

        /*
         * Volume and density
         */

        double NHC = No + Ng;
        double Cgo = Ng / NHC;
        double xio = oilMolarDensity.get(p, Cgo, T);
        double Vo = (No + Ng) / xio;

        oil.setMolarDensity(xio);
        oil.setVolume(Vo);

        /*
         * Volume derivatives
         */

        double dxio_dp = oilMolarDensity.deriv(0, p, Cgo, T);
        double dxio_dC = oilMolarDensity.deriv(1, p, Cgo, T);
        double dxio_dT = oilMolarDensity.deriv(2, p, Cgo, T);

        double dCgo_dNo = -Cgo / NHC;
        double dCgo_dNg = (1 - Cgo) / NHC;

        double Vo_xio = Vo / xio;
        double dVodp = -Vo_xio * dxio_dp;
        double dVodT = -Vo_xio * dxio_dT;
        double dVodNo = (1 / xio) - Vo_xio * dxio_dC * dCgo_dNo;
        double dVodNg = (1 / xio) - Vo_xio * dxio_dC * dCgo_dNg;

        oil.setdVdp(dVodp);
        oil.setdVdT(dVodT);

        // Inverse grouping
        for (Component nu : O)
            oil.setdVdN(nu, dVodNo);
        for (Component nu : G)
            oil.setdVdN(nu, dVodNg);

        // Enthalpy and heat capacity
        calculateEnthalpy(p, T, 1, 0, xio, dxio_dT, dxio_dC, oilMolarEnthalpy,
                oil);

        // Viscosity
        calculateViscosity(p, T, Cgo, oilViscosity, oil);
    }

    /**
     * Calculates hydrocarbon properties for a single-phase gas case
     */
    private void calculateSinglePhaseGas(double p, double T, Composition N,
            double No, double Ng, EquationOfStateData gas) {

        // Phase composition
        inverseGroupComposition(N, No, Ng, No, Ng, gas);

        /*
         * Volume and density
         */

        double NHC = No + Ng;
        double Cog = No / NHC;
        double xig = gasMolarDensity.get(p, Cog, T);
        double Vg = (No + Ng) / xig;

        gas.setMolarDensity(xig);
        gas.setVolume(Vg);

        /*
         * Volume derivatives
         */

        double dxig_dp = gasMolarDensity.deriv(0, p, Cog, T);
        double dxig_dC = gasMolarDensity.deriv(1, p, Cog, T);
        double dxig_dT = gasMolarDensity.deriv(2, p, Cog, T);

        double dCgo_dNo = -(1 - Cog) / NHC;
        double dCgo_dNg = -Cog / NHC;

        double Vg_xig = Vg / xig;
        double dVgdp = -Vg_xig * dxig_dp;
        double dVgdT = -Vg_xig * dxig_dT;
        double dVgdNo = (1 / xig) - Vg_xig * dxig_dC * dCgo_dNo;
        double dVgdNg = (1 / xig) - Vg_xig * dxig_dC * dCgo_dNg;

        gas.setdVdp(dVgdp);
        gas.setdVdT(dVgdT);

        // Inverse grouping
        for (Component nu : O)
            gas.setdVdN(nu, dVgdNo);
        for (Component nu : G)
            gas.setdVdN(nu, dVgdNg);

        // Enthalpy and heat capacity
        calculateEnthalpy(p, T, 1, 0, xig, dxig_dT, dxig_dC, gasMolarEnthalpy,
                gas);

        // Viscosity
        calculateViscosity(p, T, Cog, gasViscosity, gas);
    }

    /**
     * Calculates hydrocarbon properties for a two-phase oil/gas case
     */
    private void calculateTwoPhaseOilGas(double p, double T, Composition N,
            double No, double Ng, double Cgo, double Cog,
            EquationOfStateData oil, EquationOfStateData gas) {

        /*
         * Phase composition
         */

        double NHC = No + Ng;
        double Zo = No / NHC;
        double L = (Cog - Zo) / (Cog - (1 - Cgo));

        double Noo = L * NHC * (1 - Cgo);
        double Ngo = L * NHC * Cgo;
        double Nog = (1 - L) * NHC * Cog;
        double Ngg = (1 - L) * NHC * (1 - Cog);

        inverseGroupComposition(N, No, Ng, Noo, Ngo, oil);
        inverseGroupComposition(N, No, Ng, Nog, Ngg, gas);

        /*
         * Volume and density
         */

        double xio = oilMolarDensity.get(p, Cgo, T);
        double xig = gasMolarDensity.get(p, Cog, T);

        double Vo = (Noo + Ngo) / xio;
        double Vg = (Nog + Ngg) / xig;

        oil.setMolarDensity(xio);
        gas.setMolarDensity(xig);

        oil.setVolume(Vo);
        gas.setVolume(Vg);

        /*
         * Volume derivatives
         */

        double dCgo_dp = bubblePoint.deriv(0, p, T);
        double dCog_dp = dewPoint.deriv(0, p, T);

        double dCgo_dT = bubblePoint.deriv(1, p, T);
        double dCog_dT = dewPoint.deriv(1, p, T);

        double dLdp = (1 / (Cog - (1 - Cgo)))
                * ((1 - L) * dCog_dp - L * (dCgo_dp));
        double dLdT = (1 / (Cog - (1 - Cgo)))
                * ((1 - L) * dCog_dT - L * (dCgo_dT));
        double dLdNo = (1 / (Cog - (1 - Cgo))) * ((Zo - 1) / NHC);
        double dLdNg = (1 / (Cog - (1 - Cgo))) * (Zo / NHC);

        double dxio_dp = oilMolarDensity.deriv(0, p, Cgo, T);
        double dxio_dC = oilMolarDensity.deriv(1, p, Cgo, T);
        double dxio_dT = oilMolarDensity.deriv(2, p, Cgo, T);

        double dxig_dp = gasMolarDensity.deriv(0, p, Cog, T);
        double dxig_dC = gasMolarDensity.deriv(1, p, Cog, T);
        double dxig_dT = gasMolarDensity.deriv(2, p, Cog, T);

        double dVodp = (Vo / L) * dLdp - (Vo / xio)
                * (dxio_dp + dxio_dC * dCgo_dp);
        double dVgdp = -(Vg / (1 - L)) * dLdp - (Vg / xig)
                * (dxig_dp + dxig_dC * dCog_dp);

        double dVodT = (Vo / L) * dLdT - (Vo / xio)
                * (dxio_dT + dxio_dC * dCgo_dT);
        double dVgdT = -(Vg / (1 - L)) * dLdT - (Vg / xig)
                * (dxig_dT + dxig_dC * dCog_dT);

        double dVodNo = (1 / xio) * (dLdNo * NHC + L);
        double dVodNg = (1 / xio) * (dLdNg * NHC + L);
        double dVgdNo = (1 / xig) * (-dLdNo * NHC + (1 - L));
        double dVgdNg = (1 / xig) * (-dLdNg * NHC + (1 - L));

        oil.setdVdp(dVodp);
        oil.setdVdT(dVodT);

        gas.setdVdp(dVgdp);
        gas.setdVdT(dVgdT);

        // Inverse grouping
        for (Component nu : O) {
            oil.setdVdN(nu, dVodNo);
            gas.setdVdN(nu, dVgdNo);
        }
        for (Component nu : G) {
            oil.setdVdN(nu, dVodNg);
            gas.setdVdN(nu, dVgdNg);
        }

        /*
         * Data check
         */

        // Check that the oil component prefers the oil phase
        if (Cog - (1 - Cgo) > 0)
            throw new RuntimeException(
                    "Error detected in the bubble- and dew-point functions. Must have Cog < Coo, but was "
                            + Cog
                            + " >= "
                            + (1 - Cgo)
                            + "\n"
                            + printPVT(p, T, N));

        // Check that the liquid fraction increases with increasing pressure
        if ((1 - L) * dCog_dp - L * dCgo_dp > 0)
            throw new RuntimeException(
                    "Liquid hydrocarbon fraction must increase with increasing pressure. Check dew- and bubble-point tables.\ndL/dp = "
                            + dLdp + "\n" + printPVT(p, T, N));

        // Check that the hydrocarbon mixture is compressible
        if (dVodp + dVgdp > 0)
            throw new RuntimeException(
                    "Negative hydrocarbon compressibility detected.\ndVo/dp = "
                            + dVodp + "\ndVg/dp = " + dVgdp + "\n"
                            + printPVT(p, T, N));

        // Check that adding oil increases the hydrocarbon volume
        if (dVodNo + dVgdNo <= 0)
            throw new RuntimeException(
                    "Hydrocarbon volume doesn't increase with increasing number of oil moles.\ndVo/dNo = "
                            + dVodNo
                            + "\ndVg/dNo = "
                            + dVgdNo
                            + "\n"
                            + printPVT(p, T, N));

        // Check that adding gas increases the hydrocarbon volume
        if (dVodNg + dVgdNg <= 0)
            throw new RuntimeException(
                    "Hydrocarbon volume doesn't increase with increasing number of gas moles.\ndVo/dNg = "
                            + dVodNg
                            + "\ndVg/dNg = "
                            + dVgdNg
                            + "\n"
                            + printPVT(p, T, N));

        /*
         * Enthalpy and viscosity
         */

        // Enthalpy and heat capacity
        calculateEnthalpy(p, T, Cgo, dCgo_dT, xio, dxio_dT, dxio_dC,
                oilMolarEnthalpy, oil);
        calculateEnthalpy(p, T, Cog, dCog_dT, xig, dxig_dT, dxig_dC,
                gasMolarEnthalpy, gas);

        // Viscosity
        calculateViscosity(p, T, Cgo, oilViscosity, oil);
        calculateViscosity(p, T, Cog, gasViscosity, gas);
    }

    /**
     * Inverse grouping of a phase composition
     */
    private void inverseGroupComposition(Composition N, double No, double Ng,
            double Nop, double Ngp, EquationOfStateData eos) {
        double Nt = N.getMoles();
        double Co = No / Nt, Cg = Ng / Nt;

        Composition composition = eos.getComposition();
        if (Co > 0)
            for (Component nu : O)
                composition.setMoles(nu, N.getMoleFraction(nu) * Nop / Co);
        if (Cg > 0)
            for (Component nu : G)
                composition.setMoles(nu, N.getMoleFraction(nu) * Ngp / Cg);
    }

    /**
     * Calculates enthalpy and heat capacity
     */
    private void calculateEnthalpy(double p, double T, double C, double dCdT,
            double xi, double dxidT, double dxidC, Function enthalpy,
            EquationOfStateData eos) {

        // Enthalpy density
        double hbar = enthalpy.get(p, C, T);
        eos.setEnthalpyDensity(hbar * xi);

        double dhbar_dT = enthalpy.deriv(2, p, C, T);
        double dhbar_dC = 0;
        if (dCdT != 0)
            dhbar_dC = enthalpy.deriv(1, p, C, T);

        // Heat capacity
        double c = (dhbar_dT + dhbar_dC * dCdT) * xi + hbar
                * (dxidT + dxidC * dCdT);
        eos.setHeatCapacity(c);
    }

    /**
     * Calculates viscosity
     */
    private void calculateViscosity(double p, double T, double C,
            Function viscosity, EquationOfStateData eos) {
        double mu = viscosity.get(p, C, T);
        eos.setViscosity(mu);
    }

    /**
     * Gets the total number of moles of a component group in a composition
     */
    private double getMoles(Composition N, Component[] group) {
        double moles = 0;
        for (Component nu : group)
            moles += N.getMoles(nu);
        return moles;
    }
}
