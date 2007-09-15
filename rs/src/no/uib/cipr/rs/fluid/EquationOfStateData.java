package no.uib.cipr.rs.fluid;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Stores output data from an equation of state calculation. One such
 * data-object is needed for each phase in each gridblock.
 */
public class EquationOfStateData implements Serializable {

    private static final long serialVersionUID = -7956439176327014930L;

    /**
     * Components database
     */
    private final Components components;

    /**
     * The phase these properties are stored for
     */
    private final Phase phase;

    /**
     * Phase presence or absence
     */
    private boolean present;

    /**
     * Volume [m^3]
     */
    private double V;

    /**
     * Composition [mol]
     */
    private Composition N;

    /**
     * Molar density [mol/m^3]
     */
    private double xi;

    /**
     * Phase volume derivative with pressure [m^3/Pa]
     */
    private double dVdp;

    /**
     * Phase volume derivative with temperature [m^3/K]
     */
    private double dVdT;

    /**
     * Phase volume derivative with masses [m^3/mol]
     */
    private double[] dVdN;

    /**
     * Enthalpy density [J/m^3]
     */
    private double h;

    /**
     * Enthalpy density derivatives with temperature (heat capacity) [J/(K*m^3)]
     */
    private double dhdT;

    /**
     * Viscosity [Pa*s]
     */
    private double mu;

    /**
     * Allocates equation of state data
     * 
     * @param components
     *                Component database
     * @param phase
     *                The phase the properties are stored for
     */
    public EquationOfStateData(Components components, Phase phase) {
        this.components = components;
        this.phase = phase;

        int nc = components.numComponents();

        N = new Composition(components);
        dVdN = new double[nc];
    }

    /**
     * Checks if the phase is recorded as present
     * 
     * @return True if present, else false
     */
    public boolean isPresent() {
        return present;
    }

    /**
     * Sets the phase to be present
     */
    void setPresent() {
        setPresent(true);
    }

    /**
     * Sets the phase to not be present. Its composition is zeroed
     */
    void setNotPresent() {
        setPresent(false);
    }

    /**
     * Sets the phase present or not. Its properties are zeroed if it does not
     * exists.
     * 
     * @param exists
     *                True if the phase should be present, false if not
     */
    void setPresent(boolean exists) {
        present = exists;
        if (!exists) {
            V = 0;
            N.zero();
            xi = 0;

            dVdp = 0;
            dVdT = 0;

            h = 0;
            dhdT = 0;
            mu = 0;

            Arrays.fill(dVdN, 0);
        }
    }

    /**
     * Gets the phase composition
     * 
     * @return [mol]
     */
    public Composition getComposition() {
        return N;
    }

    /**
     * Gets the phase volume
     * 
     * @return Phase volume. [m^3]
     */
    public double getVolume() {
        return V;
    }

    /**
     * Sets the phase volume
     * 
     * @param V
     *                New phase volume. [m^3]
     */
    void setVolume(double V) {
        if (present)
            this.V = V;
    }

    /**
     * Gets the phase molar density
     * 
     * @return Phase molar density. [mol/m^3]
     */
    public double getMolarDensity() {
        return xi;
    }

    /**
     * Sets the molar density
     * 
     * @param xi
     *                New phase molar density. [mol/m^3]
     */
    void setMolarDensity(double xi) {
        if (present) {
            if (xi <= 0)
                throw new IllegalArgumentException(phase
                        + " molar density cannot be negative, but was " + xi);
            this.xi = xi;
        }
    }

    /**
     * Gets the phase volume change with pressure
     * 
     * @return dVdp [m^3/Pa]
     */
    public double getdVdp() {
        return dVdp;
    }

    /**
     * Sets the phase volume change with pressure
     * 
     * @param dVdp
     *                dVdp [m^3/Pa]
     */
    void setdVdp(double dVdp) {
        if (present)
            this.dVdp = dVdp;
    }

    /**
     * Gets the phase volume derivative with respect to molar mass change
     * 
     * @param component
     *                Component
     * @return dVdN. [m^3/mol]
     */
    public double getdVdN(Component component) {
        return dVdN[component.index()];
    }

    /**
     * Sets the phase volume derivative with respect to molar mass change
     * 
     * @param component
     *                The changing component
     * @param dVdN
     *                The dVdN derivative. [m^3/mol]
     */
    void setdVdN(Component component, double dVdN) {
        if (present)
            this.dVdN[component.index()] = dVdN;
    }

    /**
     * Gets the phase volume derivative with respect to temperature change
     * 
     * @return dVdT. [m^3/K]
     */
    public double getdVdT() {
        return dVdT;
    }

    /**
     * Sets the phase volume derivative with respect to temperature change
     * 
     * @param dVdT
     *                dVdT. [m^3/K]
     */
    void setdVdT(double dVdT) {
        if (present)
            this.dVdT = dVdT;
    }

    /**
     * Gets the enthalpy density
     * 
     * @return Enthalpy density [J/m^3]
     */
    public double getEnthalpyDensity() {
        return h;
    }

    /**
     * Sets the enthalpy density
     * 
     * @param h
     *                New enthalpy density [J/m^3]
     */
    void setEnthalpyDensity(double h) {
        if (h < 0)
            throw new IllegalArgumentException(phase
                    + " enthalpy density cannot be negative, but was " + h);

        if (present)
            this.h = h;
    }

    /**
     * Gets the volumetric heat capacity
     * 
     * @return Volumetric heat capacity [J/(K*m^3)]
     */
    public double getHeatCapacity() {
        return dhdT;
    }

    /**
     * Sets the volumetric heat capacity
     * 
     * @param c
     *                New volumetric heat capacity [J/(K*m^3)]
     */
    void setHeatCapacity(double c) {
        // Could check for negative heat capacity, but that's more suitable
        // for the temperature solver, which can check for a negative diagonal
        if (present)
            this.dhdT = c;
    }

    /**
     * Sets the phase viscosity
     * 
     * @param mu
     *                New viscosity [Pa*s]
     */
    void setViscosity(double mu) {
        if (mu < 0)
            throw new IllegalArgumentException(phase
                    + " viscosity cannot be negative, but was " + mu);

        if (present)
            this.mu = mu;
    }

    /**
     * Gets the phase viscosity
     * 
     * @return Phase viscosity [Pa*s]
     */
    public double getViscosity() {
        return mu;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        String stringFormat = "%-16s%15s\n";
        String boolFormat = "%-16s%15b\n";
        String numberFormat = "%-16s%15g\n";

        string.append(String.format(stringFormat, "Phase", phase.toString()));
        string.append(String.format(boolFormat, "Present", present));

        if (!present)
            return string.toString();

        string.append(String.format(numberFormat, "Volume", V));
        string.append(String.format(numberFormat, "Molar density", xi));
        string.append(N);

        string.append(String.format(numberFormat, "Enthalpy", h));
        string.append(String.format(numberFormat, "Heat capacity", dhdT));

        string.append(String.format(numberFormat, "Viscosity", mu));

        string.append(String.format(numberFormat, "dV/dp", dVdp));
        string.append(String.format(numberFormat, "dV/dT", dVdT));
        for (Component nu : components)
            string.append(String.format(numberFormat, String.format(
                    "dV/dN(%s)", nu), dVdN[nu.index()]));

        return string.toString();
    }
}
