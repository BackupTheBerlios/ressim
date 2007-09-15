package no.uib.cipr.rs.util;

/**
 * Converts between the user visible units and the internal SI units
 */
public final class Conversions {

    /**
     * SI unit of permeability [m^2] in Darcy.
     */
    public final static double squareMeterInDarcy = 1.01325e+12;

    /**
     * MilliDarcy in square meter
     */
    public final static double mDarcyInSquareMeter = 1e-3 * (1 / squareMeterInDarcy);

    /**
     * SPE feet in meter
     */
    public final static double feetInMeter = 0.3048;

    /**
     * Multiply to convert from user to SI
     */
    private static double length, permeability, pressure, viscosity, rate,
            time, volume;

    /**
     * Sets up the unit conversions
     */
    public static void setup(Configuration config) {
        // Length is 'cm'
        length = 1e-2;

        // Permeability is 'md'
        permeability = 1e-3 / 1.01325e+12;

        // Pressure
        String pressureUnit = config.getString("PressureUnit");
        if (pressureUnit.equals("kPa"))
            pressure = 1e+3;
        else if (pressureUnit.equals("mbar"))
            pressure = 1e+2;
        else if (pressureUnit.equals("bar"))
            pressure = 1e+5;
        else
            throw new IllegalArgumentException("Unknown PressureUnit '"
                    + pressureUnit + "'");

        // Viscosity is 'cp'
        viscosity = 1e-3;

        // Rate (volume/time)
        String rateUnit = config.getString("RateUnit");
        if (rateUnit.equals("cc/min"))
            rate = 1e-6 / 60;
        else if (rateUnit.equals("m^3/day"))
            rate = 1. / (24 * 60 * 60);
        else
            throw new IllegalArgumentException("Unknown RateUnit '" + rateUnit
                    + "'");

        // Time
        String timeUnit = config.getString("TimeUnit");
        if (timeUnit.equals("minutes"))
            time = 60;
        else if (timeUnit.equals("hours"))
            time = 60 * 60;
        else if (timeUnit.equals("days"))
            time = 24 * 60 * 60;
        else
            throw new IllegalArgumentException("Unknown TimeUnit '" + timeUnit
                    + "'");

        // Volume is 'ml'
        volume = 1e-6;
    }

    /**
     * Converts user length into SI
     */
    public static double length(double user) {
        return user * length;
    }

    /**
     * Converts user length into SI
     */
    public static double[] length(double[] user) {
        return multiply(user, length);
    }

    /**
     * Converts user permeability into SI
     */
    public static double[] permeability(double[] user) {
        return multiply(user, permeability);
    }

    /**
     * Converts user pressure into SI
     */
    public static double pressure(double user) {
        return user * pressure;
    }

    /**
     * Converts user pressure into SI
     */
    public static double[] pressure(double[] user) {
        return multiply(user, pressure);
    }

    /**
     * Converts user viscosity into SI
     */
    public static double viscosity(double user) {
        return user * viscosity;
    }

    /**
     * Converts user viscosity into SI
     */
    public static double[] viscosity(double[] user) {
        return multiply(user, viscosity);
    }

    /**
     * Converts user rate into SI
     */
    public static double rate(double user) {
        return user * rate;
    }

    /**
     * Converts user rate into SI
     */
    public static double[] rate(double[] user) {
        return multiply(user, rate);
    }

    /**
     * Converts user time into SI
     */
    public static double time(double user) {
        return user * time;
    }

    /**
     * Converts user time into SI
     */
    public static double[] time(double[] user) {
        return multiply(user, time);
    }

    /**
     * Converts an array of user data into SI
     */
    private static double[] multiply(double[] user, double factor) {
        double[] si = user.clone();
        for (int i = 0; i < si.length; ++i)
            si[i] *= factor;
        return si;
    }

    /**
     * Converts SI pressure to user unit
     */
    public static double userPressure(double si) {
        return si / pressure;
    }

    /**
     * Converts SI time to user unit
     */
    public static double userTime(double si) {
        return si / time;
    }

    /**
     * Converts SI volume to user unit
     */
    public static double userVolume(double si) {
        return si / volume;
    }

    /**
     * Converts SI length to user unit
     */
    public static double userLength(double si) {
        return si / length;
    }
}
