package no.uib.cipr.rs.fluid;

import no.uib.cipr.rs.util.Configuration;

/**
 * General equation of state
 */
public abstract class EquationOfState {

    /**
     * Calculates the phase states at given system conditions.
     * 
     * @param p
     *            System pressure [Pa]
     * @param N
     *            System composition [mol]
     * @param T
     *            System temperature [K]
     * @param data
     *            Where to store the results
     */
    public abstract void calculatePhaseState(double p, Composition N, double T,
            PhaseData<EquationOfStateData> data);

    /**
     * Creates an equation of state from the given configuration
     */
    public static EquationOfState create(Configuration config,
            Components components) {
        return config.getObject("EquationOfState", EquationOfState.class,
                components);
    }
}
