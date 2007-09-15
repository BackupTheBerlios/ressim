package no.uib.cipr.rs.rock;

import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.ConstantValue;
import no.uib.cipr.rs.util.Function;

/**
 * A simple rock/fluid model. Uses quadratic relative permeabilities, with
 * possibly residual saturations, and accepts tables of capillary pressures
 */
public class TwoPhaseRockFluid extends RockFluid {

    /**
     * Residual saturations
     */
    private final PhaseDataDouble Sr;

    /**
     * Oil/water capillary pressure as a function of water saturation [Pa]
     */
    private final Function pcow;

    /**
     * Gas/oil capillary pressure as a function of gas saturation [Pa]
     */
    private final Function pcgo;

    public TwoPhaseRockFluid(Configuration config) {
        System.out.println(config.trace() + "Two phase properties");

        Sr = new PhaseDataDouble();

        // Get the residual saturations
        for (Phase phase : Phase.all())
            Sr.set(phase, config.getDouble("S" + phase.letter() + "r", 0));

        // Default is zero capillary pressure
        pcow = config.getFunction("pcow", 1, ConstantValue.class, 0.);
        pcgo = config.getFunction("pcgo", 1, ConstantValue.class, 0.);
    }

    @Override
    public void calculateRelativePermeability(PhaseDataDouble S, Element el,
            PhaseDataDouble kr) {
        for (Phase phase : Phase.values()) {
            double Srp = Sr.get(phase);

            double Se = (S.get(phase) - Srp) / (1 - Srp);

            // Keep the saturation within [0,1]
            Se = Math.min(1, Math.max(0, Se));

            // kr = Se^2
            kr.set(phase, Se * Se);
        }
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
