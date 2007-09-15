package no.uib.cipr.rs.rock;

import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.ConstantValue;
import no.uib.cipr.rs.util.Function;

/**
 * Tabular relations for calculating rock/fluid properties where the tables are
 * functions of two saturations (water and gas).
 */
public class ThreePhaseTabularRockFluid extends RockFluid {

    /**
     * Water relative permeability [-]
     */
    private final Function krw;

    /**
     * Gas relative permeability [-]
     */
    private final Function krg;

    /**
     * Oil relative permeability [-]
     */
    private final Function kro;

    /**
     * Oil/water capillary pressure [Pa]
     */
    private final Function pcow;

    /**
     * Gas/oil capillary pressure [Pa]
     */
    private final Function pcgo;

    public ThreePhaseTabularRockFluid(Configuration config) {
        System.out.println(config.trace() + "Three-phase tabular properties");

        krw = getRelPermTable(config, "krw");
        krg = getRelPermTable(config, "krg");
        kro = getRelPermTable(config, "kro");

        // Default is zero capillary pressure
        pcow = config.getFunction("pcow", 2, ConstantValue.class, 0.);
        pcgo = config.getFunction("pcgo", 2, ConstantValue.class, 0.);
    }

    private Function getRelPermTable(Configuration config, String name) {
        Function kr = config.getFunction(name, 2);

        if (kr.minOutput() < 0 || kr.maxOutput() > 1)
            throw new IllegalArgumentException(config.trace() + name
                    + " must have values between 0 and 1");

        return kr;
    }

    @Override
    public double calculateGasOilCapillaryPressure(PhaseDataDouble S, Element el) {
        return pcgo.get(S.water, S.gas);
    }

    @Override
    public double calculateOilWaterCapillaryPressure(PhaseDataDouble S,
            Element el) {
        return pcow.get(S.water, S.gas);
    }

    @Override
    public void calculateRelativePermeability(PhaseDataDouble S, Element el,
            PhaseDataDouble kr) {
        kr.water = krw.get(S.water, S.gas);
        kr.gas = krg.get(S.water, S.gas);
        kr.oil = kro.get(S.water, S.gas);
    }
}
