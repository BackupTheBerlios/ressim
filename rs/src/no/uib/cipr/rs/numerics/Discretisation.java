package no.uib.cipr.rs.numerics;

import static no.uib.cipr.rs.field.Source.SourceType.FIXED;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import no.uib.cipr.rs.field.CS;
import no.uib.cipr.rs.field.CV;
import no.uib.cipr.rs.field.Field;
import no.uib.cipr.rs.field.Source;
import no.uib.cipr.rs.field.Source.OutletSource;
import no.uib.cipr.rs.field.Source.RegularSource;
import no.uib.cipr.rs.fluid.Component;
import no.uib.cipr.rs.fluid.Components;
import no.uib.cipr.rs.fluid.Composition;
import no.uib.cipr.rs.fluid.EquationOfStateData;
import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.meshgen.util.ArrayData;

/**
 * Discretisation of the fluid flow. Performs flow couplings in time and space
 * to calculate the primary variables.
 */
class Discretisation {

    /**
     * Computational mesh
     */
    final Mesh mesh;

    /**
     * Components database
     */
    final Components components;

    /**
     * Field state
     */
    final Field field;

    /**
     * Elements which state are locked
     */
    final int[] locked;

    /**
     * Non-linear pressure solver
     */
    private final PressureNewtonRaphson pressure;

    /**
     * Non-linear temperature solver
     */
    private final TemperatureNewtonRaphson temperature;

    /**
     * Explicit molar mass solver
     */
    private final MolarMassExplicit molarMass;

    /**
     * Linear system solver
     */
    final LinearSolver solver;

    /**
     * Thermal run?
     */
    final boolean thermal;

    /**
     * Pressure at the previous timestep
     */
    final double[] p;

    /**
     * Temperature at the previous timestep
     */
    final double[] T;

    /**
     * Residual volume at the previous timestep
     */
    final double[] R;

    /**
     * Compositions at the previous timestep
     */
    final Composition[] N;

    /**
     * Tuning parameter for choosing the next timestep size
     */
    private final double lambda;

    /**
     * Target residual volume
     */
    private final double targetResidualVolume;

    /**
     * Target number of pressure non-linear iterations
     */
    private final int targetNonLinearPressureIterations;

    /**
     * Target throughput ratio
     */
    private final double targetThroughput;

    /**
     * Smallest and largest timestep size
     */
    private final double dtMin, dtMax;

    /**
     * Truncate negative mole numbers instead of reporting an error?
     */
    final boolean truncateNegativeMoles;

    /**
     * Sets up the discretisation and associated solvers
     */
    public Discretisation(Field field, RunSpec runSpec) {
        this.mesh = field.getMesh();
        this.field = field;
        this.components = field.getComponents();

        /*
         * Copy parameters from the run specification
         */

        targetResidualVolume = runSpec.getTargetResidualVolume();
        targetNonLinearPressureIterations = runSpec
                .getTargetNonLinearPressureIterations();
        targetThroughput = runSpec.getTargetThroughputRatio();

        lambda = runSpec.getLambda();

        dtMin = runSpec.getTimeStepMinimum();
        dtMax = runSpec.getTimeStepMaximum();

        truncateNegativeMoles = runSpec.truncateNegativeMoles();

        /*
         * Allocate system state at previous timestep
         */

        int numElements = mesh.elements().size();
        p = new double[numElements];
        T = new double[numElements];
        R = new double[numElements];
        N = new Composition[numElements];
        for (int i = 0; i < numElements; ++i)
            N[i] = new Composition(components);

        /*
         * Thermal run?
         */

        thermal = runSpec.isThermal();

        /*
         * Non-linear system solvers
         */

        pressure = new PressureNewtonRaphson(runSpec);
        temperature = new TemperatureNewtonRaphson();
        molarMass = new MolarMassExplicit();

        /*
         * Find which grid block the system state is locked in
         */

        Set<Integer> lockSet = new HashSet<Integer>();

        // Lock the state in fixed grid blocks
        for (Source source : field.sources())
            if (source.type() == FIXED)
                for (Element el : mesh.elements(source.name()))
                    lockSet.add(el.index);

        // Convert to int[] array
        locked = ArrayData.integerSetToSortedArray(lockSet);

        /*
         * Linear system solver
         */

        solver = runSpec.createSolver(mesh, locked);
    }

    /**
     * Steps the field state forward a single time step by solving for all the
     * primary and secondary variables
     * 
     * @param dt
     *                Time step size
     * @throws SolverFailure
     *                 If any of the discretisations detect problems
     */
    public void solve(double dt) throws SolverFailure {

        storeCurrentState();

        field.changeTime(dt);

        // Solve the nonlinear volume balance equation for the pressure
        pressure.solve(dt);

        // If a thermal run, solve for the temperature
        if (thermal)
            temperature.solve(dt);

        // Solve for the components
        molarMass.solve(dt);

        field.calculateSecondaries();
    }

    /**
     * Stores the current system state, including saturations. This is for later
     * comparisons for adjusting the timestepsize and for retractions
     */
    private void storeCurrentState() {
        for (Element el : mesh.elements()) {
            int i = el.index;

            CV cv = field.getControlVolume(el);

            p[i] = cv.getPressure();
            T[i] = cv.getTemperature();
            R[i] = cv.getResidualVolume();

            N[i].set(cv.getComposition());
        }
    }

    /**
     * Newton-Raphson iteration on the volume balance equation for the
     * determination of the pressure
     */
    private class PressureNewtonRaphson {

        /**
         * Maximum number of nonlinear pressure iterations
         */
        private final int maximumNonLinearPressureIterations;

        /**
         * Pressure tolerance
         */
        private final double pressureTolerance;

        /**
         * The number of nonlinear Newton-Raphson iterations
         */
        int pressureIterations;

        /**
         * Pressure correction
         */
        private final double[] dp = new double[mesh.elements().size()];

        public PressureNewtonRaphson(RunSpec runSpec) {
            maximumNonLinearPressureIterations = runSpec
                    .getMaximumNonLinearPressureIterations();
            pressureTolerance = runSpec.getPressureTolerance();
        }

        /**
         * Carries out the Newton-Raphson iteration
         */
        public void solve(double dt) throws SolverFailure {
            pressureIterations = 0;
            double dpMax = 0, dpMax_prev = 0, diff = 0;

            do {

                // Assembles the flux part of the Jacobian matrix
                assemblePressureJacobianFlux();

                // Assembles the diagonal part of the Jacobian matrix
                assemblePressureJacobianDiagonal(dt);

                // Solve J * dx = -r for dx
                solver.solve();

                // Apply the pressure update, and get the largest change
                dpMax_prev = dpMax;
                dpMax = updatePressure(solver.getCorrection(dp));

                // Flash calculations, but only for positive pressures
                field.calculateSecondaries();

                // Difference in the relative pressure changes
                diff = Math.abs(dpMax_prev - dpMax);

            } while (!converged(Math.min(dpMax, diff), ++pressureIterations));
        }

        /**
         * Checks for pressure convergence across all the threads
         * 
         * @param dpMax
         *                Largest pressure change on this rank
         * @return True if the largest pressure change globally is smaller than
         *         the target change, false otherwise
         * @throws SolverFailure
         *                 Thrown if too many iterations have been spent without
         *                 convergence
         */
        private boolean converged(double dpMax, int pressureIterations)
                throws SolverFailure {

            System.out.format("%3d %6e\n", pressureIterations, dpMax);

            if (dp[0] < pressureTolerance)
                return true;

            if (pressureIterations >= maximumNonLinearPressureIterations)
                throw new SolverFailure(
                        "Nonlinear pressure solver failed to converge");

            return false;
        }

        /**
         * Updates the pressure, and returns the largest relative change
         * 
         * @throws SolverFailure
         *                 If a non-positive pressure was found
         */
        private double updatePressure(double[] dp) throws SolverFailure {
            double dpMax = 0;
            for (Element el : mesh.elements()) {
                int i = el.index;
                CV cv = field.getControlVolume(el);
                double p_old = cv.getPressure();
                double p_new = p_old + dp[i];

                // Check for non-positive pressure
                if (p_new <= 0)
                    throw new SolverFailure("Non-positive pressure in element "
                            + (i + 1) + ": " + p_old + " -> " + p_new);

                cv.setPressure(p_new);

                dpMax = Math.max(dpMax, Math.abs(dp[i] / p[i]));
            }

            return dpMax;
        }

        /**
         * Builds the diagonal (elementwise) part of the Jacobian matrix and
         * residual vector for the pressure calculations
         */
        private void assemblePressureJacobianDiagonal(double dt) {
            for (Element el : mesh.elements()) {
                int i = el.index;

                CV cv = field.getControlVolume(el);

                double dp = cv.getPressure() - p[i];
                double dRdp = cv.getResidualVolumeDerivativePressure();

                solver.addToResidual(i, (R[i] + dRdp * dp) / dt);
                solver.addToJacobian(i, i, dRdp / dt);
            }

            for (Source source : field.sources()) {

                switch (source.type()) {
                case FIXED:

                    // Nothing to do

                    break;
                case REGULAR:

                    assemblePressureRegularSource((RegularSource) source);

                    break;
                case OUTLET:

                    assemblePressureOutletSource((OutletSource) source);

                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported source type: " + source.type());
                }
            }
        }

        /**
         * Assembles a regular fluid source into the pressure Jacobian system
         */
        private void assemblePressureRegularSource(RegularSource regular) {
            for (Element el : mesh.elements(regular.name())) {

                CV cv = field.getControlVolume(el);
                double qe = regular.getEnergySource();

                for (Component nu : components.all()) {
                    double qnu = regular.getMassSource(nu);
                    double dRdN = cv.getResidualVolumeDerivativeMolarMass(nu);
                    solver.addToResidual(el.index, dRdN * qnu);
                }

                if (thermal) {
                    double delta = cv.getHeatCapacity();
                    double dRdT = cv.getResidualVolumeDerivativeTemperature();
                    solver.addToResidual(el.index, dRdT * qe / delta);
                }
            }
        }

        /**
         * Assembles an outlet fluid source into the pressure Jacobian system
         */
        private void assemblePressureOutletSource(OutletSource outlet) {
            double qe = outlet.getEnergySource();

            for (Element el : mesh.elements(outlet.name())) {

                int i = el.index;
                CV cv = field.getControlVolume(el);

                for (Component nu : components.all()) {

                    double qnu = 0;
                    for (Phase phase : Phase.all())
                        qnu += outlet.getMassSource(el, phase, nu);

                    double dRdN = cv.getResidualVolumeDerivativeMolarMass(nu);
                    solver.addToResidual(i, dRdN * qnu);
                }

                if (thermal) {
                    double delta = cv.getHeatCapacity();
                    double dRdT = cv.getResidualVolumeDerivativeTemperature();
                    solver.addToResidual(i, dRdT * qe / delta);
                }
            }
        }

        /**
         * Builds the flux part of the Jacobian matrix and residual vector for
         * the pressure calculations
         */
        private void assemblePressureJacobianFlux() {
            for (Connection c : mesh.connections()) {

                Element here = mesh.here(c), there = mesh.there(c);
                int i = here.index, j = there.index;

                CV cvHere = field.getControlVolume(here);
                CV cvThere = field.getControlVolume(there);
                CS cs = field.getControlSurface(c);

                // Heat capacities on either side of the connection
                double deltaHere = cvHere.getHeatCapacity();
                double deltaThere = cvThere.getHeatCapacity();

                double dRdT_here = cvHere
                        .getResidualVolumeDerivativeTemperature();
                double dRdT_there = cvThere
                        .getResidualVolumeDerivativeTemperature();

                double ri = 0, rj = 0;

                // Darcy flux
                for (Transmissibility t : c.MD) {

                    Element ek = mesh.element(t);
                    int ik = ek.index;
                    double tk = t.k;
                    double b = cs.bodyForce(ek);
                    CV ck = field.getControlVolume(ek);

                    double Ji = 0, Jj = 0;

                    for (Phase phase : Phase.all()) {

                        Element up = cs.getUpstream(phase);
                        CV cvUp = field.getControlVolume(up);

                        // See if the phase is present upstream
                        if (!cvUp.isPhasePresent(phase))
                            continue;

                        // Phase potential
                        double psi = ck.getPhasePressure(phase)
                                + cs.getRho(phase) * b;

                        // Assemble component fluxes
                        for (Component nu : components.all()) {
                            double phaseCompMob = cvUp.getComponentMobility(
                                    phase, nu);

                            double dRdNhere = cvHere
                                    .getResidualVolumeDerivativeMolarMass(nu);
                            double dRdNthere = cvThere
                                    .getResidualVolumeDerivativeMolarMass(nu);

                            double dNdt = -phaseCompMob * tk * psi;
                            double d2Ndtp = -phaseCompMob * tk;

                            ri += dRdNhere * dNdt;
                            rj += -dRdNthere * dNdt;

                            Ji += dRdNhere * d2Ndtp;
                            Jj += -dRdNthere * d2Ndtp;
                        }

                        // Assemble convective thermal fluxes
                        if (thermal) {
                            double phaseEnergyMob = cvUp
                                    .getEnergyMobility(phase);

                            double dTdt = -phaseEnergyMob * tk * psi;
                            double d2Tdtp = -phaseEnergyMob * tk;

                            ri += dRdT_here * dTdt / deltaHere;
                            rj += -dRdT_there * dTdt / deltaThere;

                            Ji += dRdT_here * d2Tdtp / deltaHere;
                            Jj += -dRdT_there * d2Tdtp / deltaThere;
                        }
                    }

                    solver.addToJacobian(i, ik, Ji);
                    solver.addToJacobian(j, ik, Jj);
                }

                // Fourier flux
                if (thermal)
                    for (Transmissibility t : c.MF) {

                        Element ek = mesh.element(t);
                        double tk = t.k;
                        CV ck = field.getControlVolume(ek);

                        double F = tk * ck.getTemperature();

                        ri += -dRdT_here * F / deltaHere;
                        rj += dRdT_there * F / deltaThere;
                    }

                solver.addToResidual(i, ri);
                solver.addToResidual(j, rj);
            }
        }
    }

    /**
     * Solves the non-linear energy conservation law for the temperature
     */
    private class TemperatureNewtonRaphson {

        /**
         * Temperature correction
         */
        private double[] dT = new double[mesh.elements().size()];

        /**
         * Carries out a single Newton-Raphson iteration
         */
        public void solve(double dt) throws SolverFailure {

            // Assembles the advective flux part
            assembleTemperatureJacobianAdvectiveFlux();

            // Assembles the conductive flux part of the Jacobian matrix
            assembleTemperatureJacobianConductiveFlux();

            // Assembles the diagonal (elementwise) part of the Jacobian matrix
            assembleTemperatureJacobianDiagonal(dt);

            // Solve J * dx = -r for dx
            solver.solve();

            // Apply the temperature update
            updateTemperature(solver.getCorrection(dT));
        }

        /**
         * Updates the temperature, and checks it for validity
         * 
         * @throws SolverFailure
         *                 If a non-positive temperature was found
         */
        private void updateTemperature(double[] dT) throws SolverFailure {
            for (Element el : mesh.elements()) {
                int i = el.index;

                CV cv = field.getControlVolume(el);
                double T_old = cv.getTemperature();
                double T_new = T_old + dT[i];

                // Check for non-positive temperature
                if (T_new <= 0)
                    throw new SolverFailure(
                            "Non-positive temperature in element " + (i + 1)
                                    + ": " + T_old + " -> " + T_new);

                cv.setTemperature(T_new);
            }
        }

        /**
         * Builds the advective flux (Darcy) part of the temperature system.
         */
        private void assembleTemperatureJacobianAdvectiveFlux() {
            for (Connection c : mesh.connections()) {

                Element here = mesh.here(c), there = mesh.there(c);
                int i = here.index, j = there.index;

                CS cs = field.getControlSurface(c);

                double energyFlux = 0;
                for (Phase phase : Phase.all()) {

                    // Upstream direction
                    Element up = cs.getUpstream(phase);
                    CV cvUp = field.getControlVolume(up);

                    // Check for phase presence
                    if (!cvUp.isPhasePresent(phase))
                        continue;

                    double energyMob = cvUp.getEnergyMobility(phase);

                    for (Transmissibility t : c.MD) {

                        Element ek = mesh.element(t);
                        double tk = t.k;
                        double b = cs.bodyForce(ek);
                        CV ck = field.getControlVolume(ek);

                        // Phase potential
                        double psi = ck.getPhasePressure(phase)
                                + cs.getRho(phase) * b;

                        // Energy flux
                        energyFlux += energyMob * tk * psi;
                    }
                }

                solver.addToResidual(i, energyFlux);
                solver.addToResidual(j, -energyFlux);
            }
        }

        /**
         * Builds the conductive flux (Fourier) part of the temperature system
         */
        private void assembleTemperatureJacobianConductiveFlux() {
            for (Connection c : mesh.connections()) {

                Element here = mesh.here(c), there = mesh.there(c);
                int i = here.index, j = there.index;

                for (Transmissibility t : c.MF) {

                    Element ek = mesh.element(t);
                    int ik = ek.index;
                    double tk = t.k;
                    CV ck = field.getControlVolume(ek);

                    double T = ck.getTemperature();

                    solver.addToResidual(i, tk * T);
                    solver.addToResidual(j, -tk * T);

                    solver.addToJacobian(i, ik, tk);
                    solver.addToJacobian(j, ik, -tk);
                }
            }
        }

        /**
         * Builds the diagonal (elementwise) part of the temperature system
         */
        private void assembleTemperatureJacobianDiagonal(double dt) {
            for (Element el : mesh.elements()) {
                int i = el.index;

                CV cv = field.getControlVolume(el);

                double delta = cv.getHeatCapacity();
                double dT = cv.getTemperature() - T[i];

                solver.addToResidual(i, delta * dT / dt);
                solver.addToJacobian(i, i, delta / dt);
            }

            for (Source source : field.sources()) {

                switch (source.type()) {
                case FIXED:

                    // Nothing to do

                    break;
                case REGULAR:

                    assembleTemperatureSource((RegularSource) source);

                    break;
                case OUTLET:

                    assembleTemperatureSource((OutletSource) source);

                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported source type: " + source.type());
                }
            }
        }

        /**
         * Assembles an energy source into the temperature system
         */
        private void assembleTemperatureSource(RegularSource regular) {
            for (Element el : mesh.elements(regular.name())) {
                double qe = regular.getEnergySource();
                solver.addToResidual(el.index, -qe);
            }
        }

        /**
         * Assembles an energy source into the temperature system
         */
        private void assembleTemperatureSource(OutletSource outlet) {
            for (Element el : mesh.elements(outlet.name())) {
                double qe = outlet.getEnergySource();
                solver.addToResidual(el.index, -qe);
            }
        }
    }

    /**
     * Solves for the molar masses explicitly
     */
    private class MolarMassExplicit {

        /**
         * Solution corrections for the explicit molar mass calculations of each
         * component in each gridblock
         */
        final double[][] dN = new double[components.numComponents()][mesh
                .elements().size()];

        /**
         * Explicitly calculates the molar mass corrections by solving the
         * component conservation laws
         */
        public void solve(double dt) throws SolverFailure {
            for (Component nu : components.all())
                Arrays.fill(dN[nu.index()], 0);

            calculateMolarMassFlux(dt);

            calculateMolarMassSource(dt);

            // Enforce no change in locked cells
            for (int lock : locked)
                for (Component nu : components.all())
                    dN[nu.index()][lock] = 0;

            // Update the molar masses
            updateMolarMass();
        }

        /**
         * Updates the molar masses, while checking for negative mole numbers
         */
        private void updateMolarMass() throws SolverFailure {
            for (Element el : mesh.elements()) {
                int i = el.index;
                Composition composition = field.getControlVolume(el)
                        .getComposition();

                for (Component nu : components.all()) {
                    double Nold = N[i].getMoles(nu);
                    double Nnew = Nold + dN[nu.index()][i];

                    // Check for negative moles
                    if (Nnew < 0) {
                        String message = "Negative number of moles of component "
                                + nu.name()
                                + " in element "
                                + (i + 1)
                                + ": "
                                + Nold + " -> " + Nnew;

                        if (truncateNegativeMoles) {
                            System.err.println(message);
                            Nnew = 0;
                        } else
                            throw new SolverFailure(message);
                    }

                    composition.setMoles(nu, Nnew);
                }
            }
        }

        /**
         * Calculates changes due to fluxes
         */
        private void calculateMolarMassFlux(double dt) {
            double[] compFlux = new double[components.numComponents()];

            for (Connection c : mesh.connections()) {
                Element here = mesh.here(c), there = mesh.there(c);
                int i = here.index, j = there.index;

                CS cs = field.getControlSurface(c);

                Arrays.fill(compFlux, 0);
                for (Phase phase : Phase.all()) {

                    // Upstream direction
                    Element up = cs.getUpstream(phase);
                    CV cvUp = field.getControlVolume(up);

                    if (!cvUp.isPhasePresent(phase))
                        continue;

                    double D = cs.getDarcyFlux(phase);

                    EquationOfStateData eos = cvUp
                            .getEquationOfStateData(phase);

                    Composition N = eos.getComposition();
                    double xi = eos.getMolarDensity();

                    for (Component nu : components.all()) {
                        double C = N.getMoleFraction(nu);
                        compFlux[nu.index()] += C * xi * D;
                    }
                }

                for (Component nu : components.all()) {
                    int index = nu.index();
                    dN[index][i] -= dt * compFlux[index];
                    dN[index][j] += dt * compFlux[index];
                }
            }
        }

        /**
         * Calculates changes due to sources
         */
        private void calculateMolarMassSource(double dt) {
            for (Source source : field.sources()) {

                switch (source.type()) {
                case FIXED:

                    // Nothing to do

                    break;
                case REGULAR:

                    calculateMolarMassRegularSource(dt, (RegularSource) source);

                    break;
                case OUTLET:

                    calculateMolarMassOutletSource(dt, (OutletSource) source);

                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported source type: " + source.type());
                }
            }
        }

        /**
         * Calculates the molar mass change for a regular fluid source
         */
        private void calculateMolarMassRegularSource(double dt,
                RegularSource regular) {
            for (Element el : mesh.elements(regular.name()))
                for (Component nu : components.all()) {
                    double qnu = regular.getMassSource(nu);
                    dN[nu.index()][el.index] += dt * qnu;
                }
        }

        /**
         * Calculates the molar mass change for an outlet fluid source
         */
        private void calculateMolarMassOutletSource(double dt,
                OutletSource outlet) {
            for (Element el : mesh.elements(outlet.name())) {
                int i = el.index;

                for (Component nu : components.all())
                    for (Phase phase : Phase.all())
                        dN[nu.index()][i] += dt
                                * outlet.getMassSource(el, phase, nu);
            }
        }
    }

    /**
     * Calculates a new timestep size
     * 
     * @param dt
     *                Current (successful) timestep size
     * @return Proposed new timestep size
     */
    public double calculateDt(double dt) {

        // Residual volume target
        double R = residualVolumeFactor();

        // Non-linear pressure iteration target
        double NR = pressureIterationsFactor();

        // Throughput target
        double T = throughputFactor();

        // Find the most conservative
        double dt_R = dt * R, dt_NR = dt * NR, dt_T = dt * T;
        double dtNew = Double.MAX_VALUE;
        dtNew = Math.min(dtNew, dt_R);
        dtNew = Math.min(dtNew, dt_NR);
        dtNew = Math.min(dtNew, dt_T);

        dtNew = Math.min(dtNew, dtMax);

        // What's limiting us?
        String limit = "(none)";
        if (dtMax == dtNew)
            limit = "Maximum timestep";
        else if (dt_R == dtNew)
            limit = "Residual volume";
        else if (dt_NR == dtNew)
            limit = "Pressure iterations";
        else if (dt_T == dtNew)
            limit = "Throughput";

        System.out.format("R = %4f, NR = %4f, T = %4f. Limit: %s\n", R, NR, T,
                limit);

        dtNew = Math.max(dtNew, dtMin);

        return dtNew;
    }

    private double residualVolumeFactor() {
        double maxR = 0;

        for (Element el : mesh.elements()) {
            CV cv = field.getControlVolume(el);
            double R = cv.getResidualVolume();
            double Vp = el.volume * cv.getPorosity();
            maxR = Math.max(maxR, Math.abs(R) / Vp);
        }

        return dtFactor(maxR, targetResidualVolume);
    }

    private double pressureIterationsFactor() {
        return dtFactor(pressure.pressureIterations,
                targetNonLinearPressureIterations);
    }

    public double throughputFactor() {
        double dN = 0;

        for (Element el : mesh.elements()) {
            int i = el.index;
            double Ni = N[i].getMoles();
            double dNi = 0;
            for (Component nu : components.all())
                dNi += Math.abs(molarMass.dN[nu.index()][i]);
            dN = Math.max(dN, dNi / Ni);
        }

        return dtFactor(dN, targetThroughput);
    }

    private double dtFactor(double value, double target) {
        return (1 + lambda) * target / (value + lambda * target);
    }

    /**
     * Retracts the field data backwards
     * 
     * @param dt
     *                The timestep which failed
     */
    public void retractField(double dt) {
        field.retract(field.getTime() - dt, p, T, N);
    }
}
