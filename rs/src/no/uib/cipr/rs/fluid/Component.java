package no.uib.cipr.rs.fluid;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.rs.util.Configuration;

/**
 * A chemical component. Stores certain component properties, and also maintains
 * a database of default values
 */
public class Component implements Serializable, Comparable<Component> {

    private static final long serialVersionUID = 3258129150387960121L;

    /**
     * Default molecular weights based on the components chemical name
     */
    private transient static Map<String, Double> defaultM, defaultTc,
            defaultPc, defaultVc, defaultOmega;

    static {
        // Molecular weights
        defaultM = new HashMap<String, Double>();
        defaultM.put("H2O", 0.01801528);
        defaultM.put("N2", 0.028013);
        defaultM.put("CO2", 0.044010);
        defaultM.put("H2S", 0.0341);

        defaultM.put("C1", 0.016054);
        defaultM.put("C2", 0.030070);
        defaultM.put("C3", 0.044010);
        defaultM.put("C4", 0.058124);
        defaultM.put("C5", 0.072151);
        defaultM.put("C6", 0.086178);
        defaultM.put("C7", 0.100205);
        defaultM.put("C8", 0.114232);
        defaultM.put("C9", 0.128259);
        defaultM.put("C10", 0.142286);
        defaultM.put("C11", 0.156313);
        defaultM.put("C12", 0.170340);
        defaultM.put("C13", 0.184367);
        defaultM.put("C14", 0.198394);
        defaultM.put("C15", 0.212421);
        defaultM.put("C16", 0.226448);
        defaultM.put("C17", 0.240475);
        defaultM.put("C18", 0.254504);
        defaultM.put("C19", 0.258529);
        defaultM.put("C20", 0.282556);

        // Critical temperatures
        defaultTc = new HashMap<String, Double>();
        defaultTc.put("H2O", 647.10);
        defaultTc.put("N2", 126.2);
        defaultTc.put("CO2", 304.1);
        defaultTc.put("H2S", 373.2);

        defaultTc.put("C1", 190.58);
        defaultTc.put("C2", 305.42);
        defaultTc.put("C3", 369.82);
        defaultTc.put("C4", 425.18);
        defaultTc.put("C5", 469.7);
        defaultTc.put("C6", 507.3);
        defaultTc.put("C7", 540.1);
        defaultTc.put("C8", 568.7);
        defaultTc.put("C9", 594.6);
        defaultTc.put("C10", 617.7);
        defaultTc.put("C11", 638.8);
        defaultTc.put("C12", 658.4);
        defaultTc.put("C13", 675.9);
        defaultTc.put("C14", 692.3);
        defaultTc.put("C15", 707.8);
        defaultTc.put("C16", 722.6);
        defaultTc.put("C17", 735.6);
        defaultTc.put("C18", 774.2);
        defaultTc.put("C19", 756.);
        defaultTc.put("C20", 767.);

        // Critical pressures
        defaultPc = new HashMap<String, Double>();
        defaultPc.put("H2O", 22.12e+6);
        defaultPc.put("N2", 3.39e+6);
        defaultPc.put("CO2", 7.398e+6);
        defaultPc.put("H2S", 8.94e+6);

        defaultPc.put("C1", 4.604e+6);
        defaultPc.put("C2", 4.88e+6);
        defaultPc.put("C3", 4.25e+6);
        defaultPc.put("C4", 3.797e+6);
        defaultPc.put("C5", 3.369e+6);
        defaultPc.put("C6", 3.014e+6);
        defaultPc.put("C7", 2.734e+6);
        defaultPc.put("C8", 2.495e+6);
        defaultPc.put("C9", 2.280e+6);
        defaultPc.put("C10", 2.099e+6);
        defaultPc.put("C11", 1.948e+6);
        defaultPc.put("C12", 1.810e+6);
        defaultPc.put("C13", 1.679e+6);
        defaultPc.put("C14", 1.573e+6);
        defaultPc.put("C15", 1.479e+6);
        defaultPc.put("C16", 1.401e+6);
        defaultPc.put("C17", 1.342e+6);
        defaultPc.put("C18", 1.292e+6);
        defaultPc.put("C19", 1.11e+6);
        defaultPc.put("C20", 1.11e+6);

        // Critical molar volumes
        defaultVc = new HashMap<String, Double>();
        defaultVc.put("H2O", 5.71e-5);
        defaultVc.put("N2", 8.98e-5);
        defaultVc.put("CO2", 9.39e-5);
        defaultVc.put("H2S", 9.7922e-5);

        defaultVc.put("C1", 9.92e-5);
        defaultVc.put("C2", 1.483e-4);
        defaultVc.put("C3", 2.03e-4);
        defaultVc.put("C4", 20.55e-4);
        defaultVc.put("C5", 3.04e-4);
        defaultVc.put("C6", 3.7e-4);
        defaultVc.put("C7", 4.32e-4);
        defaultVc.put("C8", 4.92e-4);
        defaultVc.put("C9", 5.48e-4);
        defaultVc.put("C10", 6.03e-4);
        defaultVc.put("C11", 6.6e-4);
        defaultVc.put("C12", 7.13e-4);
        defaultVc.put("C13", 7.80e-4);
        defaultVc.put("C14", 8.3e-4);
        defaultVc.put("C15", 8.8e-4);
        defaultVc.put("C16", 8.8e-4);
        defaultVc.put("C17", 1e-3);
        defaultVc.put("C18", 1e-3);
        defaultVc.put("C19", 1e-3);
        defaultVc.put("C20", 1e-3);

        // Acentric factors
        defaultOmega = new HashMap<String, Double>();
        defaultOmega.put("H2O", 0.344);
        defaultOmega.put("N2", 0.039);
        defaultOmega.put("CO2", 0.239);
        defaultOmega.put("H2S", 0.1);

        defaultOmega.put("C1", 0.011);
        defaultOmega.put("C2", 0.099);
        defaultOmega.put("C3", 0.153);
        defaultOmega.put("C4", 0.199);
        defaultOmega.put("C5", 0.251);
        defaultOmega.put("C6", 0.299);
        defaultOmega.put("C7", 0.349);
        defaultOmega.put("C8", 0.398);
        defaultOmega.put("C9", 0.445);
        defaultOmega.put("C10", 0.489);
        defaultOmega.put("C11", 0.535);
        defaultOmega.put("C12", 0.575);
        defaultOmega.put("C13", 0.619);
        defaultOmega.put("C14", 0.581);
        defaultOmega.put("C15", 0.706);
        defaultOmega.put("C16", 0.742);
        defaultOmega.put("C17", 0.770);
        defaultOmega.put("C18", 0.790);
        defaultOmega.put("C19", 0.827);
        defaultOmega.put("C20", 0.907);
    }

    /**
     * Component chemical name
     */
    private final String name;

    /**
     * Component index. Should be set afterwards by the component database
     */
    private int index = -1;

    /**
     * Molecular weight. [kg/mol]
     */
    private final double Mw;

    /**
     * Critical temperature. [K]
     */
    private final double Tc;

    /**
     * Critical pressure. [Pa]
     */
    private final double Pc;

    /**
     * Critical molar volume. [m^3/mol]
     */
    private final double Vc;

    /**
     * Acentric factor. [-]
     */
    private final double omega;

    /**
     * Creates a component
     * 
     * @param name
     *            Name of the component
     * @param config
     *            Configuration to get component data from
     */
    public Component(String name, Configuration config) {
        this.name = name;

        Mw = getMolecularWeight(name, config);
        Tc = getCriticalTemperature(name, config);
        Pc = getCriticalPressure(name, config);
        Vc = getCriticalVolume(name, config);
        omega = getAcentricFactor(name, config);
    }

    /**
     * Sets the component index. Used by the components database
     */
    void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the component name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the molecular weight [kg/mol]
     */
    public double getMolecularWeight() {
        return Mw;
    }

    /**
     * Returns the critical temperature [K]
     */
    public double getCriticalTemperature() {
        return Tc;
    }

    /**
     * Returns the critical pressure [Pa]
     */
    public double getCriticalPressure() {
        return Pc;
    }

    /**
     * Returns the critical molar volume [m^3/mol]
     */
    public double getCriticalMolarVolume() {
        return Vc;
    }

    /**
     * Returns the acentric factor [-]
     */
    public double getAcentricFactor() {
        return omega;
    }

    /**
     * Returns the index of the component
     */
    public int index() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Component))
            return super.equals(obj);

        Component nu = (Component) obj;
        return nu.index == index;
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(Component nu) {

        // Put water first
        if (name.equalsIgnoreCase("H2O"))
            return -1;
        else if (nu.name.equalsIgnoreCase("H2O"))
            return 1;

        else {

            // Check if this and the other component is C#. In that case, ensure
            // that they use a numerical ordering rather than the ASCII one
            if (name.matches("C\\d++") && nu.name.matches(("C\\d++"))) {
                // Get numeric part
                int nu1 = Integer.parseInt(name.substring(1));
                int nu2 = Integer.parseInt(nu.name.substring(1));

                if (nu1 > nu2)
                    return 1;
                else if (nu1 < nu2)
                    return -1;
                else
                    // The component is being compared against itself
                    return 0;
            } else
                return name.compareToIgnoreCase(nu.name);
        }
    }

    /**
     * Gets the molecular weight of the given component. Both the configuration
     * and the stored defaults are checked
     */
    private static double getMolecularWeight(String name, Configuration config) {
        return getParameter(name, config, defaultM, "Mw", "molecular weight");
    }

    /**
     * Gets the critical temperature of the given component. Both the
     * configuration and the stored defaults are checked
     */
    private static double getCriticalTemperature(String name,
            Configuration config) {
        return getParameter(name, config, defaultTc, "Tc",
                "critical temperature");
    }

    /**
     * Gets the critical pressure of the given component. Both the configuration
     * and the stored defaults are checked
     */
    private static double getCriticalPressure(String name, Configuration config) {
        return getParameter(name, config, defaultPc, "pc", "critical pressure");
    }

    /**
     * Gets the critical volume of the given component. Both the configuration
     * and the stored defaults are checked
     */
    private static double getCriticalVolume(String name, Configuration config) {
        return getParameter(name, config, defaultVc, "Vc", "critical volume");
    }

    /**
     * Gets the acentric factor for the given component. Both the configuration
     * and the stored defaults are checked
     */
    private static double getAcentricFactor(String name, Configuration config) {
        return getParameter(name, config, defaultOmega, "omega",
                "acentric factor");
    }

    /**
     * Gets a parameter, either from supplied configuration or from the
     * defaults.
     */
    private static double getParameter(String name, Configuration config,
            Map<String, Double> map, String key, String value) {

        if (config.containsKey(key))
            return config.getDouble(key);
        else if (map.containsKey(name))
            return map.get(name);
        else
            throw new IllegalArgumentException(config.trace() + "No " + value
                    + " provided for component " + name);
    }

}
