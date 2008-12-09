package no.uib.cipr.rs.field;

import static no.uib.cipr.rs.field.Source.SourceType.FIXED;
import static no.uib.cipr.rs.field.Source.SourceType.OUTLET;
import static no.uib.cipr.rs.util.Constants.g;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.rs.field.Source.FixedSource;
import no.uib.cipr.rs.field.Source.OutletSource;
import no.uib.cipr.rs.fluid.Component;
import no.uib.cipr.rs.fluid.Components;
import no.uib.cipr.rs.fluid.Composition;
import no.uib.cipr.rs.fluid.EquationOfState;
import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.rock.RockFluid;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Tolerances;

/**
 * System (field) data at a given time step. It stores all primary and secondary
 * variables, calculates secondary variables, and performs initialisation.
 */
public class Field implements Serializable, Comparable<Field> {

    private static final long serialVersionUID = 9069550493378350020L;

    /**
     * Don't serialize the mesh along with the field
     */
    private final transient Mesh mesh;

    /**
     * Control volumes for storing the system state. One for each element
     */
    private final CV[] cv;

    /**
     * Control surfaces for storing fluxes. One for each connection
     */
    private final CS[] cs;

    /**
     * Set of fluid sources
     */
    private final Set<Source> sources;

    /**
     * Fluid components
     */
    private final Components components;

    /**
     * Current time [s]
     */
    private double t;

    /**
     * Sets up the field data, and calculates a pressure to fulfill local volume
     * balance
     */
    public Field(Configuration config, Mesh mesh, boolean thermal) {

        this.mesh = mesh;

        this.components = new Components(config);
        this.sources = readSources(config);

        EquationOfState eos = EquationOfState.create(config, components);

        cv = allocateControlVolumes(config, eos, thermal);
        cs = allocateControlSurfaces(config);

        /*
         * Get data for field initialisation
         */

        InitialValues init = new InitialValues(config, components);

        // Initialise the regular grid blocks
        volumeBalance(init);

        // Initialise the fixed source blocks. This also verifies that all
        // sources are present
        volumeBalance(sources, init);

        // Do a full recalculation of the secondaries
        calculateSecondaries();
    }

    /**
     * Reads in the fluid sources
     */
    private Set<Source> readSources(Configuration config) {
        Configuration sourceConfig = config.getConfiguration("Sources");
        Set<Source> sources = new HashSet<Source>();

        StringBuilder out;

        // Fixed sources
        Configuration fixed = sourceConfig.getConfiguration("Fixed");
        out = new StringBuilder();
        for (String key : fixed.keys()) {
            Source source = new Source.FixedSource(fixed.getConfiguration(key),
                    key, components);
            sources.add(source);
            out.append(key + " ");
        }
        System.out.println(fixed.trace() + out.toString());

        // Regular sources
        Configuration regular = sourceConfig.getConfiguration("Regular");
        out = new StringBuilder();
        for (String key : regular.keys()) {
            Source source = new Source.RegularSource(regular
                    .getConfiguration(key), key, components);
            sources.add(source);
            out.append(key + " ");
        }
        System.out.println(regular.trace() + out.toString());

        // Outlet sources
        Configuration outlet = sourceConfig.getConfiguration("Outlet");
        out = new StringBuilder();
        for (String key : outlet.keys()) {
            Source source = new Source.OutletSource(outlet
                    .getConfiguration(key), key, components);
            sources.add(source);
            out.append(key + " ");
        }
        System.out.println(outlet.trace() + out.toString());

        return sources;
    }

    /**
     * Allocates the control volumes, without initialisation
     */
    private CV[] allocateControlVolumes(Configuration config,
            EquationOfState eos, boolean thermal) {

        Map<String, RockFluid> rockFluid = readRockFluids(config);

        int numElements = mesh.elements.length;

        CV[] cv = new CV[numElements];
        for (Element el : mesh.elements) {
            String region = el.rock.getRegion();

            if (!rockFluid.containsKey(region))
                throw new IllegalArgumentException("Region \"" + region
                        + "\" has no associated rock/fluid properties");

            cv[el.index] = new CV(el, rockFluid.get(region), eos, components,
                    thermal);
        }

        return cv;
    }

    /**
     * Reads in the rock/fluid data
     */
    private Map<String, RockFluid> readRockFluids(Configuration config) {
        Configuration rockFluids = config.getConfiguration("RockFluid");

        Map<String, RockFluid> rockFluid = new HashMap<String, RockFluid>();
        for (String key : rockFluids.keys())
            rockFluid.put(key, RockFluid.create(rockFluids, key));

        return rockFluid;
    }

    /**
     * Creates the control surfaces, and sets up the body forces
     */
    private CS[] allocateControlSurfaces(Configuration config) {
        int numConnections = mesh.connections.length;

        // Read in centrifuge data
        // Later, can add in scaling of the gravity too
        Configuration bodyConfig = config.getConfiguration("BodyForces");
        double omega = bodyConfig.getDouble("omega", 0);
        double ax = bodyConfig.getDouble("ax", 0);
        double ay = bodyConfig.getDouble("ay", 0);
        if (omega > 0)
            System.out.println(bodyConfig.trace()
                    + "Applying centrifugal forces with omega=" + omega);

        CS[] cs = new CS[numConnections];
        for (Connection c : mesh.connections)
            cs[c.index] = new CS(c, omega, ax, ay);

        return cs;
    }

    /**
     * Constructs a new field based on the restart data of the given field
     */
    public Field(Field field, Configuration config, Mesh mesh, boolean thermal) {

        this.mesh = mesh;

        this.components = new Components(config);
        this.sources = readSources(config);

        EquationOfState eos = EquationOfState.create(config, components);

        cv = allocateControlVolumes(config, eos, thermal);
        cs = allocateControlSurfaces(config);

        copyPrimary(field);
        calculateSecondaries();

        /*
         * Initialise the field, and ensure elementwise volume balance in source
         * grid blocks
         */

        InitialValues init = new InitialValues(config, components);

        // Initialise the fixed source blocks. This also verifies that all
        // sources are present
        volumeBalance(sources, init);

        // Do a full recalculation of the secondaries
        calculateSecondaries();
    }

    /**
     * Copy the state of the given field into this field
     */
    private void copyPrimary(Field field) {
        t = field.t;

        for (int i = 0; i < cv.length; ++i) {
            cv[i].setPressure(field.cv[i].getPressure());
            cv[i].setReferencePressure(field.cv[i].getReferencePressure());
            cv[i].setTemperature(field.cv[i].getTemperature());

            Composition Nnew = cv[i].getComposition();
            Composition Nold = field.cv[i].getComposition();
            Components old = field.getComponents();
            for (Component nu : old)
                Nnew.setMoles(components.getComponent(nu.name()), Nold
                        .getMoles(nu));
        }
    }

    /**
     * Volume balance initialisation in regular elements
     */
    private void volumeBalance(InitialValues init) {
        for (Element el : mesh.elements()) {

            // Set initial state
            double[] C = setInitialState(init, el);

            // Adjust the state into to ensure volume balance and a hydrostatic
            // pressure distribution
            volumeBalance(init, C, el);
        }
    }

    /**
     * Volume balance initialisation in fixed source elements
     */
    private void volumeBalance(Set<Source> sources, InitialValues init) {
        for (Source source : sources)
            if (source.type() == FIXED) {

                FixedSource fixed = (FixedSource) source;

                double[] C = new double[components.numComponents()];
                for (Component nu : components)
                    C[nu.index()] = fixed.getComponentFraction(nu);

                for (Element el : mesh.elements(source.name())) {

                    // Set initial state in the source block
                    setInitialSourceState(init, C, fixed, el);

                    // Adjust the state into to ensure volume balance and a
                    // hydrostatic pressure distribution
                    volumeBalance(init, C, el);
                }
            }
    }

    /**
     * Sets initial pressure, temperature, and masses. This is to be adjusted
     * later by the volume balance
     * 
     * @param init
     *            Initial values as a function of depth
     * @param el
     *            Pertinent element
     * @return Component mass fractions
     */
    private double[] setInitialState(InitialValues init, Element el) {
        // Arbitrary amount of total moles
        final double N0 = 1;

        double z = el.center.z();
        int i = el.index;

        // Initial pressure and temperature
        cv[i].setPressure(init.getDatumPressure());
        cv[i].setReferencePressure(init.getDatumPressure());
        cv[i].setTemperature(init.getTemperature(z));

        // Get component fractions
        double[] C = new double[components.numComponents()];
        double sum = 0;
        for (Component nu : components) {
            int index = nu.index();
            C[index] = init.getComponentFraction(nu, z);
            sum += C[index];
        }

        // If nothing was given, fill with water
        if (sum == 0) {
            C[components.water().index()] = 1;
            sum = 1;
        }

        // Set initial composition
        Composition N = cv[i].getComposition();
        for (Component nu : components) {
            int index = nu.index();
            C[index] /= sum;
            N.setMoles(nu, N0 * C[index]);
        }

        return C;
    }

    /**
     * Sets initial pressure, temperature, and masses for fixed fluid source
     * grid blocks. This is to be adjusted later by the volume balance
     * 
     * @param init
     *            Initial values as a function of depth
     * @param C
     *            Normalised mass fractions
     * @param fixed
     *            The fixed source
     * @param el
     *            Pertinent element
     */
    private void setInitialSourceState(InitialValues init, double[] C,
            FixedSource fixed, Element el) {
        // Arbitrary amount of total moles
        final double N0 = 1;

        int i = el.index;

        // Initial pressure and temperature
        cv[i].setPressure(init.getDatumPressure());
        cv[i].setReferencePressure(init.getDatumPressure());
        cv[i].setTemperature(fixed.getTemperature());

        // Set initial composition
        Composition N = cv[i].getComposition();
        for (Component nu : components) {
            int index = nu.index();
            N.setMoles(nu, N0 * C[index]);
        }
    }

    /**
     * Establishes volume balance by adjusting the total amount of mass in the
     * element, and simultaneously establishes an approximate hydrostatic
     * pressure distribution
     * 
     * @param init
     *            Initial values
     * @param C
     *            Total composition fractions, normalised
     * @param el
     *            Pertinent element
     */
    private void volumeBalance(InitialValues init, double[] C, Element el) {
        // The number of iterations which must be done
        final int minIters = 3;

        // The number of iterations which can be done
        final int maxIters = 50;

        int i = el.index;
        double dz = (el.center.z() - init.getDatumDepth());

        cv[i].calculateSecondaries();

        Composition N = cv[i].getComposition();

        int iters = 0;
        do {

            // Change in R with the total mass
            double dRdNt = 0;
            for (Component nu : components)
                dRdNt += cv[i].getResidualVolumeDerivativeMolarMass(nu)
                        * C[nu.index()];

            if (dRdNt == 0)
                throw new IllegalArgumentException("dR/dN is zero in element "
                        + (i + 1));

            double Nt = N.getMoles();
            double dNt = -cv[i].getResidualVolume() / dRdNt;

            // Update such that there is a positive amount of mass
            double alpha = 1;
            while (Nt + alpha * dNt < 0)
                alpha /= 2;

            Nt += alpha * dNt;
            for (Component nu : components)
                N.setMoles(nu, Nt * C[nu.index()]);

            // Establish hydrostatic pressure
            double p = init.getDatumPressure();
            for (Phase phase : Phase.values())
                p -= g * cv[i].getMassDensity(phase)
                        * cv[i].getSaturation(phase) * dz;
            cv[i].setPressure(p);
            cv[i].setReferencePressure(p);

            cv[i].calculateSecondaries();

            iters++;

        } while (iters < minIters
                || (Math.abs(cv[i].getResidualVolume()) > Tolerances.smallEps && iters < maxIters));

        if (iters > maxIters)
            throw new IllegalArgumentException("Non-zero residual volume ("
                    + cv[i].getResidualVolume() + ") in element " + (i + 1));
    }

    /**
     * Gets the associated mesh
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Retrives the components database
     */
    public Components getComponents() {
        return components;
    }

    /**
     * Gets the control volume for the given element
     */
    public CV getControlVolume(Element el) {
        return cv[el.index];
    }

    /**
     * Gets the control surface for the given connection
     */
    public CS getControlSurface(Connection c) {
        return cs[c.index];
    }

    /**
     * Gets the fluid sources in the field
     */
    public Iterable<Source> sources() {
        return sources;
    }

    /**
     * Calculates all secondary variables in both the control volumes, on the
     * control surfaces, and in the sources
     */
    public void calculateSecondaries() {
        // Perform flash calculations and find new rock/fluid parameters
        for (CV CV : cv)
            CV.calculateSecondaries();

        // Calculate Darcy fluxes
        for (CS CS : cs)
            CS.calculateSecondaries(cv, mesh);

        // Update outlet sources
        for (Source q : sources)
            if (q.type() == OUTLET)
                ((OutletSource) q).update(cv, mesh);
    }

    /**
     * Gets field time
     * 
     * @return Time [s]
     */
    public double getTime() {
        return t;
    }

    /**
     * Changes the field time
     * 
     * @param dt
     *            Time increment [s]
     */
    public void changeTime(double dt) {
        t += dt;
    }

    /**
     * Retracts the field state to the given state. This recalculates all
     * secondary variables
     * 
     * @param t
     *            Time [s]
     * @param p
     *            Oil pressure [Pa]
     * @param T
     *            Temperature [K]
     * @param N
     *            Molar masses [mol]
     */
    public void retract(double t, double[] p, double[] T, Composition[] N) {
        this.t = t;

        for (int i = 0; i < cv.length; ++i) {
            cv[i].setPressure(p[i]);
            cv[i].setTemperature(T[i]);
            cv[i].getComposition().set(N[i]);
        }

        calculateSecondaries();
    }

    public int compareTo(Field o) {
        if (t > o.getTime())
            return 1;
        else if (t < o.getTime())
            return -1;
        else
            return 0;
    }
}
