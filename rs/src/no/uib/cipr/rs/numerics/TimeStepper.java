package no.uib.cipr.rs.numerics;

import no.uib.cipr.rs.field.Field;

/**
 * Performs administrative timestepping tasks, but does not perform actual
 * timediscretisations
 */
class TimeStepper {

    /**
     * These is the actual discretisation of the timedependent problem
     */
    private final Discretisation discretisation;

    /**
     * The system field state
     */
    private final Field field;

    /**
     * The used timeunit
     */
    private final TimeUnit timeUnit;

    /**
     * Smallest and largest timestep sizes
     */
    private final double dtMin, dtMax;

    /**
     * Current timestep size [s]
     */
    private double dt;

    /**
     * Sets up the timestepper
     */
    public TimeStepper(RunSpec runSpec, Field field,
            Discretisation discretisation) {
        this.field = field;
        this.discretisation = discretisation;

        timeUnit = runSpec.getTimeUnit();

        // Smallest and largest permitted timestep sizes
        dtMin = runSpec.getTimeStepMinimum();
        dtMax = runSpec.getTimeStepMaximum();

        dt = dtMin;
    }

    /**
     * Starts the timestepping
     * 
     * @return True for success, false for a failure
     */
    public boolean stepTo(double endTime) {

        // Advance the field state forward to the end time
        while (field.getTime() < endTime) {

            // Adjust stepsize down to meet the next report time
            dt = adjustDtForReporting(dt, endTime);

            reportTime(dt);

            // Advance a single time step, halving the timestep on failure
            while (true) {
                try {
                    discretisation.solve(dt);
                    break;
                } catch (SolverFailure e) {

                    System.err.println("\n\t" + e.getMessage() + "\n");

                    if (dt > dtMin)
                        System.err
                                .println("\n\tRetrying with reduced timestep...");
                    else {
                        System.err
                                .println("\n\tTimestep cannot be further reduced");

                        // Failed simulation
                        return false;
                    }

                    discretisation.retractField(dt);
                    double factor = Math.min(1. / 2., discretisation
                            .throughputFactor());
                    dt = Math.min(Math.max(dt * factor, dtMin), dtMax);

                    reportTime(dt);
                }
            }

            // Calculate a new local timestep
            dt = discretisation.calculateDt(dt);
        }

        return true;
    }

    /**
     * Reports the time and timestep size
     */
    private void reportTime(double dt) {
        double t = toTimeUnit(field.getTime());

        System.out.format("\nt =%13e, dt =%13e\n", t, toTimeUnit(dt));
    }

    /**
     * Adjusts the time step to fit with the end time. This will only reduce the
     * proposed dt, not increase it
     */
    private double adjustDtForReporting(double dt, double endTime) {

        double nextTime = dt + field.getTime();

        // Ensure that we do not pass beyond the end time
        if (nextTime > endTime) {
            dt = endTime - field.getTime();
            nextTime = dt + field.getTime();
        }

        return dt;
    }

    /**
     * Converts the given number of seconds into the user-chosen timeunit
     */
    private double toTimeUnit(double s) {
        return timeUnit.inSeconds(s);
    }
}
