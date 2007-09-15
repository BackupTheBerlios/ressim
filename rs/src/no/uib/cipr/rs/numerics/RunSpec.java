package no.uib.cipr.rs.numerics;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Contains the information regarding the numerics of the simulation
 * (discretisation, timestepping, etc).
 */
class RunSpec {

    /**
     * The end time [s]
     */
    private final double endTime;

    /**
     * Times to output field data [s]
     */
    private final SortedSet<Double> reportTimes;

    /**
     * If true, every single timestep is to be reported
     */
    private final boolean reportAlways;

    /**
     * Time unit used externally. Internally seconds are used
     */
    private final TimeUnit timeUnit;

    /**
     * True for a thermal run, false for an iso-thermal run
     */
    private final boolean thermal;

    /**
     * Tuning parameter for estimating the next timestep
     */
    private final double lambda;

    /**
     * Target residual volume
     */
    private final double targetResidualVolume;

    /**
     * Target number of non-linear pressure iterations
     */
    private final int targetNonLinearPressureIterations;

    /**
     * Largest and smallest timestep sizes
     */
    private final double dtMin, dtMax;

    /**
     * Maximum number of nonlinear pressure iterations
     */
    private final int maximumNonLinearPressureIterations;

    /**
     * Pressure tolerance
     */
    private final double pressureTolerance;

    /**
     * This is used for creating the linear system solver
     */
    private final Configuration runSpec;

    /**
     * Output directory
     */
    private final File directory;

    /**
     * Target throughput ratio
     */
    private final double targetThroughputRatio;

    /**
     * Name of the linear solver
     */
    private final String linearSolver;

    /**
     * Name of the block preconditioner
     */
    private final String preconditioner;

    /**
     * Maximum number of linear iterations
     */
    private final int maxNumberOfLinearIterations;

    /**
     * Relative convergence tolerance for the linear solver
     */
    private final double relativeTolerance;

    /**
     * Absolute convergence tolerance for the linear solver
     */
    private final double absoluteTolerance;

    /**
     * Relative divergence tolerance for the linear solver
     */
    private final double divergenceTolerance;

    /**
     * Report iteration progress?
     */
    private final boolean reportIterations;

    /**
     * Time to restart from. Set to null for no restart
     */
    private final String restartTime;

    /**
     * Truncate negative mole numbers instead of reporting an error?
     */
    private final boolean truncateNegativeMoles;

    public RunSpec(Configuration config, String restartTime) {
        runSpec = config.getConfiguration("RunSpec");

        /*
         * Timestepping parameters
         */

        // Time units and the end time
        timeUnit = TimeUnit.getTimeUnit(runSpec.getString("TimeUnit",
                TimeUnit.SECONDS.toString()));
        endTime = runSpec.getDouble("EndTime", 0) * timeUnit.inSeconds();

        // Smallest and largest timestep
        dtMin = runSpec.getDouble("TimeStepMinimum", 1e-10)
                * timeUnit.inSeconds();
        dtMax = runSpec.getDouble("TimeStepMaximum", 1) * timeUnit.inSeconds();

        if (dtMin < 0)
            throw new IllegalArgumentException(runSpec.trace()
                    + "TimeStepMinimum cannot be negative");

        if (dtMax < dtMin)
            throw new IllegalArgumentException(runSpec.trace()
                    + "TimeStepMaximum cannot be smaller than TimeStepMinimum");

        reportAlways = runSpec.getBoolean("ReportAlways", false);

        // The specified times to output current state
        double[] rt = runSpec.getDoubleArray("ReportTimes");
        reportTimes = new TreeSet<Double>();
        for (double r : rt)
            reportTimes.add(r * timeUnit.inSeconds());

        // Evenly spaced report times
        double ert = runSpec.getDouble("ReportEvery", 0) * timeUnit.inSeconds();
        if (ert > 0)
            for (double r = 0; r <= endTime + Tolerances.smallEps; r += ert)
                reportTimes.add(r);

        double startTime = restartTime == null ? 0 : Double
                .parseDouble(restartTime);
        System.out.println(runSpec.trace() + "Simulating from " + startTime
                + " to " + (endTime / timeUnit.inSeconds()) + " " + timeUnit);
        startTime *= timeUnit.inSeconds();

        /*
         * Thermal run?
         */

        thermal = runSpec.getBoolean("Thermal", false);

        /*
         * Non-linear system solvers and tolerances
         */

        maximumNonLinearPressureIterations = runSpec.getInt(
                "MaximumNonLinearPressureIterations", 10);

        if (maximumNonLinearPressureIterations < 0)
            throw new IllegalArgumentException(runSpec.trace()
                    + "MaximumNonLinearPressureIterations must be positive");

        lambda = runSpec.getDouble("Lambda", 0.5);

        targetResidualVolume = getTolerance(runSpec, "TargetResidualVolume",
                1e-2);
        pressureTolerance = getTolerance(runSpec, "PressureTolerance", 1e-6);

        targetNonLinearPressureIterations = runSpec.getInt(
                "TargetNonLinearPressureIterations", 5);

        targetThroughputRatio = runSpec.getDouble("TargetThroughputRatio", 0.5);

        truncateNegativeMoles = runSpec.getBoolean("TruncateNegativeMoles",
                false);

        /*
         * Linear system solver and tolerances
         */

        linearSolver = runSpec.getString("LinearSolver", "BiCGstab");
        preconditioner = runSpec.getString("Preconditioner", "ILU");

        maxNumberOfLinearIterations = runSpec
                .getInt("NumberOfIterations", 5000);
        relativeTolerance = runSpec.getDouble("RelativeTolerance", 1e-50);
        absoluteTolerance = runSpec.getDouble("AbsoluteTolerance", 1e-12);
        divergenceTolerance = runSpec.getDouble("DivergenceTolerance", 1e+5);
        reportIterations = runSpec.getBoolean("ReportIterations", false);

        /*
         * I/O streams and files
         */

        // since we use a particular formatting of the numbers to create
        // directories with restart information, then we use the same formatting
        // regardless of the number passed to us by the user (so that we don't
        // have to remember how many zeros to include after period etc.)
        this.restartTime = restartTime == null ? null : Paths.restart(Double
                .parseDouble(restartTime));

        // Output directory
        directory = new File(Paths.SIMULATION_OUTPUT);
        directory.mkdirs();

        // Clean out the output-directory, unless this is a restart run
        if (!restart() && !reportTimes.isEmpty())
            for (File file : directory.listFiles()) {
                if (file.isDirectory())
                    for (File sub : file.listFiles())
                        sub.delete();
                file.delete();
            }

        // Do not report the past for a restart run
        else
            while (!reportTimes.isEmpty() && reportTimes.first() <= startTime)
                reportTimes.remove(reportTimes.first());
    }

    private double getTolerance(Configuration config, String name, double std) {
        double tolerance = runSpec.getDouble(name, std);

        if (tolerance < 0)
            throw new IllegalArgumentException(config.trace() + name
                    + " cannot be negative");

        if (tolerance > 1)
            throw new IllegalArgumentException(config.trace() + name
                    + " cannot be larger than 1");

        return tolerance;
    }

    /**
     * Is this a restart run?
     */
    public boolean restart() {
        return restartTime != null;
    }

    /**
     * Restart time for a restart run
     */
    public String restartTime() {
        return restartTime;
    }

    /**
     * The endtime of the run
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Returns the report times
     */
    public SortedSet<Double> getReportTimes() {
        return new TreeSet<Double>(reportTimes);
    }

    /**
     * True if every timestep data should be reported
     */
    public boolean reportAlways() {
        return reportAlways;
    }

    /**
     * Returns the timeunit of the run
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Returns the smallest timestep size
     */
    public double getTimeStepMinimum() {
        return dtMin;
    }

    /**
     * Returns the largest timestep size
     */
    public double getTimeStepMaximum() {
        return dtMax;
    }

    /**
     * Target relative residual volume
     */
    public double getTargetResidualVolume() {
        return targetResidualVolume;
    }

    /**
     * Target number of non-linear pressure iterations
     */
    public int getTargetNonLinearPressureIterations() {
        return targetNonLinearPressureIterations;
    }

    /**
     * Gets the relative pressure change tolerance
     */
    public double getPressureTolerance() {
        return pressureTolerance;
    }

    /**
     * Returns the maximum number of non-linear pressure iterations
     */
    public int getMaximumNonLinearPressureIterations() {
        return maximumNonLinearPressureIterations;
    }

    /**
     * True for a thermal run
     */
    public boolean isThermal() {
        return thermal;
    }

    /**
     * Gets the tuning parameter for choosing the next timestep length
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Gets the output directory
     */
    public File getOutputDirectory() {
        return directory;
    }

    /**
     * Returns the target throughput ratio
     */
    public double getTargetThroughputRatio() {
        return targetThroughputRatio;
    }

    /**
     * Name of the linear solver
     */
    public String getLinearSolver() {
        return linearSolver;
    }

    /**
     * Name of the preconditioner
     */
    public String getPreconditioner() {
        return preconditioner;
    }

    /**
     * Maximum number of linear iterations per non-linear iteration
     */
    public int getMaxNumberOfLinearIterations() {
        return maxNumberOfLinearIterations;
    }

    /**
     * Relative convergence tolerance for the linear solver
     */
    public double getRelativeTolerance() {
        return relativeTolerance;
    }

    /**
     * Absolute convergence tolerance for the linear solver
     */
    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    /**
     * Divergence tolerance for the linear solver
     */
    public double getDivergenceTolerance() {
        return divergenceTolerance;
    }

    /**
     * True for reporting the progress of the linear solver
     */
    public boolean reportIterations() {
        return reportIterations;
    }

    /**
     * Truncate negative mole numbers instead of reporting an error?
     */
    public boolean truncateNegativeMoles() {
        return truncateNegativeMoles;
    }

    /**
     * Creates a linear system solver for the given mesh
     * 
     * @param locked
     *                Indices to cells whose state is locked
     */
    public LinearSolver createSolver(Mesh mesh, int[] locked) {
        return new LinearSolver(this, mesh, locked);
    }
}
