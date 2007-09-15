package no.uib.cipr.rs.field;

import no.uib.cipr.rs.fluid.Component;
import no.uib.cipr.rs.fluid.Components;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.ConstantValue;
import no.uib.cipr.rs.util.Function;

/**
 * Initial values for volume balance initialisation
 */
class InitialValues {

    /**
     * Datum depth [m]
     */
    private double zd;

    /**
     * Datum pressure [Pa]
     */
    private double pd;

    /**
     * Temperature with depth, T(z) [K]
     */
    private Function Tz;

    /**
     * Molar mass fractions with depth, C(z) [-]
     */
    private Function[] Cz;

    /**
     * Reads in the initial values from the given configuration
     */
    public InitialValues(Configuration config, Components components) {
        Configuration init = config.getConfiguration("InitialValues");

        zd = init.getDouble("DatumDepth");
        pd = init.getDouble("DatumPressure");

        if (pd <= 0)
            throw new IllegalArgumentException(init.trace()
                    + "Datum pressure was " + pd + ", but it be positive");

        Tz = init.getFunction("Temperature", 1);

        int nc = components.numComponents();
        Cz = new Function[nc];
        for (Component nu : components)
            Cz[nu.index()] = init.getFunction(nu.name(), 1,
                    ConstantValue.class, 0.);
    }

    /**
     * Gets the datum depth
     * 
     * @return [m]
     */
    public double getDatumDepth() {
        return zd;
    }

    /**
     * Gets the datum pressure
     * 
     * @return [Pa]
     */
    public double getDatumPressure() {
        return pd;
    }

    /**
     * Gets the temperature at a given depth
     * 
     * @param z
     *            [m]
     * @return [K]
     */
    public double getTemperature(double z) {
        double T = Tz.get(z);
        if (T <= 0)
            throw new IllegalArgumentException("Temperature at z=" + z + " is "
                    + T + ", but must it be positive");
        return T;
    }

    /**
     * Gets a (potentially unnormalised) component mass fraction at a given
     * depth
     * 
     * @param nu
     *            Relevant component
     * @param z
     *            [m]
     * @return [-]
     */
    public double getComponentFraction(Component nu, double z) {
        double C = Cz[nu.index()].get(z);
        if (C < 0)
            throw new IllegalArgumentException("Fraction of " + nu + " at z="
                    + z + " is " + C + ", but it cannot be negative");
        return C;
    }
}