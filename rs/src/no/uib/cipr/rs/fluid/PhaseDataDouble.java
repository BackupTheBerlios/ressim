package no.uib.cipr.rs.fluid;

import java.io.Serializable;

/**
 * Phase specific double precision data. Profiling showed that storing doubles
 * in a PhaseData materially reduced performance.
 */
public class PhaseDataDouble implements Serializable {

    private static final long serialVersionUID = -8974165889401744513L;

    public double water, oil, gas;

    /**
     * Sets up the phase data
     */
    public PhaseDataDouble(double water, double oil, double gas) {
        this.water = water;
        this.oil = oil;
        this.gas = gas;
    }

    /**
     * Sets up an empty phase data
     */
    public PhaseDataDouble() {
        water = oil = gas = 0;
    }

    /**
     * Copy constructor
     */
    public PhaseDataDouble(PhaseDataDouble phaseData) {
        set(phaseData);
    }

    /**
     * Returns the given phase value
     */
    public double get(Phase phase) {
        switch (phase) {
        case WATER:
            return water;
        case OIL:
            return oil;
        case GAS:
            return gas;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Sets the given phase value
     */
    public void set(Phase phase, double value) {
        switch (phase) {
        case WATER:
            water = value;
            break;
        case OIL:
            oil = value;
            break;
        case GAS:
            gas = value;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Adds the given phase value
     */
    public void add(Phase phase, double value) {
        switch (phase) {
        case WATER:
            water += value;
            break;
        case OIL:
            oil += value;
            break;
        case GAS:
            gas += value;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Zeros the phase data
     */
    public void zero() {
        water = oil = gas = 0;
    }

    public void set(PhaseDataDouble data) {
        water = data.water;
        oil = data.oil;
        gas = data.gas;
    }

    @Override
    public String toString() {
        return "Water:\t" + water + "\nOil:\t" + oil + "\nGas:\t" + gas + "\n";
    }

}
