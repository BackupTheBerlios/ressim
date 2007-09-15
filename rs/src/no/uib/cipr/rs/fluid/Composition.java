package no.uib.cipr.rs.fluid;

import java.io.Serializable;
import java.util.Arrays;

import no.uib.cipr.rs.util.Configuration;

/**
 * Holds a chemical composition
 */
public class Composition implements Serializable {

    private static final long serialVersionUID = 3257284747016550451L;

    /**
     * Component database
     */
    private final Components components;

    /**
     * Number of moles of each component. [mol]
     */
    private double[] N;

    /**
     * Total number of moles. [mol]
     */
    private double NT;

    /**
     * Reads in a composition from the given configuration
     * 
     * @param config
     *            Reads in component names and mole numbers
     * @param components
     *            Component database
     */
    public Composition(Configuration config, Components components) {
        this.components = components;
        N = new double[components.numComponents()];
        for (Component nu : components.all())
            setMoles(nu, config.getDouble(nu.name(), 0));
    }

    /**
     * Sets up a new composition
     * 
     * @param components
     *            Components data base
     */
    public Composition(Components components) {
        this.components = components;
        N = new double[components.numComponents()];
    }

    /**
     * Sets the number of moles of the given component
     * 
     * @param nu
     *            Component
     * @param N
     *            Number of moles
     */
    public void setMoles(Component nu, double N) {
        int index = nu.index();
        NT += N - this.N[index];
        this.N[index] = N;
    }

    /**
     * Adds a number of moles to the given component moles
     * 
     * @param nu
     *            Component
     * @param N
     *            Number of moles to add
     */
    public void addMoles(Component nu, double N) {
        NT += N;
        this.N[nu.index()] += N;
    }

    /**
     * Gets the number of moles of the given component
     * 
     * @param nu
     *            Component
     * @return Number of moles
     */
    public double getMoles(Component nu) {
        return N[nu.index()];
    }

    /**
     * Gets the mole fraction of the given component in the composition. If
     * there are no moles in the composition, 0 is returned.
     * 
     * @param nu
     *            Component
     * @return Component mole fraction
     */
    public double getMoleFraction(Component nu) {
        if (NT > 0)
            return N[nu.index()] / NT;
        else
            return 0;
    }

    /**
     * Gets the total number of moles
     * 
     * @return Total mole number
     */
    public double getMoles() {
        return NT;
    }

    /**
     * Sets this composition equal the given composition
     */
    public void set(Composition composition) {
        System.arraycopy(composition.N, 0, N, 0, N.length);
        NT = composition.getMoles();
    }

    /**
     * Zeros the composition
     */
    public void zero() {
        Arrays.fill(N, 0);
        NT = 0;
    }

    /**
     * Number of components
     */
    public int numComponents() {
        return N.length;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        for (Component c : components)
            string.append(String.format("%-12s%15g\n", c, N[c.index()]));

        return string.toString();
    }

}
