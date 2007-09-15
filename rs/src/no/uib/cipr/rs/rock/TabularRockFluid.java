package no.uib.cipr.rs.rock;

import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.ConstantValue;
import no.uib.cipr.rs.util.Function;

/**
 * Tabular relations for calculating rock/fluid properties. Uses Baker's
 * interpolation method to determine the three-phase oil relative permeability
 */
public class TabularRockFluid extends RockFluid {

    /**
     * Water relative permeability as a function of the water saturation [-]
     */
    private final Function krw;

    /**
     * Gas relative permeability as a function of the gas saturation [-]
     */
    private final Function krg;

    /**
     * Oil/water relative permeability as a function of the oil saturation [-]
     */
    private final Function krow;

    /**
     * Oil/gas relative permeability as a function of the liquid (oil+water)
     * saturation [-]
     */
    private final Function krog;

    /**
     * Oil/water capillary pressure as a function of water saturation [Pa]
     */
    private final Function pcow;

    /**
     * Gas/oil capillary pressure as a function of gas saturation [Pa]
     */
    private final Function pcgo;

    public TabularRockFluid(Configuration config) {
        System.out.println(config.trace() + "Tabular properties");

        krw = getRelPermTable(config, "krw");
        krg = getRelPermTable(config, "krg");
        krow = getRelPermTable(config, "krow");
        krog = getRelPermTable(config, "krog");

        // Default is zero capillary pressure
        pcow = config.getFunction("pcow", 1, ConstantValue.class, 0.);
        pcgo = config.getFunction("pcgo", 1, ConstantValue.class, 0.);
    }

    private Function getRelPermTable(Configuration config, String name) {
        Function kr = config.getFunction(name, 1);

        if (kr.minOutput() < 0 || kr.maxOutput() > 1)
            throw new IllegalArgumentException(config.trace() + name
                    + " must have values between 0 and 1");

        return kr;
    }

    @Override
    public void calculateRelativePermeability(PhaseDataDouble S, Element el,
            PhaseDataDouble kr) {
        kr.water = krw.get(S.water);
        kr.gas = krg.get(S.gas);

        // Water must always be present
        if (S.water <= 0)
            throw new RuntimeException("Water is not present");

        kr.oil = (S.gas * krog.get(S.oil + S.water) + S.water * krow.get(S.oil))
                / (S.gas + S.water);
    }

    @Override
    public double calculateOilWaterCapillaryPressure(PhaseDataDouble S,
            Element el) {
        return pcow.get(S.water);
    }

    @Override
    public double calculateGasOilCapillaryPressure(PhaseDataDouble S, Element el) {
        return pcgo.get(S.gas);
    }
}
