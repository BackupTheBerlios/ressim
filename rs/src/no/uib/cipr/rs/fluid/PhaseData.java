package no.uib.cipr.rs.fluid;

import java.io.Serializable;

/**
 * Phase specific data
 */
public class PhaseData<T> implements Serializable {

    private static final long serialVersionUID = 3227580230508283191L;

    public T water, oil, gas;

    /**
     * Sets up the phase data
     */
    public PhaseData(T water, T oil, T gas) {
        this.water = water;
        this.oil = oil;
        this.gas = gas;
    }

    /**
     * Sets up an empty phase data. Properties must be set before they are used
     */
    public PhaseData() {
        water = null;
        oil = null;
        gas = null;
    }

    /**
     * Returns the given phase value
     */
    public T get(Phase phase) {
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
    public void set(Phase phase, T value) {
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

    @Override
    public String toString() {
        return "Water:\t" + water + "\nOil:\t" + oil + "\nGas:\t" + gas + "\n";
    }

}
