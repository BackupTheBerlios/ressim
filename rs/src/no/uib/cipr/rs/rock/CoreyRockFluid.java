package no.uib.cipr.rs.rock;

import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.util.Configuration;

/**
 * Corey relative permeabilities and Skjæveland capillary pressure. Developed
 * for two-phase flow, but here, it's simply extended to three phases.
 * 
 * For genuine three-phase flow, a Stone type correlation should be included.
 */
public class CoreyRockFluid extends RockFluid {

    /**
     * Residual saturations
     */
    private final PhaseDataDouble Sr;

    /**
     * Corey exponents
     */
    private final PhaseDataDouble n;

    /**
     * End-point relative permeabilities
     */
    private final PhaseDataDouble ke;

    /**
     * Threshold pressures [Pa]
     */
    private final double cw, cow, cg, cog;

    /**
     * Skjæveland exponents
     */
    private final double aw, aow, ag, aog;

    public CoreyRockFluid(Configuration config) {
        System.out.println(config.trace() + "Corey/Skjæveland correlations");

        /*
         * Residual saturations
         */

        Sr = new PhaseDataDouble(config.getDouble("Swi", 0), config.getDouble(
                "Sor", 0), config.getDouble("Sgr", 0));

        /*
         * Corey exponents
         */

        n = new PhaseDataDouble(config.getDouble("nw", 2), config.getDouble(
                "no", 2), config.getDouble("ng", 2));

        /*
         * End-point relative permeabilities
         */

        ke = new PhaseDataDouble(config.getDouble("kw", 1), config.getDouble(
                "ko", 1), config.getDouble("kg", 1));

        /*
         * Threshold pressures
         */

        cw = config.getDouble("cw", 0);
        cow = config.getDouble("cow", 0);
        cg = config.getDouble("cg", 0);
        cog = config.getDouble("cog", 0);

        /*
         * Skjæveland exponents
         */

        aw = config.getDouble("aw", 1);
        aow = config.getDouble("aow", 1);
        ag = config.getDouble("ag", 1);
        aog = config.getDouble("aog", 1);
    }

    @Override
    public double calculateGasOilCapillaryPressure(PhaseDataDouble S, Element el) {
        double Sg = effectiveSaturation(Phase.GAS, S.get(Phase.GAS));
        double So = effectiveSaturation(Phase.OIL, S.get(Phase.OIL));

        double pc = 0;

        if (Sg > 0)
            pc += cg / Math.pow(Sg, ag);

        if (So > 0)
            pc -= cog / Math.pow(So, aog);

        return pc;
    }

    @Override
    public double calculateOilWaterCapillaryPressure(PhaseDataDouble S,
            Element el) {
        double Sw = effectiveSaturation(Phase.WATER, S.get(Phase.WATER));
        double So = effectiveSaturation(Phase.OIL, S.get(Phase.OIL));

        double pc = 0;

        if (Sw > 0)
            pc += cw / Math.pow(Sw, aw);

        if (So > 0)
            pc -= cow / Math.pow(So, aow);

        return pc;
    }

    @Override
    public void calculateRelativePermeability(PhaseDataDouble S, Element el,
            PhaseDataDouble kr) {
        for (Phase phase : Phase.values()) {

            double Se = effectiveSaturation(phase, S.get(phase));

            kr.set(phase, ke.get(phase) * Math.pow(Se, n.get(phase)));
        }
    }

    private double effectiveSaturation(Phase phase, double S) {
        double Se = S;

        // Subtract its residual
        Se -= Sr.get(phase);

        // Divide by its maximum attainable saturation
        double denom = 1;
        for (Phase p : Phase.values())
            denom -= Sr.get(p);
        Se /= denom;

        return Se;
    }

}
