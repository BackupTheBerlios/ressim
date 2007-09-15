package no.uib.cipr.rs.numerics;

/**
 * Different timeunits supported. These are Seconds, Days, Years and
 * Million years
 */
enum TimeUnit {

    /**
     * Timeunit of seconds
     */
    SECONDS("seconds", 1),

    /**
     * Timeunit of days
     */
    DAYS("days", 60. * 60. * 24.),

    /**
     * Timeunit of years
     */
    YEARS("years", 60. * 60. * 24. * 365.),

    /**
     * Timeunit of million of years
     */
    MILLION_YEARS("million years", 60. * 60. * 24. * 365. * 1000. * 1000.);

    /**
     * Name of the timeunit
     */
    private String name;

    /**
     * Number of seconds in the timeunit
     */
    private double seconds;

    private TimeUnit(String name, double seconds) {
        this.name = name;
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the number of seconds in this timeunit
     */
    public double inSeconds() {
        return seconds;
    }

    /**
     * Converts the given number of seconds into the number of timeunits
     */
    public double inSeconds(double sec) {
        return sec / seconds;
    }

    /**
     * Gets the timeunit for the given string. It must be Seconds, Days, Years,
     * or MillionYears (case insensitive)
     */
    public static TimeUnit getTimeUnit(String value) {
        if (value.equalsIgnoreCase("Seconds"))
            return SECONDS;
        else if (value.equalsIgnoreCase("Days"))
            return DAYS;
        else if (value.equalsIgnoreCase("Years"))
            return YEARS;
        else if (value.equalsIgnoreCase("MillionYears"))
            return MILLION_YEARS;
        else
            throw new IllegalArgumentException("Unknown timeunit \"" + value
                    + "\"");
    }

}
