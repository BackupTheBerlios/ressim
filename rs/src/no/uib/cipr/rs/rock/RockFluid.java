package no.uib.cipr.rs.rock;

import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.util.Configuration;

/**
 * Rock/fluid interaction parameters (relative permeability and capillary
 * pressures)
 */
public abstract class RockFluid {

    /**
     * Calculates all the relative permeabilities
     * 
     * @param S
     *                Phase saturations
     * @param el
     *                Element to calculate in
     * @param kr
     *                Relative permeabilities
     */
    public abstract void calculateRelativePermeability(PhaseDataDouble S,
            Element el, PhaseDataDouble kr);

    /**
     * Calculates the oil/water capillary pressure
     * 
     * @param S
     *                Phase saturations
     * @param el
     *                Element to calculate in
     * @return pcow [Pa]
     */
    public abstract double calculateOilWaterCapillaryPressure(
            PhaseDataDouble S, Element el);

    /**
     * Calculates the gas/oil capillary pressure
     * 
     * @param S
     *                Phase saturations
     * @param el
     *                Element to calculate in
     * @return pcgo [Pa]
     */
    public abstract double calculateGasOilCapillaryPressure(PhaseDataDouble S,
            Element el);

    /**
     * Creates a rock/fluid instance from the given configuration. Defaults to a
     * two-phase model
     */
    public static RockFluid create(Configuration config, String name) {
        return config.getObject(name, RockFluid.class, TwoPhaseRockFluid.class);
    }
}
