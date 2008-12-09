package no.uib.cipr.rs.field;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import no.uib.cipr.rs.fluid.Component;
import no.uib.cipr.rs.fluid.Components;
import no.uib.cipr.rs.fluid.Composition;
import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.fluid.PhaseData;
import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.util.Configuration;

/**
 * Source (pressure, mass, and energy).
 */
public abstract class Source implements Serializable {

    private static final long serialVersionUID = 5048574447895373793L;

    /**
     * Types of sources
     */
    public enum SourceType {

        FIXED,

        REGULAR,

        OUTLET

    }

    /**
     * Name of the source
     */
    private final String name;

    /**
     * Creates a source with the given name
     */
    Source(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the source
     */
    public String name() {
        return name;
    }

    /**
     * Gets the source type
     */
    public abstract SourceType type();

    /**
     * A fixed source locks the whole system state in associated gridblocks.
     * Hence it acts as Dirichlet type boundary condition
     */
    public static class FixedSource extends Source {

        private static final long serialVersionUID = -3529445293494025899L;

        /**
         * Temperature [K]
         */
        private final double T;

        /**
         * Overall component fractions [-]
         */
        private final double[] C;

        /**
         * Reads in the source (temperature and component fractions)
         */
        public FixedSource(Configuration config, String name,
                Components components) {
            super(name);

            T = config.getDouble("Temperature");
            if (T < 0)
                throw new IllegalArgumentException(config.trace()
                        + "Temperature cannot be negative");

            C = new double[components.numComponents()];

            double sum = 0;
            for (Component nu : components) {
                int index = nu.index();
                C[index] = config.getDouble(nu.name(), 0);
                sum += C[index];

                if (C[index] < 0)
                    throw new IllegalArgumentException(config.trace()
                            + "Fraction of component " + nu
                            + " cannot be negative, but was " + C[index]);
            }

            // If nothing was given, fill with water
            if (sum == 0) {
                C[components.water().index()] = 1;
                sum = 1;
            }

            for (Component nu : components)
                C[nu.index()] /= sum;
        }

        /**
         * Returns the temperature
         * 
         * @return [K]
         */
        public double getTemperature() {
            return T;
        }

        /**
         * Returns the component fraction
         * 
         * @return [-]
         */
        public double getComponentFraction(Component nu) {
            return C[nu.index()];
        }

        @Override
        public SourceType type() {
            return SourceType.FIXED;
        }
    }

    /**
     * A regular source adds mass/energy to associated gridblocks. Hence it acts
     * as Neumann type boundary condition
     */
    public static class RegularSource extends Source {

        private static final long serialVersionUID = -6523099594558544650L;

        /**
         * Calculated mass sources [mol/s]
         */
        private double[] q;

        /**
         * Provided energy source [W]
         */
        private double qe;

        public RegularSource(Configuration config, String name,
                Components components) {
            super(name);

            q = new double[components.numComponents()];

            for (Component nu : components) {
                q[nu.index()] = config.getDouble(nu.name(), 0);
                if (q[nu.index()] < 0)
                    throw new IllegalArgumentException(config.trace()
                            + "Mass source of component " + nu.name()
                            + " cannot be negative");
            }

            qe = config.getDouble("Energy", 0);
            if (qe < 0)
                throw new IllegalArgumentException(config.trace()
                        + "Energy source cannot be negative");
        }

        /**
         * Gets the mass source rate
         * 
         * @param nu
         *            Component
         * @return [mol/s]
         */
        public double getMassSource(Component nu) {
            return q[nu.index()];
        }

        /**
         * Gets the energy source rate
         * 
         * @return [W]
         */
        public double getEnergySource() {
            return qe;
        }

        @Override
        public SourceType type() {
            return SourceType.REGULAR;
        }
    }

    /**
     * An outlet source subtracts mass/energy from associated gridblocks based
     * on fluid mobility. Hence it acts as a Robin type boundary condition
     */
    public static class OutletSource extends Source {

        private static final long serialVersionUID = -3006585603630497876L;

        /**
         * Components database
         */
        private final Components components;

        /**
         * Total mass outlet [mol/s]
         */
        private final double qt;

        /**
         * Energy outlet [W]
         */
        private final double qe;

        /**
         * Rates of each component in each control volume, ordered by phases
         */
        private PhaseData<double[][]> q;

        /**
         * Maps from mesh element index into an element index in the source
         * location
         */
        private Map<Integer, Integer> map;

        public OutletSource(Configuration config, String name,
                Components components) {
            super(name);
            this.components = components;

            qt = config.getDouble("Mass", 0);
            qe = config.getDouble("Energy", 0);

            if (qt > 0)
                throw new IllegalArgumentException(config.trace()
                        + "Mass source must be negative for an outlet");
            if (qe > 0)
                throw new IllegalArgumentException(config.trace()
                        + "Energy source must be negative for an outlet");
        }

        /**
         * Gets the mass source rate
         * 
         * @param el
         *            Element this outlets belong to
         * @param phase
         *            Phase
         * @param nu
         *            Component
         * @return [mol/s]
         */
        public double getMassSource(Element el, Phase phase, Component nu) {
            return q.get(phase)[map.get(el.index)][nu.index()];
        }

        /**
         * Gets the energy source rate
         * 
         * @return [W]
         */
        public double getEnergySource() {
            return qe;
        }

        /**
         * Updates the outlet source
         * 
         * @param cv
         *            Control volumes in the field
         * @param mesh
         *            Computational mesh
         */
        void update(CV[] cv, Mesh mesh) {
            PhaseDataDouble lambda = new PhaseDataDouble();

            for (Element el : mesh.elements(name())) {

                int i = el.index;
                CV CV = cv[i];

                // Determine the fractional flow function
                lambda.zero();
                double lambdaT = 0;
                for (Phase phase : Phase.values()) {
                    double l = CV.getPhaseMobility(phase);
                    lambda.set(phase, l);
                    lambdaT += l;
                }

                // Determine the component/phase flow
                for (Phase phase : Phase.all()) {
                    double[] qnu = q.get(phase)[map.get(i)];
                    Arrays.fill(qnu, 0);

                    Composition N = CV.getEquationOfStateData(phase)
                            .getComposition();
                    for (Component nu : components.all()) {
                        int index = nu.index();
                        double C = N.getMoleFraction(nu);
                        qnu[index] += C * lambda.get(phase) * qt / lambdaT;
                    }
                }
            }
        }

        @Override
        public SourceType type() {
            return SourceType.OUTLET;
        }
    }
}
